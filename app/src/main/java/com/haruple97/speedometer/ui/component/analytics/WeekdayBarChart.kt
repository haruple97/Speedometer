package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.WeekdayStat
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.TickMinor
import com.haruple97.speedometer.ui.theme.UnitGray
import kotlin.math.roundToInt

private val DAY_LABELS = arrayOf("일", "월", "화", "수", "목", "금", "토")

/**
 * 요일별 평균 속도 + 주행 횟수 dual-metric.
 * - 막대 높이 = 평균 속도
 * - 막대 상단 배지 = 주행 횟수 (0회면 배지 없음)
 * - 최대 속도 요일은 GaugeCaution 강조
 * - 데이터 없는 요일은 가로 점선
 */
@Composable
fun WeekdayBarChart(
    data: List<WeekdayStat>,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val dayLabelStyle = SpeedometerTextStyle.CaptionRegularStyle().copy(color = UnitGray)
    val speedLabelStyle = SpeedometerTextStyle.CaptionStyle().copy(color = DigitalWhite)
    val countLabelStyle = SpeedometerTextStyle.CaptionRegularStyle().copy(color = UnitGray)

    val byDay = (0..6).map { day -> data.firstOrNull { it.dayOfWeek == day } }
    val maxSpeed = (data.maxOfOrNull { it.avgSpeedKmh } ?: 0f).coerceAtLeast(1f)
    val fastestDay = data.maxByOrNull { it.avgSpeedKmh }?.dayOfWeek

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            val leftPad = 8.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 18.dp.toPx()
            val bottomPad = 26.dp.toPx()

            val plotLeft = leftPad
            val plotRight = size.width - rightPad
            val plotTop = topPad
            val plotBottom = size.height - bottomPad
            val plotW = plotRight - plotLeft
            val plotH = plotBottom - plotTop

            val barCount = 7
            val barSlot = plotW / barCount
            val barWidth = barSlot * 0.55f

            // baseline
            drawLine(
                color = TickMinor,
                start = Offset(plotLeft, plotBottom),
                end = Offset(plotRight, plotBottom),
                strokeWidth = 1.dp.toPx(),
            )

            byDay.forEachIndexed { index, stat ->
                val cx = plotLeft + barSlot * index + barSlot / 2f

                if (stat == null || stat.tripCount == 0) {
                    // 빈 요일 — 점선
                    drawLine(
                        color = TickMinor,
                        start = Offset(cx - barWidth / 2f, plotBottom),
                        end = Offset(cx + barWidth / 2f, plotBottom),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(3.dp.toPx(), 3.dp.toPx()), 0f,
                        ),
                    )
                } else {
                    val hPx = plotH * (stat.avgSpeedKmh / maxSpeed)
                    val color = if (index == fastestDay) GaugeCaution else GaugeSafe
                    drawRect(
                        color = color,
                        topLeft = Offset(cx - barWidth / 2f, plotBottom - hPx),
                        size = Size(barWidth, hPx),
                    )

                    // 평균 속도 숫자 상단
                    val speedText = "${speedUnit.fromKmh(stat.avgSpeedKmh).roundToInt()}"
                    drawCenteredLabel(
                        text = speedText,
                        style = speedLabelStyle,
                        cx = cx,
                        y = plotBottom - hPx - 10.dp.toPx(),
                        textMeasurer = textMeasurer,
                    )

                    // 주행 횟수 배지 (최상단)
                    val countText = "${stat.tripCount}"
                    drawCenteredLabel(
                        text = countText,
                        style = countLabelStyle,
                        cx = cx,
                        y = plotTop - 2.dp.toPx() + 6.dp.toPx(),
                        textMeasurer = textMeasurer,
                    )
                }

                // 요일 라벨
                drawCenteredLabel(
                    text = DAY_LABELS[index],
                    style = dayLabelStyle,
                    cx = cx,
                    y = size.height - 8.dp.toPx(),
                    textMeasurer = textMeasurer,
                )
            }
        }

        if (fastestDay != null && data.isNotEmpty()) {
            val fastestStat = data.firstOrNull { it.dayOfWeek == fastestDay }
            if (fastestStat != null) {
                Text(
                    text = "가장 빠른 날: ${DAY_LABELS[fastestDay]}요일 · 평균 " +
                        "${speedUnit.fromKmh(fastestStat.avgSpeedKmh).roundToInt()} ${speedUnit.label}",
                    style = SpeedometerTextStyle.CaptionRegularStyle(),
                    color = GaugeCaution,
                )
            }
        }
    }
}

private fun DrawScope.drawCenteredLabel(
    text: String,
    style: TextStyle,
    cx: Float,
    y: Float,
    textMeasurer: TextMeasurer,
) {
    val layout = textMeasurer.measure(text, style)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(cx - layout.size.width / 2f, y - layout.size.height / 2f),
    )
}
