package com.haruple97.speedometer.ui.screen

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.ui.component.settings.MaxSpeedDialog
import com.haruple97.speedometer.ui.component.settings.OverspeedThresholdDialog
import com.haruple97.speedometer.ui.component.settings.SettingActionRow
import com.haruple97.speedometer.ui.component.settings.SettingSection
import com.haruple97.speedometer.ui.component.settings.SettingSegmentRow
import com.haruple97.speedometer.ui.component.settings.SettingToggleRow
import com.haruple97.speedometer.ui.component.settings.SettingValueRow
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.viewmodel.SettingsViewModel

private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.haruple97.speedometer"

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SettingsScreen(
        preferences = preferences,
        onNavigateBack = onNavigateBack,
        onKeepScreenOnChange = viewModel::setKeepScreenOn,
        onHudModeChange = viewModel::setHudMode,
        onMaxSpeedChange = viewModel::setMaxSpeed,
        onOverspeedEnabledChange = viewModel::setOverspeedEnabled,
        onOverspeedThresholdChange = viewModel::setOverspeedThreshold,
        onSpeedUnitChange = viewModel::setSpeedUnit,
        onDistanceUnitChange = viewModel::setDistanceUnit,
        onShareApp = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Speedometer — $PLAY_STORE_URL")
            }
            context.startActivity(Intent.createChooser(intent, "앱 공유"))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onHudModeChange: (Boolean) -> Unit,
    onMaxSpeedChange: (Float) -> Unit,
    onOverspeedEnabledChange: (Boolean) -> Unit,
    onOverspeedThresholdChange: (Float) -> Unit,
    onSpeedUnitChange: (SpeedUnit) -> Unit,
    onDistanceUnitChange: (DistanceUnit) -> Unit,
    onShareApp: () -> Unit,
) {
    var showMaxSpeedDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DashboardBlack,
        topBar = {
            TopAppBar(
                title = { Text(text = "설정", color = DigitalWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = DigitalWhite,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DashboardDarkGray,
                    titleContentColor = DigitalWhite,
                    navigationIconContentColor = DigitalWhite,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingSection(title = "주행") {
                SettingToggleRow(
                    title = "화면 켜짐 유지",
                    checked = preferences.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                )
                SettingToggleRow(
                    title = "HUD 모드",
                    checked = preferences.hudMode,
                    onCheckedChange = onHudModeChange,
                )
                SettingValueRow(
                    title = "최대 속도",
                    value = "${preferences.maxSpeed.toInt()} km/h",
                    onClick = { showMaxSpeedDialog = true },
                )
            }

            SettingSection(title = "경고") {
                SettingToggleRow(
                    title = "과속 경고",
                    checked = preferences.overspeedEnabled,
                    onCheckedChange = onOverspeedEnabledChange,
                )
                AnimatedVisibility(
                    visible = preferences.overspeedEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    SettingValueRow(
                        title = "임계값",
                        value = "${preferences.overspeedThreshold.toInt()} km/h",
                        onClick = { showThresholdDialog = true },
                    )
                }
            }

            SettingSection(title = "단위") {
                SettingSegmentRow(
                    title = "속도",
                    options = listOf(
                        SpeedUnit.KMH to SpeedUnit.KMH.label,
                        SpeedUnit.MPH to SpeedUnit.MPH.label,
                    ),
                    selected = preferences.speedUnit,
                    onSelect = onSpeedUnitChange,
                )
                SettingSegmentRow(
                    title = "거리",
                    options = listOf(
                        DistanceUnit.KM to DistanceUnit.KM.label,
                        DistanceUnit.MI to DistanceUnit.MI.label,
                    ),
                    selected = preferences.distanceUnit,
                    onSelect = onDistanceUnitChange,
                )
            }

            SettingSection(title = "앱") {
                SettingActionRow(
                    title = "앱 공유",
                    onClick = onShareApp,
                    leadingIcon = Icons.Filled.Share,
                )
            }
        }
    }

    if (showMaxSpeedDialog) {
        MaxSpeedDialog(
            current = preferences.maxSpeed,
            onDismiss = { showMaxSpeedDialog = false },
            onConfirm = { value ->
                onMaxSpeedChange(value)
                showMaxSpeedDialog = false
            },
        )
    }

    if (showThresholdDialog) {
        OverspeedThresholdDialog(
            current = preferences.overspeedThreshold,
            onDismiss = { showThresholdDialog = false },
            onConfirm = { value ->
                onOverspeedThresholdChange(value)
                showThresholdDialog = false
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingsScreenPreview() {
    SpeedometerTheme {
        SettingsScreen(
            preferences = UserPreferences(
                maxSpeed = 350f,
                keepScreenOn = true,
                hudMode = false,
                overspeedEnabled = true,
                overspeedThreshold = 110f,
                speedUnit = SpeedUnit.KMH,
                distanceUnit = DistanceUnit.KM,
            ),
            onNavigateBack = {},
            onKeepScreenOnChange = {},
            onHudModeChange = {},
            onMaxSpeedChange = {},
            onOverspeedEnabledChange = {},
            onOverspeedThresholdChange = {},
            onSpeedUnitChange = {},
            onDistanceUnitChange = {},
            onShareApp = {},
        )
    }
}
