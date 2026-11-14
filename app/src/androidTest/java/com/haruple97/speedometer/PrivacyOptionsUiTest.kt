package com.haruple97.speedometer

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.ui.screen.SettingsScreen
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PrivacyOptionsUiTest {
    @get:Rule val compose = createComposeRule()
    private val label get() = InstrumentationRegistry.getInstrumentation().targetContext
        .getString(R.string.settings_privacy_options)

    private fun render(required: Boolean, onOpen: () -> Unit = {}) {
        compose.setContent {
            SpeedometerTheme {
                SettingsScreen(
                    preferences = UserPreferences(),
                    onKeepScreenOnChange = {}, onHudModeChange = {}, onMaxSpeedChange = {},
                    onOverspeedEnabledChange = {}, onOverspeedThresholdChange = {},
                    onSpeedUnitChange = {}, onDistanceUnitChange = {}, onApplyPreset = {},
                    onAutoRecordingEnabledChange = {}, onShareApp = {},
                    privacyOptionsRequired = required, onPrivacyOptions = onOpen,
                )
            }
        }
    }

    @Test fun requiredPrivacyEntryIsVisibleAndOpensOptions() {
        var opens = 0
        render(true) { opens++ }
        compose.onNodeWithText(label).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, opens) }
    }

    @Test fun unnecessaryPrivacyEntryIsAbsent() {
        render(false)
        compose.onNodeWithText(label).assertDoesNotExist()
    }
}
