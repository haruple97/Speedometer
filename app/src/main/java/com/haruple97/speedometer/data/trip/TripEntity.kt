package com.haruple97.speedometer.data.trip

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long,
    val distanceMeters: Float,
    val maxSpeedKmh: Float,
    val avgSpeedKmh: Float,
    val sampleCount: Int,
    val overspeedEventCount: Int,
    /** 트립 시작 시점의 과속 경고 임계값 스냅샷(km/h). 경고 OFF 이었으면 0. */
    val overspeedThresholdKmh: Float = 0f,
)
