package com.haruple97.speedometer.data.settings

import androidx.annotation.StringRes
import com.haruple97.speedometer.R

enum class SpeedPreset(
    @StringRes val displayNameRes: Int,
    @StringRes val descriptionRes: Int,
    val maxSpeedKmh: Float,
    val overspeedEnabled: Boolean,
    val overspeedThresholdKmh: Float,
    val hudMode: Boolean,
    val keepScreenOn: Boolean,
) {
    WALK(
        displayNameRes = R.string.preset_walk_name,
        descriptionRes = R.string.preset_walk_desc,
        maxSpeedKmh = 20f,
        overspeedEnabled = false,
        overspeedThresholdKmh = 15f,
        hudMode = false,
        keepScreenOn = true,
    ),
    BIKE(
        displayNameRes = R.string.preset_bike_name,
        descriptionRes = R.string.preset_bike_desc,
        maxSpeedKmh = 60f,
        overspeedEnabled = false,
        overspeedThresholdKmh = 40f,
        hudMode = false,
        keepScreenOn = true,
    ),
    DRIVE(
        displayNameRes = R.string.preset_drive_name,
        descriptionRes = R.string.preset_drive_desc,
        maxSpeedKmh = 200f,
        overspeedEnabled = true,
        overspeedThresholdKmh = 110f,
        hudMode = true,
        keepScreenOn = true,
    ),
    EXPRESS(
        displayNameRes = R.string.preset_express_name,
        descriptionRes = R.string.preset_express_desc,
        maxSpeedKmh = 350f,
        overspeedEnabled = false,
        overspeedThresholdKmh = 300f,
        hudMode = false,
        keepScreenOn = true,
    ),
}
