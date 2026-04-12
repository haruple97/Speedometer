package com.haruple97.speedometer.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeCritical
import com.haruple97.speedometer.ui.theme.GaugeDanger
import com.haruple97.speedometer.ui.theme.GaugeSafe
import kotlin.math.cos
import kotlin.math.sin

object GaugeGeometry {
    const val START_ANGLE = 150f
    const val SWEEP_ANGLE = 240f
    const val MAX_SPEED = 350f

    /** 속도(0~350)를 게이지 각도(150~390)로 변환 */
    fun speedToAngle(speed: Float): Float {
        val clamped = speed.coerceIn(0f, MAX_SPEED)
        return START_ANGLE + (clamped / MAX_SPEED) * SWEEP_ANGLE
    }

    /** 각도(도)와 반지름으로 원 위의 좌표를 계산 */
    fun angleToOffset(angleDeg: Float, center: Offset, radius: Float): Offset {
        val rad = Math.toRadians(angleDeg.toDouble())
        return Offset(
            x = center.x + radius * cos(rad).toFloat(),
            y = center.y + radius * sin(rad).toFloat()
        )
    }

    /** 속도에 따른 구간 색상 반환 */
    fun speedToColor(speed: Float): Color {
        return when {
            speed < 120f -> GaugeSafe
            speed < 200f -> GaugeCaution
            speed < 280f -> GaugeDanger
            else -> GaugeCritical
        }
    }
}
