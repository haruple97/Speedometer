package com.haruple97.speedometer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpeedometerColorScheme = darkColorScheme(
    primary = GaugeSafe,
    secondary = GaugeCaution,
    tertiary = AccentAmber,
    background = DashboardBlack,
    surface = DashboardDarkGray,
    onPrimary = DashboardBlack,
    onSecondary = DashboardBlack,
    onTertiary = DashboardBlack,
    onBackground = DigitalWhite,
    onSurface = DigitalWhite
)

@Composable
fun SpeedometerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = SpeedometerColorScheme,
        typography = Typography,
        content = content
    )
}
