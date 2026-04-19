package com.haruple97.speedometer.data.model

data class SpeedData(
    val speedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val accuracyMeters: Float? = null,
    val isGpsActive: Boolean = false,
    val timestamp: Long = 0L,
    val deltaDistanceMeters: Float = 0f,
)
