package com.haruple97.speedometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.speedometer.data.location.LocationRepositoryProvider
import com.haruple97.speedometer.data.model.SpeedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * 화면의 UI state 전용. 트립 기록은 앱 싱글턴 TripRecorder 가 MainActivity 범위에서 처리한다.
 */
class SpeedViewModel(application: Application) : AndroidViewModel(application) {

    private val speedFlow = LocationRepositoryProvider.get(application).speedFlow

    private val _speedState = MutableStateFlow(SpeedData())
    val speedState: StateFlow<SpeedData> = _speedState.asStateFlow()

    init {
        viewModelScope.launch {
            speedFlow
                .catch { /* GPS 오류 시 기본값 유지 */ }
                .collect { newData ->
                    val maxSpeed = maxOf(_speedState.value.maxSpeedKmh, newData.speedKmh)
                    _speedState.value = newData.copy(maxSpeedKmh = maxSpeed)
                }
        }
    }
}
