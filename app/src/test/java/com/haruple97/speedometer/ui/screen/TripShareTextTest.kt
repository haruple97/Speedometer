package com.haruple97.speedometer.ui.screen

import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.TripEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TripShareTextTest {
    private val trip = TripEntity(startedAt = 0, endedAt = 60_000, distanceMeters = 10_000f,
        maxSpeedKmh = 100f, avgSpeedKmh = 50f, sampleCount = 60, overspeedEventCount = 0)

    @Test fun distanceAndSpeedUnitsAreIndependentInSharedText() {
        for (speedUnit in SpeedUnit.entries) {
            for (distanceUnit in DistanceUnit.entries) {
                val text = buildShareText(trip, speedUnit, distanceUnit, "yyyy", Locale.US,
                    "%2\$s %3\$s / %5\$s %6\$s")
                val distance = if (distanceUnit == DistanceUnit.MI) "6.2 mi" else "10.0 km"
                val speed = if (speedUnit == SpeedUnit.MPH) "62 mph" else "100 km/h"
                assertEquals("$distance / $speed", text)
            }
        }
    }
}
