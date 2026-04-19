package com.haruple97.speedometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SettingsRepository
import com.haruple97.speedometer.data.settings.SpeedPreset
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.settings.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UserPreferences()
        )

    fun setMaxSpeed(value: Float) = viewModelScope.launch {
        repository.setMaxSpeed(value)
    }

    fun setKeepScreenOn(value: Boolean) = viewModelScope.launch {
        repository.setKeepScreenOn(value)
    }

    fun setHudMode(value: Boolean) = viewModelScope.launch {
        repository.setHudMode(value)
    }

    fun setOverspeedEnabled(value: Boolean) = viewModelScope.launch {
        repository.setOverspeedEnabled(value)
    }

    fun setOverspeedThreshold(value: Float) = viewModelScope.launch {
        repository.setOverspeedThreshold(value)
    }

    fun setSpeedUnit(unit: SpeedUnit) = viewModelScope.launch {
        repository.setSpeedUnit(unit)
    }

    fun setDistanceUnit(unit: DistanceUnit) = viewModelScope.launch {
        repository.setDistanceUnit(unit)
    }

    fun applyPreset(preset: SpeedPreset) = viewModelScope.launch {
        repository.applyPreset(preset)
    }

    fun setAutoRecordingEnabled(value: Boolean) = viewModelScope.launch {
        repository.setAutoRecordingEnabled(value)
    }
}
