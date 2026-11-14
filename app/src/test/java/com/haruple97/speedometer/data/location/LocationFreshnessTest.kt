package com.haruple97.speedometer.data.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationFreshnessTest {
    @Test fun freshFixIsValid() {
        assertTrue(isLocationFresh(10_000, 10_000))
        assertTrue(isLocationFresh(14_999, 10_000))
    }
    @Test fun missingExpiredAndFutureFixesAreInvalid() {
        assertFalse(isLocationFresh(10_000, null))
        assertFalse(isLocationFresh(15_000, 10_000))
        assertFalse(isLocationFresh(20_000, 10_000))
        assertFalse(isLocationFresh(9_999, 10_000))
    }
}
