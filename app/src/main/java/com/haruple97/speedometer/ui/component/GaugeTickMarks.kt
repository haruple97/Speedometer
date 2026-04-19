package com.haruple97.speedometer.ui.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.ui.theme.TickMajor
import com.haruple97.speedometer.ui.theme.TickMinor
import com.haruple97.speedometer.ui.util.GaugeGeometry

fun DrawScope.drawTickMarks(
    textMeasurer: TextMeasurer,
    tickTextStyle: TextStyle,
    maxSpeed: Float,
    speedUnit: SpeedUnit,
    scale: Float = 1f,
) {
    val center = Offset(size.width / 2, size.height / 2)
    val outerRadius = size.minDimension / 2 - 24.dp.toPx() * scale
    val majorTickLength = 18.dp.toPx() * scale
    val minorTickLength = 10.dp.toPx() * scale
    val textRadius = outerRadius - majorTickLength - 16.dp.toPx() * scale

    val maxDisplay = speedUnit.fromKmh(maxSpeed).toInt()
    val (majorStep, minorStep) = pickTickSteps(maxDisplay)

    var value = 0
    while (value <= maxDisplay) {
        val kmh = speedUnit.toKmh(value.toFloat())
        val angle = GaugeGeometry.speedToAngle(kmh, maxSpeed)
        val isMajor = value % majorStep == 0

        val outerPoint = GaugeGeometry.angleToOffset(angle, center, outerRadius)
        val tickLength = if (isMajor) majorTickLength else minorTickLength
        val innerPoint = GaugeGeometry.angleToOffset(angle, center, outerRadius - tickLength)

        drawLine(
            color = if (isMajor) TickMajor else TickMinor,
            start = outerPoint,
            end = innerPoint,
            strokeWidth = if (isMajor) 2.dp.toPx() * scale else 1.dp.toPx() * scale
        )

        if (isMajor) {
            val textPoint = GaugeGeometry.angleToOffset(angle, center, textRadius)
            val textLayoutResult = textMeasurer.measure(value.toString(), tickTextStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = textPoint.x - textLayoutResult.size.width / 2,
                    y = textPoint.y - textLayoutResult.size.height / 2
                )
            )
        }

        value += minorStep
    }
}

// 라벨 5~8개 수준의 일관된 밀도를 유지하도록 maxDisplay 구간별 step 선택.
// 120~175 구간은 50 이면 너무 드문드문(150→4개), 20 이면 과밀(150→9개)이라 25 로 보정.
private fun pickTickSteps(maxDisplay: Int): Pair<Int, Int> = when {
    maxDisplay <= 30  -> 5 to 1
    maxDisplay <= 60  -> 10 to 2
    maxDisplay <= 120 -> 20 to 5
    maxDisplay <= 175 -> 25 to 5
    maxDisplay <= 400 -> 50 to 10
    else              -> 100 to 20
}
