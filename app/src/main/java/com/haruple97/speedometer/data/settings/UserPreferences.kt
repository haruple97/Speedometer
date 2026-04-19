package com.haruple97.speedometer.data.settings

enum class SpeedUnit(
    val storageKey: String,
    val label: String,
    val multiplier: Float,
) {
    KMH("KMH", "km/h", 1f),
    MPH("MPH", "mph", 0.621371f);

    fun fromKmh(kmh: Float): Float = kmh * multiplier
    fun toKmh(display: Float): Float = display / multiplier

    companion object {
        fun fromStorage(key: String?): SpeedUnit =
            entries.firstOrNull { it.storageKey == key } ?: KMH
    }
}

enum class DistanceUnit(
    val storageKey: String,
    val label: String,
    val multiplier: Float,
) {
    KM("KM", "km", 1f),
    MI("MI", "mi", 0.621371f);

    fun fromKm(km: Float): Float = km * multiplier
    fun toKm(display: Float): Float = display / multiplier

    companion object {
        fun fromStorage(key: String?): DistanceUnit =
            entries.firstOrNull { it.storageKey == key } ?: KM
    }
}

data class UserPreferences(
    val maxSpeed: Float = 350f,
    val keepScreenOn: Boolean = true,
    val hudMode: Boolean = false,
    val overspeedEnabled: Boolean = false,
    val overspeedThreshold: Float = 110f,
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
)
