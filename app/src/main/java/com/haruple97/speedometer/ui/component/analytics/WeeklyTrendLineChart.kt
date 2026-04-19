package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.trip.WeekStat
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.theme.NeedleGlow
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.TickMinor
import com.haruple97.speedometer.ui.theme.UnitGray
import kotlin.math.roundToInt

/** 최근 8주 주간 총 거리 라인 그래프. 마지막 점(이번 주)은 크게 강조. */
@Composable
fun WeeklyTrendLineChart(
    trend: List<WeekStat>,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = SpeedometerTextStyle.CaptionRegularStyle().copy(color = UnitGray)

    if (trend.size < 2) {
        // SectionCard 내부에서 렌더되므로 여기선 간단히 플레이스홀더 텍스트만.
        androidx.compose.material3.Text(
            text = "주행 데이터가 2주 이상 누적되면 추이가 보여요.",
            style = SpeedometerTextStyle.Body2RegularStyle(),
            color = UnitGray,
        )
        return
    }

    val maxMeters = trend.maxOf { it.totalDistanceMeters }.coerceAtLeast(1f)
    val avgMeters = trend.sumOf { it.totalDistanceMeters.toDouble() }.toFloat() / trend.size

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        val leftPad = 36.dp.toPx()
        val rightPad = 12.dp.toPx()
        val topPad = 12.dp.toPx()
        val bottomPad = 24.dp.toPx()

        val plotLeft = leftPad
        val plotRight = size.width - rightPad
        val plotTop = topPad
        val plotBottom = size.height - bottomPad
        val plotW = plotRight - plotLeft
        val plotH = plotBottom - plotTop

        val stepX = if (trend.size > 1) plotW / (trend.size - 1) else 0f

        fun xFor(index: Int) = plotLeft + stepX * index
        fun yFor(meters: Float) =
            plotBottom - plotH * (meters / maxMeters).coerceIn(0f, 1f)

        // 평균 점선
        val avgY = yFor(avgMeters)
        drawLine(
            color = TickMinor,
            start = Offset(plotLeft, avgY),
            end = Offset(plotRight, avgY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f,
            ),
        )

        // 선 세그먼트
        for (i in 0 until trend.size - 1) {
            drawLine(
                color = GaugeSafe,
                start = Offset(xFor(i), yFor(trend[i].totalDistanceMeters)),
                end = Offset(xFor(i + 1), yFor(trend[i + 1].totalDistanceMeters)),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // 포인트
        trend.forEachIndexed { i, stat ->
            val isLast = i == trend.lastIndex
            val r = if (isLast) 6.dp.toPx() else 3.dp.toPx()
            val color = if (isLast) NeedleGlow else GaugeSafe
            drawCircle(
                color = color,
                radius = r,
                center = Offset(xFor(i), yFor(stat.totalDistanceMeters)),
            )
        }

        // Y축 간단 라벨 (max)
        val maxKm = maxMeters / 1000f
        val maxDisplay = distanceUnit.fromKm(maxKm)
        val maxLabel = textMeasurer.measure(
            "${maxDisplay.roundToInt()}",
            labelStyle,
        )
        drawText(
            textLayoutResult = maxLabel,
            topLeft = Offset(
                plotLeft - maxLabel.size.width - 4.dp.toPx(),
                plotTop - maxLabel.size.height / 2f,
            ),
        )

        // X축: 첫 주/이번 주 라벨
        val firstLabel = textMeasurer.measure("8주 전", labelStyle)
        drawText(
            textLayoutResult = firstLabel,
            topLeft = Offset(plotLeft, size.height - firstLabel.size.height - 2.dp.toPx()),
        )
        val lastLabel = textMeasurer.measure("이번 주", labelStyle)
        drawText(
            textLayoutResult = lastLabel,
            topLeft = Offset(
                plotRight - lastLabel.size.width,
                size.height - lastLabel.size.height - 2.dp.toPx(),
            ),
        )
    }
}
