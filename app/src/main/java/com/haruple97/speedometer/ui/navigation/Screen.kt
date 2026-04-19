package com.haruple97.speedometer.ui.navigation

sealed class Screen(val route: String) {
    data object Speedometer : Screen("speedometer")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object TripDetail : Screen("trip_detail/{tripId}") {
        const val ARG_TRIP_ID = "tripId"
        fun createRoute(tripId: Long): String = "trip_detail/$tripId"
    }
}
