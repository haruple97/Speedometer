package com.haruple97.speedometer.ui.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.NeedleGlow
import com.haruple97.speedometer.ui.theme.NeedleRed
import com.haruple97.speedometer.ui.util.GaugeGeometry

fun DrawScope.drawNeedle(
    currentSpeed: Float,
    maxSpeed: Float,
    scale: Float = 1f,
) {
    val center = Offset(size.width / 2, size.height / 2)
    val needleLength = size.minDimension / 2 - 60.dp.toPx() * scale
    val needleBaseWidth = 8.dp.toPx() * scale
    val pivotRadius = 10.dp.toPx() * scale
    val tailOffset = 20.dp.toPx() * scale

    val angle = GaugeGeometry.speedToAngle(currentSpeed.coerceIn(0f, maxSpeed), maxSpeed)

    // 바늘 경로 (위를 향한 삼각형, 0도 = 오른쪽)
    val needlePath = Path().apply {
        moveTo(center.x + needleLength, center.y)                    // 끝 (뾰족)
        lineTo(center.x - tailOffset, center.y - needleBaseWidth / 2) // 좌상단
        lineTo(center.x - tailOffset, center.y + needleBaseWidth / 2) // 좌하단
        close()
    }

    // 글로우 효과 (반투명 바늘)
    rotate(degrees = angle, pivot = center) {
        drawPath(
            path = needlePath,
            color = NeedleGlow,
            alpha = 0.4f
        )
    }

    // 메인 바늘
    rotate(degrees = angle, pivot = center) {
        drawPath(
            path = needlePath,
            color = NeedleRed
        )
    }

    // 중앙 피벗 원 (외곽)
    drawCircle(
        color = NeedleRed,
        radius = pivotRadius,
        center = center
    )

    // 중앙 피벗 원 (내부)
    drawCircle(
        color = DashboardDarkGray,
        radius = pivotRadius * 0.5f,
        center = center
    )
}
