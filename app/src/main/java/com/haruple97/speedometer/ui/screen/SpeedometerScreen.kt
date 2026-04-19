package com.haruple97.speedometer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.data.ads.AdUnitIds
import com.haruple97.speedometer.data.model.SpeedData
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.ui.component.AdBanner
import com.haruple97.speedometer.ui.component.SpeedInfoPanel
import com.haruple97.speedometer.ui.component.SpeedometerGauge
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.util.VibrationHelper
import com.haruple97.speedometer.viewmodel.SettingsViewModel
import com.haruple97.speedometer.viewmodel.SpeedViewModel

@Composable
fun SpeedometerRoute(
    speedViewModel: SpeedViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    isInPipMode: Boolean = false,
) {
    val speedData by speedViewModel.speedState.collectAsStateWithLifecycle()
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()

    SpeedometerScreen(
        speedData = speedData,
        preferences = preferences,
        isInPipMode = isInPipMode,
    )
}

@Composable
fun SpeedometerScreen(
    speedData: SpeedData,
    preferences: UserPreferences,
    isInPipMode: Boolean = false,
) {
    val context = LocalContext.current
    val vibrationHelper = remember(context) { VibrationHelper(context) }

    val isOverspeed = preferences.overspeedEnabled &&
        speedData.speedKmh > preferences.overspeedThreshold

    // 에지 트리거: false→true 전이 시 1회 진동. true 지속 중엔 재발화하지 않는다.
    LaunchedEffect(isOverspeed) {
        if (isOverspeed) vibrationHelper.vibrateOnce()
    }

    if (isInPipMode) {
        // PiP 모드: 게이지만 전체 화면에 표시. HUD 미적용.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack),
            contentAlignment = Alignment.Center
        ) {
            SpeedometerGauge(
                currentSpeed = speedData.speedKmh,
                maxSpeed = preferences.maxSpeed,
                speedUnit = preferences.speedUnit,
                isOverspeed = isOverspeed,
            )
        }
    } else {
        // 일반 모드: 상단 아이콘 Row 는 바텀 내비게이션으로 이관되어 제거됨.
        // HUD 활성 시 게이지/인포판만 좌우 반전.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 상단 배너 — HUD 반전 대상 밖에 배치해 반사 모드에서도 정방향 유지.
            AdBanner(adUnitId = AdUnitIds.bannerMain)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleX = if (preferences.hudMode) -1f else 1f),
                contentAlignment = Alignment.TopCenter
            ) {
                SpeedometerGauge(
                    currentSpeed = speedData.speedKmh,
                    maxSpeed = preferences.maxSpeed,
                    speedUnit = preferences.speedUnit,
                    isOverspeed = isOverspeed,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                SpeedInfoPanel(
                    maxSpeedKmh = speedData.maxSpeedKmh,
                    gpsAccuracy = speedData.accuracyMeters,
                    isGpsActive = speedData.isGpsActive,
                    speedUnit = preferences.speedUnit,
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
            preferences = UserPreferences(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, name = "HUD + Overspeed")
@Composable
private fun SpeedometerScreenHudPreview() {
    SpeedometerTheme {
        SpeedometerScreen(
            speedData = SpeedData(
                speedKmh = 150f,
                maxSpeedKmh = 142f,
                accuracyMeters = 3f,
                isGpsActive = true,
            ),
            preferences = UserPreferences(
                maxSpeed = 200f,
                hudMode = true,
                overspeedEnabled = true,
                overspeedThreshold = 110f,
            ),
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
            preferences = UserPreferences(),
            isInPipMode = true,
        )
    }
}
