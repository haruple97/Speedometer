package com.haruple97.speedometer.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.SpeedometerTheme

@Composable
fun SpeedometerGauge(
    currentSpeed: Float,
    modifier: Modifier = Modifier
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

    Box(
        modifier = modifier
            .widthIn(max = 330.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGaugeArc(currentSpeed = animatedSpeed)
            drawTickMarks(textMeasurer = textMeasurer)
            drawNeedle(currentSpeed = animatedSpeed)
        }

        // 디지털 속도 표시 (게이지 하단 빈 영역에 배치)
        DigitalSpeedDisplay(
            speed = animatedSpeed.toInt(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
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
