package com.haruple97.speedometer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.data.model.SpeedData
import com.haruple97.speedometer.ui.component.SpeedInfoPanel
import com.haruple97.speedometer.ui.component.SpeedometerGauge
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.viewmodel.SpeedViewModel

@Composable
fun SpeedometerRoute(
    viewModel: SpeedViewModel = viewModel()
) {
    val speedData by viewModel.speedState.collectAsStateWithLifecycle()

    SpeedometerScreen(speedData = speedData)
}

@Composable
fun SpeedometerScreen(
    speedData: SpeedData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBlack)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 게이지 (화면 상부 ~70%)
        SpeedometerGauge(
            currentSpeed = speedData.speedKmh,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 하단 정보 패널
        SpeedInfoPanel(
            maxSpeed = speedData.maxSpeedKmh,
            gpsAccuracy = speedData.accuracyMeters,
            isGpsActive = speedData.isGpsActive
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SpeedometerScreenPreview() {
    SpeedometerTheme {
        SpeedometerScreen(
            speedData = SpeedData(
                speedKmh = 85f,
                maxSpeedKmh = 142f,
                accuracyMeters = 3f,
                isGpsActive = true,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
