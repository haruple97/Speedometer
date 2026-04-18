package com.haruple97.speedometer.ui.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.speedometer.ui.theme.TickLight
import com.haruple97.speedometer.ui.theme.TickMajor
import com.haruple97.speedometer.ui.theme.TickMinor
import com.haruple97.speedometer.ui.util.GaugeGeometry

fun DrawScope.drawTickMarks(
    textMeasurer: TextMeasurer,
    scale: Float = 1f,
    maxSpeed: Float = GaugeGeometry.MAX_SPEED
) {
    val center = Offset(size.width / 2, size.height / 2)
    val outerRadius = size.minDimension / 2 - 24.dp.toPx() * scale
    val majorTickLength = 18.dp.toPx() * scale
    val minorTickLength = 10.dp.toPx() * scale
    val textRadius = outerRadius - majorTickLength - 16.dp.toPx() * scale

    val majorStep = 50
    val minorStep = 10

    var speed = 0
    while (speed <= maxSpeed.toInt()) {
        val angle = GaugeGeometry.speedToAngle(speed.toFloat())
        val isMajor = speed % majorStep == 0

        val outerPoint = GaugeGeometry.angleToOffset(angle, center, outerRadius)
        val tickLength = if (isMajor) majorTickLength else minorTickLength
        val innerPoint = GaugeGeometry.angleToOffset(angle, center, outerRadius - tickLength)

        // 눈금선
        drawLine(
            color = if (isMajor) TickMajor else TickMinor,
            start = outerPoint,
            end = innerPoint,
            strokeWidth = if (isMajor) 2.dp.toPx() * scale else 1.dp.toPx() * scale
        )

        // 주요 눈금 숫자 (50 단위)
        if (isMajor) {
            val textPoint = GaugeGeometry.angleToOffset(angle, center, textRadius)
            val text = speed.toString()
            val textStyle = TextStyle(
                color = TickLight,
                fontSize = 13.sp * scale,
                fontWeight = FontWeight.Medium
            )
            val textLayoutResult = textMeasurer.measure(text, textStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = textPoint.x - textLayoutResult.size.width / 2,
                    y = textPoint.y - textLayoutResult.size.height / 2
                )
            )
        }

        speed += minorStep
    }
}
