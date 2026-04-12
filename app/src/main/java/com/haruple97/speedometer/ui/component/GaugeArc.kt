package com.haruple97.speedometer.ui.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeCritical
import com.haruple97.speedometer.ui.theme.GaugeDanger
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.util.GaugeGeometry

fun DrawScope.drawGaugeArc(
    currentSpeed: Float,
    maxSpeed: Float = GaugeGeometry.MAX_SPEED
) {
    val strokeWidth = 12.dp.toPx()
    val padding = strokeWidth / 2 + 24.dp.toPx()
    val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
    val topLeft = Offset(padding, padding)

    // 배경 트랙
    drawArc(
        color = GaugeTrack,
        startAngle = GaugeGeometry.START_ANGLE,
        sweepAngle = GaugeGeometry.SWEEP_ANGLE,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // 진행 호 (속도에 따른 그라데이션)
    if (currentSpeed > 0f) {
        val sweepAngle = (currentSpeed.coerceIn(0f, maxSpeed) / maxSpeed) * GaugeGeometry.SWEEP_ANGLE

        val gradientColors = buildList {
            add(GaugeSafe)
            if (currentSpeed > 120f) add(GaugeCaution)
            if (currentSpeed > 200f) add(GaugeDanger)
            if (currentSpeed > 280f) add(GaugeCritical)
        }

        val brush = if (gradientColors.size >= 2) {
            Brush.sweepGradient(gradientColors)
        } else {
            Brush.sweepGradient(listOf(gradientColors.first(), gradientColors.first()))
        }

        drawArc(
            brush = brush,
            startAngle = GaugeGeometry.START_ANGLE,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 글로우 효과 (반투명 두꺼운 호)
        drawArc(
            brush = brush,
            startAngle = GaugeGeometry.START_ANGLE,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            alpha = 0.3f,
            style = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round)
        )
    }
}
