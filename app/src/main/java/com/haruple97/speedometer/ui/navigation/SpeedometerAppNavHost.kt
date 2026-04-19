package com.haruple97.speedometer.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.haruple97.speedometer.ui.component.AppBottomBar
import com.haruple97.speedometer.ui.screen.HistoryRoute
import com.haruple97.speedometer.ui.screen.SettingsRoute
import com.haruple97.speedometer.ui.screen.SpeedometerRoute
import com.haruple97.speedometer.ui.screen.TripDetailRoute
import com.haruple97.speedometer.ui.theme.DashboardBlack

@Composable
fun SpeedometerAppNavHost(isInPipMode: Boolean) {
    // PiP 에서는 바텀 바/백스택이 의미 없으므로 속도계 단독 렌더링.
    if (isInPipMode) {
        SpeedometerRoute(isInPipMode = true)
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = DashboardBlack,
        // top/bottom inset 은 각 화면(statusBarsPadding / 내부 Scaffold) 와 바텀 바 자체가
        // 단독 담당하도록 비움. 외부 Scaffold 는 바텀 바 높이만 innerPadding 으로 전달.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppBottomBar(
                currentRoute = currentRoute,
                onTabSelected = { tab -> navController.navigateToTab(tab) },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Speedometer.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            tabComposable(Screen.Speedometer.route) {
                SpeedometerRoute(isInPipMode = false)
            }
            tabComposable(Screen.History.route) {
                HistoryRoute(
                    onTripSelected = { tripId ->
                        navController.navigate(Screen.TripDetail.createRoute(tripId))
                    },
                )
            }
            tabComposable(Screen.Settings.route) {
                SettingsRoute()
            }
            composable(
                route = Screen.TripDetail.route,
                arguments = listOf(
                    navArgument(Screen.TripDetail.ARG_TRIP_ID) { type = NavType.LongType }
                ),
            ) { backStack ->
                val tripId = backStack.arguments?.getLong(Screen.TripDetail.ARG_TRIP_ID)
                    ?: return@composable
                TripDetailRoute(
                    tripId = tripId,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/**
 * 바텀 탭 전환 표준 패턴:
 * - startDestination 까지 popUpTo (saveState) → 깊은 상세까지 포함한 현재 탭 하위 스택 저장
 * - launchSingleTop → 같은 탭 연타 시 중복 인스턴스 방지
 * - restoreState → 이전에 이 탭에 있었을 때 남긴 상세 화면까지 복원
 */
private fun androidx.navigation.NavController.navigateToTab(tab: BottomTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * 탭 destination 전용 composable — 탭 간 왕복 시 스냅 전환(모든 enter/exit None).
 * TripDetail 같은 드릴다운 destination 은 기본 composable 을 그대로 써서 fade 전환 유지.
 */
private fun NavGraphBuilder.tabComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        content = content,
    )
}
