package com.haruple97.speedometer.ui.component.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.TripSampleEntity
import com.haruple97.speedometer.ui.theme.NeedleGlow
import com.haruple97.speedometer.ui.theme.NeedleRed
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.TickMinor
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.ui.util.GaugeGeometry
import kotlin.math.roundToInt

@Composable
fun SpeedTimelineChart(
    samples: List<TripSampleEntity>,
    maxSpeedKmh: Float,
    speedUnit: SpeedUnit,
    overspeedThresholdKmh: Float?,
    modifier: Modifier = Modifier,
) {
    if (samples.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "그래프를 그릴 데이터가 부족합니다",
                style = SpeedometerTextStyle.Body2RegularStyle(),
                color = UnitGray,
            )
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = SpeedometerTextStyle.CaptionRegularStyle().copy(color = UnitGray)
    val maxPointLabelStyle = SpeedometerTextStyle.Body2Style().copy(color = NeedleGlow)

    val startMs = samples.first().timestampMs
    val endMs = samples.last().timestampMs
    val durationMs = (endMs - startMs).coerceAtLeast(1L)
    val yMaxKmh = (maxSpeedKmh * 1.1f).coerceAtLeast(10f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val leftPad = 44.dp.toPx()
        val rightPad = 12.dp.toPx()
        val topPad = 16.dp.toPx()
        val bottomPad = 12.dp.toPx()

        val plotLeft = leftPad
        val plotRight = size.width - rightPad
        val plotTop = topPad
        val plotBottom = size.height - bottomPad
        val plotW = plotRight - plotLeft
        val plotH = plotBottom - plotTop

        fun xFor(ts: Long): Float = plotLeft + plotW * ((ts - startMs).toFloat() / durationMs)
        fun yFor(kmh: Float): Float = plotBottom - plotH * (kmh / yMaxKmh).coerceIn(0f, 1f)

        // 수평 그리드 + Y축 라벨
        val gridLines = 4
        for (i in 0..gridLines) {
            val valueKmh = yMaxKmh * (i.toFloat() / gridLines)
            val y = yFor(valueKmh)
            drawLine(
                color = TickMinor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx(),
            )
            drawLabel(
                textMeasurer = textMeasurer,
                text = speedUnit.fromKmh(valueKmh).roundToInt().toString(),
                style = labelStyle,
                anchorX = plotLeft - 6.dp.toPx(),
                anchorY = y,
                alignRight = true,
            )
        }

        // 과속 임계 수평 점선
        if (overspeedThresholdKmh != null && overspeedThresholdKmh < yMaxKmh) {
            val y = yFor(overspeedThresholdKmh)
            drawLine(
                color = NeedleRed,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f
                ),
            )
        }

        // 선 세그먼트 (구간별 색)
        val strokeWidth = 2.dp.toPx()
        for (i in 0 until samples.size - 1) {
            val a = samples[i]
            val b = samples[i + 1]
            val color = GaugeGeometry.speedToColor((a.speedKmh + b.speedKmh) / 2f)
            drawLine(
                color = color,
                start = Offset(xFor(a.timestampMs), yFor(a.speedKmh)),
                end = Offset(xFor(b.timestampMs), yFor(b.speedKmh)),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        // 최고 속도 포인트
        val maxSample = samples.maxByOrNull { it.speedKmh } ?: samples.first()
        val maxX = xFor(maxSample.timestampMs)
        val maxY = yFor(maxSample.speedKmh)
        drawCircle(
            color = NeedleRed,
            radius = 4.dp.toPx(),
            center = Offset(maxX, maxY),
        )
        val maxLabel = "${speedUnit.fromKmh(maxSample.speedKmh).roundToInt()} ${speedUnit.label}"
        val labelLayout = textMeasurer.measure(maxLabel, maxPointLabelStyle)
        val labelX = (maxX - labelLayout.size.width / 2f)
            .coerceIn(plotLeft, plotRight - labelLayout.size.width)
        val labelY = (maxY - labelLayout.size.height - 6.dp.toPx()).coerceAtLeast(plotTop)
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(labelX, labelY),
        )
    }
}

private fun DrawScope.drawLabel(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    anchorX: Float,
    anchorY: Float,
    alignRight: Boolean,
) {
    val layout = textMeasurer.measure(text, style)
    val x = if (alignRight) anchorX - layout.size.width else anchorX
    val y = anchorY - layout.size.height / 2f
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(x, y),
    )
}
