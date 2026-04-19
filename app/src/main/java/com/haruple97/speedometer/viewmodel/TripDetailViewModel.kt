package com.haruple97.speedometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.speedometer.data.database.DatabaseProvider
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.data.trip.TripSampleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val trip: TripEntity? = null,
    val samples: List<TripSampleEntity> = emptyList(),
    val isLoading: Boolean = true,
)

class TripDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.get(application).tripDao()

    private val _state = MutableStateFlow(TripDetailUiState())
    val state: StateFlow<TripDetailUiState> = _state.asStateFlow()

    fun load(tripId: Long) {
        viewModelScope.launch {
            val trip = dao.getTrip(tripId)
            val samples = if (trip != null) dao.getSamples(tripId) else emptyList()
            _state.value = TripDetailUiState(trip = trip, samples = samples, isLoading = false)
        }
    }
}
