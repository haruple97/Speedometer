package com.haruple97.speedometer.ui.screen

import android.app.Activity
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.filled.PrivacyTip
import com.haruple97.speedometer.data.ads.AdConsentManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.R
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
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.viewmodel.SettingsViewModel

private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.haruple97.speedometer"

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = viewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val consentManager = remember(context) { (context as? Activity)?.let(::AdConsentManager) }
    val privacyOptionsRequired by AdConsentManager.privacyOptionsRequired.collectAsStateWithLifecycle()
    val privacyError = stringResource(R.string.privacy_options_error)
    val shareMessage = stringResource(R.string.settings_share_message, PLAY_STORE_URL)
    val shareChooserTitle = stringResource(R.string.settings_share_app)

    SettingsScreen(
        preferences = preferences,
        onKeepScreenOnChange = viewModel::setKeepScreenOn,
        onHudModeChange = viewModel::setHudMode,
        onMaxSpeedChange = viewModel::setMaxSpeed,
        onOverspeedEnabledChange = viewModel::setOverspeedEnabled,
        onOverspeedThresholdChange = viewModel::setOverspeedThreshold,
        onSpeedUnitChange = viewModel::setSpeedUnit,
        onDistanceUnitChange = viewModel::setDistanceUnit,
        onApplyPreset = viewModel::applyPreset,
        onAutoRecordingEnabledChange = viewModel::setAutoRecordingEnabled,
        onPrivacyPolicy = {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://haruple97.github.io/privacy.html")))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, privacyError, Toast.LENGTH_LONG).show()
            }
        },
        privacyOptionsRequired = privacyOptionsRequired && consentManager != null,
        onPrivacyOptions = {
            consentManager?.showPrivacyOptions { error ->
                if (error != null) Toast.makeText(context, privacyError, Toast.LENGTH_LONG).show()
            }
        },
        onShareApp = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            context.startActivity(Intent.createChooser(intent, shareChooserTitle))
        },
    )
}

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
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
    privacyOptionsRequired: Boolean = false,
    onPrivacyOptions: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
) {
    var showMaxSpeedDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var pendingPreset by remember { mutableStateOf<SpeedPreset?>(null) }

    Scaffold(
        containerColor = DashboardBlack,
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingSection(title = stringResource(R.string.settings_section_preset)) {
                PresetRow(
                    onPresetClick = { pendingPreset = it },
                )
            }

            SettingSection(title = stringResource(R.string.settings_section_driving)) {
                SettingToggleRow(
                    title = stringResource(R.string.settings_keep_screen_on),
                    checked = preferences.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                )
                SettingToggleRow(
                    title = stringResource(R.string.settings_hud_mode),
                    checked = preferences.hudMode,
                    onCheckedChange = onHudModeChange,
                )
                SettingValueRow(
                    title = stringResource(R.string.settings_max_speed),
                    value = stringResource(
                        R.string.dialog_value_with_unit,
                        preferences.speedUnit.fromKmh(preferences.maxSpeed).toInt(),
                        preferences.speedUnit.label,
                    ),
                    onClick = { showMaxSpeedDialog = true },
                )
            }

            SettingSection(title = stringResource(R.string.settings_section_recording)) {
                SettingToggleRow(
                    title = stringResource(R.string.settings_auto_record),
                    checked = preferences.autoRecordingEnabled,
                    onCheckedChange = onAutoRecordingEnabledChange,
                )
                Text(
                    text = stringResource(R.string.settings_auto_record_hint),
                    style = SpeedometerTextStyle.CaptionRegularStyle(),
                    color = UnitGray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }

            SettingSection(title = stringResource(R.string.settings_section_warning)) {
                SettingToggleRow(
                    title = stringResource(R.string.settings_overspeed),
                    checked = preferences.overspeedEnabled,
                    onCheckedChange = onOverspeedEnabledChange,
                )
                AnimatedVisibility(
                    visible = preferences.overspeedEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    SettingValueRow(
                        title = stringResource(R.string.settings_overspeed_threshold),
                        value = stringResource(
                            R.string.dialog_value_with_unit,
                            preferences.speedUnit.fromKmh(preferences.overspeedThreshold).toInt(),
                            preferences.speedUnit.label,
                        ),
                        onClick = { showThresholdDialog = true },
                    )
                }
            }

            SettingSection(title = stringResource(R.string.settings_section_units)) {
                SettingSegmentRow(
                    title = stringResource(R.string.settings_unit_speed),
                    options = listOf(
                        SpeedUnit.KMH to SpeedUnit.KMH.label,
                        SpeedUnit.MPH to SpeedUnit.MPH.label,
                    ),
                    selected = preferences.speedUnit,
                    onSelect = onSpeedUnitChange,
                )
                SettingSegmentRow(
                    title = stringResource(R.string.settings_unit_distance),
                    options = listOf(
                        DistanceUnit.KM to DistanceUnit.KM.label,
                        DistanceUnit.MI to DistanceUnit.MI.label,
                    ),
                    selected = preferences.distanceUnit,
                    onSelect = onDistanceUnitChange,
                )
            }

            SettingSection(title = stringResource(R.string.settings_section_app)) {
                SettingActionRow(
                    title = stringResource(R.string.settings_privacy_policy),
                    onClick = onPrivacyPolicy,
                    leadingIcon = Icons.Filled.PrivacyTip,
                )
                if (privacyOptionsRequired) {
                    SettingActionRow(
                        title = stringResource(R.string.settings_privacy_options),
                        onClick = onPrivacyOptions,
                        leadingIcon = Icons.Filled.PrivacyTip,
                    )
                }
                SettingActionRow(
                    title = stringResource(R.string.settings_share_app),
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
