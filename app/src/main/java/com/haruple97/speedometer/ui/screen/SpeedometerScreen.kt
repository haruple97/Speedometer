package com.haruple97.speedometer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.viewmodel.SpeedViewModel

@Composable
fun SpeedometerRoute(
    viewModel: SpeedViewModel = viewModel(),
    isInPipMode: Boolean = false,
    onNavigateToSettings: () -> Unit
) {
    val speedData by viewModel.speedState.collectAsStateWithLifecycle()

    SpeedometerScreen(
        speedData = speedData,
        isInPipMode = isInPipMode,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
fun SpeedometerScreen(
    speedData: SpeedData,
    isInPipMode: Boolean = false,
    onNavigateToSettings: () -> Unit
) {
    if (isInPipMode) {
        // PiP 모드: 게이지만 전체 화면에 표시
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack),
            contentAlignment = Alignment.Center
        ) {
            SpeedometerGauge(currentSpeed = speedData.speedKmh)
        }
    } else {
        // 일반 모드: 전체 UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack)
                .statusBarsPadding()
                .padding(top = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "설정",
                        tint = DigitalWhite
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                SpeedometerGauge(
                    currentSpeed = speedData.speedKmh,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                SpeedInfoPanel(
                    maxSpeed = speedData.maxSpeedKmh,
                    gpsAccuracy = speedData.accuracyMeters,
                    isGpsActive = speedData.isGpsActive,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SpeedometerScreenPreview() {
    SpeedometerTheme {
        SpeedometerScreen(
            speedData = SpeedData(
                speedKmh = 250f,
                maxSpeedKmh = 142f,
                accuracyMeters = 3f,
                isGpsActive = true,
                timestamp = System.currentTimeMillis()
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 180,
    heightDp = 180,
    name = "PiP Mode"
)
@Composable
private fun SpeedometerScreenPipPreview() {
    SpeedometerTheme {
        SpeedometerScreen(
            speedData = SpeedData(
                speedKmh = 85f,
                maxSpeedKmh = 142f,
                accuracyMeters = 3f,
                isGpsActive = true
            ),
            isInPipMode = true,
            onNavigateToSettings = {}
        )
    }
}
