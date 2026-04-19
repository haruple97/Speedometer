package com.haruple97.speedometer.ui.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeCritical
import com.haruple97.speedometer.ui.theme.GaugeDanger
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.util.GaugeGeometry

fun DrawScope.drawGaugeArc(
    currentSpeed: Float,
    maxSpeed: Float,
    scale: Float = 1f,
) {
    val strokeWidth = 12.dp.toPx() * scale
    val padding = strokeWidth / 2 + 24.dp.toPx() * scale
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

    // 진행 호 — 세그먼트 드로잉.
    // 각 세그먼트의 색상은 그 세그먼트의 각도 위치에서만 결정되며 currentSpeed와 무관.
    if (currentSpeed > 0f) {
        val sweepAngle = (currentSpeed.coerceIn(0f, maxSpeed) / maxSpeed) * GaugeGeometry.SWEEP_ANGLE
        val segments = sweepAngle.toInt().coerceAtLeast(1)
        val step = sweepAngle / segments

        // 메인 호 세그먼트 — Round 캡으로 양 끝 둥글게.
        for (i in 0 until segments) {
            val segStart = GaugeGeometry.START_ANGLE + i * step
            val segCenterAngle = segStart + step * 0.5f
            val segSpeed = ((segCenterAngle - GaugeGeometry.START_ANGLE) / GaugeGeometry.SWEEP_ANGLE) * maxSpeed
            val color = gradientColorForSpeed(segSpeed)

            drawArc(
                color = color,
                startAngle = segStart,
                sweepAngle = step + 0.5f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 글로우: saveLayer로 합성 alpha 0.3을 한 번만 적용.
        val glowPaint = Paint().apply { alpha = 0.3f }
        drawContext.canvas.saveLayer(Rect(0f, 0f, size.width, size.height), glowPaint)
        for (i in 0 until segments) {
            val segStart = GaugeGeometry.START_ANGLE + i * step
            val segCenterAngle = segStart + step * 0.5f
            val segSpeed = ((segCenterAngle - GaugeGeometry.START_ANGLE) / GaugeGeometry.SWEEP_ANGLE) * maxSpeed
            val color = gradientColorForSpeed(segSpeed)
            val cap = if (i == 0 || i == segments - 1) StrokeCap.Round else StrokeCap.Butt

            drawArc(
                color = color,
                startAngle = segStart,
                sweepAngle = step + 0.5f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth * 2.5f, cap = cap)
            )
        }
        drawContext.canvas.restore()
    }
}

// 절대 속도(km/h)에 대한 연속 보간 그라데이션. maxSpeed 무관.
private fun gradientColorForSpeed(speed: Float): Color {
    return when {
        speed <= 120f -> lerp(GaugeSafe, GaugeCaution, (speed / 120f).coerceIn(0f, 1f))
        speed <= 200f -> lerp(GaugeCaution, GaugeDanger, ((speed - 120f) / 80f).coerceIn(0f, 1f))
        speed <= 280f -> lerp(GaugeDanger, GaugeCritical, ((speed - 200f) / 80f).coerceIn(0f, 1f))
        else -> GaugeCritical
    }
}
