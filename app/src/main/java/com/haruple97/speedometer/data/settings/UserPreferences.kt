package com.haruple97.speedometer.data.settings

enum class SpeedUnit(val storageKey: String, val label: String) {
    KMH("KMH", "km/h"),
    MPH("MPH", "mph");

    companion object {
        fun fromStorage(key: String?): SpeedUnit =
            entries.firstOrNull { it.storageKey == key } ?: KMH
    }
}

enum class DistanceUnit(val storageKey: String, val label: String) {
    KM("KM", "km"),
    MI("MI", "mi");

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
