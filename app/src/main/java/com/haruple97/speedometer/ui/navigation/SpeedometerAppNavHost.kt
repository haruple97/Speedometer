package com.haruple97.speedometer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haruple97.speedometer.ui.screen.SettingsRoute
import com.haruple97.speedometer.ui.screen.SpeedometerRoute

@Composable
fun SpeedometerAppNavHost(isInPipMode: Boolean) {
    if (isInPipMode) {
        // PiP 모드에서는 설정 진입이 의미 없으므로 NavHost/백스택을 건너뛰고
        // 속도계만 직접 렌더링한다. Settings 화면에 있을 때 홈 키로 PiP 진입해도
        // 이 경로를 타서 자동으로 속도계 뷰가 노출된다.
        SpeedometerRoute(
            isInPipMode = true,
            onNavigateToSettings = {}
        )
        return
    }

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Speedometer.route
    ) {
        composable(Screen.Speedometer.route) {
            SpeedometerRoute(
                isInPipMode = false,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
