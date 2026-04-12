package com.haruple97.speedometer.data.location

import com.haruple97.speedometer.data.model.SpeedData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    val speedFlow: Flow<SpeedData>
    fun startTracking()
    fun stopTracking()
}
