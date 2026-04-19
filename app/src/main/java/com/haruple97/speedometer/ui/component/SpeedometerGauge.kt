package com.haruple97.speedometer.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.TickLight
import com.haruple97.speedometer.ui.util.GaugeGeometry

private val REFERENCE_SIZE = 330.dp

@Composable
fun SpeedometerGauge(
    currentSpeed: Float,
    maxSpeed: Float = GaugeGeometry.DEFAULT_MAX_SPEED,
    speedUnit: SpeedUnit = SpeedUnit.KMH,
    isOverspeed: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "speedAnimation"
    )

    val textMeasurer = rememberTextMeasurer()
    val tickBaseStyle = SpeedometerTextStyle.Body2Style().copy(color = TickLight)

    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = REFERENCE_SIZE)
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val scale = (maxWidth / REFERENCE_SIZE).coerceIn(0.3f, 1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGaugeArc(currentSpeed = animatedSpeed, maxSpeed = maxSpeed, scale = scale)
            drawTickMarks(
                textMeasurer = textMeasurer,
                tickTextStyle = tickBaseStyle.copy(fontSize = tickBaseStyle.fontSize * scale),
                maxSpeed = maxSpeed,
                speedUnit = speedUnit,
                scale = scale,
            )
            drawNeedle(currentSpeed = animatedSpeed, maxSpeed = maxSpeed, scale = scale)
        }

        // 디지털 속도 표시 (게이지 하단 빈 영역에 배치)
        DigitalSpeedDisplay(
            speedKmh = animatedSpeed,
            speedUnit = speedUnit,
            isOverspeed = isOverspeed,
            scale = scale,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp * scale)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SpeedometerGaugePreview() {
    SpeedometerTheme {
        SpeedometerGauge(
            currentSpeed = 120f,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, name = "Max 150 km/h")
@Composable
private fun SpeedometerGaugeLowMaxPreview() {
    SpeedometerTheme {
        SpeedometerGauge(
            currentSpeed = 80f,
            maxSpeed = 150f,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, name = "mph mode")
@Composable
private fun SpeedometerGaugeMphPreview() {
    SpeedometerTheme {
        SpeedometerGauge(
            currentSpeed = 100f,
            maxSpeed = 200f,
            speedUnit = SpeedUnit.MPH,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}
