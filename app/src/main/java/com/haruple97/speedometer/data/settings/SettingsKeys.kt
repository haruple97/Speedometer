package com.haruple97.speedometer.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val MAX_SPEED = floatPreferencesKey("max_speed")
    val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    val HUD_MODE = booleanPreferencesKey("hud_mode")
    val OVERSPEED_ENABLED = booleanPreferencesKey("overspeed_enabled")
    val OVERSPEED_THRESHOLD = floatPreferencesKey("overspeed_threshold")
    val SPEED_UNIT = stringPreferencesKey("speed_unit")
    val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
    val AUTO_RECORDING_ENABLED = booleanPreferencesKey("auto_recording_enabled")
}
