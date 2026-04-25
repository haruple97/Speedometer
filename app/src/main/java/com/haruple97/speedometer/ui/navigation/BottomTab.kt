package com.haruple97.speedometer.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.ui.graphics.vector.ImageVector
import com.haruple97.speedometer.R

/**
 * 바텀 내비게이션에 노출되는 최상위 탭.
 *
 * - `filledIcon` / `outlinedIcon` 는 선택 / 비선택 상태에서 각각 쓰인다.
 * - 상세 화면(예: `trip_detail/...`)처럼 탭 자체가 아닌 라우트에 들어가 있을 때도
 *   소속 탭이 하이라이트되도록 [fromRoute] 에서 매핑한다.
 */
enum class BottomTab(
    val route: String,
    @StringRes val labelRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
) {
    Speedometer(
        route = Screen.Speedometer.route,
        labelRes = R.string.tab_dashboard,
        filledIcon = Icons.Filled.Speed,
        outlinedIcon = Icons.Outlined.Speed,
    ),
    History(
        route = Screen.History.route,
        labelRes = R.string.tab_history,
        filledIcon = Icons.Filled.Timeline,
        outlinedIcon = Icons.Outlined.Timeline,
    ),
    Settings(
        route = Screen.Settings.route,
        labelRes = R.string.tab_settings,
        filledIcon = Icons.Filled.Settings,
        outlinedIcon = Icons.Outlined.Settings,
    );

    companion object {
        /** 현재 내비 라우트가 어느 탭에 속하는지 반환. 미지의 라우트는 Speedometer 폴백. */
        fun fromRoute(route: String?): BottomTab = when {
            route == null -> Speedometer
            route.startsWith("trip_detail") -> History
            route == Screen.AnalyticsDetail.route -> History
            else -> entries.firstOrNull { it.route == route } ?: Speedometer
        }
    }
}
