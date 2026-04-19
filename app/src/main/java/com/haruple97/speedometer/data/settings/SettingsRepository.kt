package com.haruple97.speedometer.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    val preferencesFlow: Flow<UserPreferences> =
        context.settingsDataStore.data.map { prefs ->
            UserPreferences(
                maxSpeed = prefs[SettingsKeys.MAX_SPEED] ?: 350f,
                keepScreenOn = prefs[SettingsKeys.KEEP_SCREEN_ON] ?: true,
                hudMode = prefs[SettingsKeys.HUD_MODE] ?: false,
                overspeedEnabled = prefs[SettingsKeys.OVERSPEED_ENABLED] ?: false,
                overspeedThreshold = prefs[SettingsKeys.OVERSPEED_THRESHOLD] ?: 110f,
                speedUnit = SpeedUnit.fromStorage(prefs[SettingsKeys.SPEED_UNIT]),
                distanceUnit = DistanceUnit.fromStorage(prefs[SettingsKeys.DISTANCE_UNIT]),
                autoRecordingEnabled = prefs[SettingsKeys.AUTO_RECORDING_ENABLED] ?: true,
            )
        }

    suspend fun setMaxSpeed(value: Float) {
        context.settingsDataStore.edit { it[SettingsKeys.MAX_SPEED] = value }
    }

    suspend fun setKeepScreenOn(value: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.KEEP_SCREEN_ON] = value }
    }

    suspend fun setHudMode(value: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.HUD_MODE] = value }
    }

    suspend fun setOverspeedEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.OVERSPEED_ENABLED] = value }
    }

    suspend fun setOverspeedThreshold(value: Float) {
        context.settingsDataStore.edit { it[SettingsKeys.OVERSPEED_THRESHOLD] = value }
    }

    suspend fun setSpeedUnit(unit: SpeedUnit) {
        context.settingsDataStore.edit { it[SettingsKeys.SPEED_UNIT] = unit.storageKey }
    }

    suspend fun setDistanceUnit(unit: DistanceUnit) {
        context.settingsDataStore.edit { it[SettingsKeys.DISTANCE_UNIT] = unit.storageKey }
    }

    suspend fun setAutoRecordingEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.AUTO_RECORDING_ENABLED] = value }
    }

    // 프리셋의 5개 필드를 한 번의 edit으로 원자적 적용. 단위(km/mi)는 유지.
    suspend fun applyPreset(preset: SpeedPreset) {
        context.settingsDataStore.edit {
            it[SettingsKeys.MAX_SPEED] = preset.maxSpeedKmh
            it[SettingsKeys.OVERSPEED_ENABLED] = preset.overspeedEnabled
            it[SettingsKeys.OVERSPEED_THRESHOLD] = preset.overspeedThresholdKmh
            it[SettingsKeys.HUD_MODE] = preset.hudMode
            it[SettingsKeys.KEEP_SCREEN_ON] = preset.keepScreenOn
        }
    }
}
