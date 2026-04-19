package com.haruple97.speedometer.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.navigation.BottomTab
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray

/**
 * 앱 전역 바텀 내비게이션 바 (3-탭).
 *
 * 트렌디 규약:
 * - 선택 탭에만 라벨 노출(`alwaysShowLabel = false`) — 공간을 비우고 지금 위치에 주의 집중.
 * - 아이콘은 filled/outlined 스왑 + [Crossfade] 로 부드러운 선택 전이.
 * - 인디케이터는 [GaugeSafe] 계열 반투명 pill — 대시보드 톤과 일관.
 * - 상단 1dp 구분선으로 콘텐츠와 시각적 경계.
 * - 탭 전환 시 경량 [HapticFeedbackType.TextHandleMove] 피드백.
 */
@Composable
fun AppBottomBar(
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedTab = BottomTab.fromRoute(currentRoute)
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier) {
        HorizontalDivider(thickness = 1.dp, color = GaugeTrack)
        NavigationBar(
            containerColor = DashboardDarkGray,
            tonalElevation = 0.dp,
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(tab)
                        }
                    },
                    icon = {
                        Crossfade(
                            targetState = isSelected,
                            label = "${tab.name}-icon",
                        ) { selected ->
                            Icon(
                                imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                                contentDescription = tab.label,
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = SpeedometerTextStyle.CaptionStyle(),
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DigitalWhite,
                        selectedTextColor = DigitalWhite,
                        unselectedIconColor = UnitGray,
                        unselectedTextColor = UnitGray,
                        indicatorColor = GaugeSafe.copy(alpha = 0.2f),
                    ),
                )
            }
        }
    }
}
