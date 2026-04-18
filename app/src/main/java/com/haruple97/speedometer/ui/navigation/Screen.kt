package com.haruple97.speedometer.ui.navigation

sealed class Screen(val route: String) {
    data object Speedometer : Screen("speedometer")
    data object Settings : Screen("settings")
}
