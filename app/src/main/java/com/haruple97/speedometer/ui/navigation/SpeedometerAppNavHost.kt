package com.haruple97.speedometer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.haruple97.speedometer.ui.screen.HistoryRoute
import com.haruple97.speedometer.ui.screen.SettingsRoute
import com.haruple97.speedometer.ui.screen.SpeedometerRoute
import com.haruple97.speedometer.ui.screen.TripDetailRoute

@Composable
fun SpeedometerAppNavHost(isInPipMode: Boolean) {
    if (isInPipMode) {
        // PiP 모드에서는 설정/기록 진입이 의미 없으므로 NavHost/백스택을 건너뛴다.
        SpeedometerRoute(
            isInPipMode = true,
            onNavigateToSettings = {},
            onNavigateToHistory = {},
        )
        return
    }

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Speedometer.route,
    ) {
        composable(Screen.Speedometer.route) {
            SpeedometerRoute(
                isInPipMode = false,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
            )
        }
        composable(Screen.Settings.route) {
            SettingsRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.History.route) {
            HistoryRoute(
                onNavigateBack = { navController.popBackStack() },
                onTripSelected = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                },
            )
        }
        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(
                navArgument(Screen.TripDetail.ARG_TRIP_ID) { type = NavType.LongType }
            ),
        ) { backStack ->
            val tripId = backStack.arguments?.getLong(Screen.TripDetail.ARG_TRIP_ID) ?: return@composable
            TripDetailRoute(
                tripId = tripId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
