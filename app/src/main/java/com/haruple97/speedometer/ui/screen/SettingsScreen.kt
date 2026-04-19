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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedPreset
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.ui.component.settings.MaxSpeedDialog
import com.haruple97.speedometer.ui.component.settings.OverspeedThresholdDialog
import com.haruple97.speedometer.ui.component.settings.PresetConfirmDialog
import com.haruple97.speedometer.ui.component.settings.PresetRow
import com.haruple97.speedometer.ui.component.settings.SettingActionRow
import com.haruple97.speedometer.ui.component.settings.SettingSection
import com.haruple97.speedometer.ui.component.settings.SettingSegmentRow
import com.haruple97.speedometer.ui.component.settings.SettingToggleRow
import com.haruple97.speedometer.ui.component.settings.SettingValueRow
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray
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
        onApplyPreset = viewModel::applyPreset,
        onAutoRecordingEnabledChange = viewModel::setAutoRecordingEnabled,
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
    onApplyPreset: (SpeedPreset) -> Unit,
    onAutoRecordingEnabledChange: (Boolean) -> Unit,
    onShareApp: () -> Unit,
) {
    var showMaxSpeedDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var pendingPreset by remember { mutableStateOf<SpeedPreset?>(null) }

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
            SettingSection(title = "주행 프리셋") {
                PresetRow(
                    onPresetClick = { pendingPreset = it },
                )
            }

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
                    value = "${preferences.speedUnit.fromKmh(preferences.maxSpeed).toInt()} ${preferences.speedUnit.label}",
                    onClick = { showMaxSpeedDialog = true },
                )
            }

            SettingSection(title = "기록") {
                SettingToggleRow(
                    title = "주행 자동 기록",
                    checked = preferences.autoRecordingEnabled,
                    onCheckedChange = onAutoRecordingEnabledChange,
                )
                Text(
                    text = "꺼짐 시 이후 주행은 기록 화면에 저장되지 않습니다.",
                    style = SpeedometerTextStyle.CaptionRegularStyle(),
                    color = UnitGray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
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
                        value = "${preferences.speedUnit.fromKmh(preferences.overspeedThreshold).toInt()} ${preferences.speedUnit.label}",
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
            currentKmh = preferences.maxSpeed,
            speedUnit = preferences.speedUnit,
            onDismiss = { showMaxSpeedDialog = false },
            onConfirm = { kmh ->
                onMaxSpeedChange(kmh)
                showMaxSpeedDialog = false
            },
        )
    }

    if (showThresholdDialog) {
        OverspeedThresholdDialog(
            currentKmh = preferences.overspeedThreshold,
            speedUnit = preferences.speedUnit,
            onDismiss = { showThresholdDialog = false },
            onConfirm = { kmh ->
                onOverspeedThresholdChange(kmh)
                showThresholdDialog = false
            },
        )
    }

    pendingPreset?.let { preset ->
        PresetConfirmDialog(
            preset = preset,
            speedUnit = preferences.speedUnit,
            onDismiss = { pendingPreset = null },
            onConfirm = {
                onApplyPreset(preset)
                pendingPreset = null
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
            onApplyPreset = {},
            onAutoRecordingEnabledChange = {},
            onShareApp = {},
        )
    }
}
