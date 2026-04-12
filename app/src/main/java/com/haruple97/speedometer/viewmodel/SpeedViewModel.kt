package com.haruple97.speedometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.speedometer.data.location.DefaultLocationRepository
import com.haruple97.speedometer.data.location.LocationRepository
import com.haruple97.speedometer.data.model.SpeedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SpeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LocationRepository = DefaultLocationRepository(application)

    private val _speedState = MutableStateFlow(SpeedData())
    val speedState: StateFlow<SpeedData> = _speedState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.speedFlow
                .catch { /* GPS 오류 시 기본값 유지 */ }
                .collect { newData ->
                    val maxSpeed = maxOf(_speedState.value.maxSpeedKmh, newData.speedKmh)
                    _speedState.value = newData.copy(maxSpeedKmh = maxSpeed)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopTracking()
    }
}
