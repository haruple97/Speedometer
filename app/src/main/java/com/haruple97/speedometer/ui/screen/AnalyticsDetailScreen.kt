package com.haruple97.speedometer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.ui.component.analytics.AnalyticsInsightBuilder
import com.haruple97.speedometer.ui.component.analytics.AnalyticsSummaryHeader
import com.haruple97.speedometer.ui.component.analytics.DrivingInsightRow
import com.haruple97.speedometer.ui.component.analytics.MilestoneRow
import com.haruple97.speedometer.ui.component.analytics.MonthHeatmap
import com.haruple97.speedometer.ui.component.analytics.PersonalRecordGrid
import com.haruple97.speedometer.ui.component.analytics.SectionCard
import com.haruple97.speedometer.ui.component.analytics.SpeedZonePie
import com.haruple97.speedometer.ui.component.analytics.TimeOfDayHistogram
import com.haruple97.speedometer.ui.component.analytics.WeekComparisonRow
import com.haruple97.speedometer.ui.component.analytics.WeekdayBarChart
import com.haruple97.speedometer.ui.component.analytics.WeeklyTrendLineChart
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.viewmodel.AnalyticsUiState
import com.haruple97.speedometer.viewmodel.AnalyticsViewModel
import com.haruple97.speedometer.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsDetailRoute(
    onNavigateBack: () -> Unit,
    onTripClick: (Long) -> Unit,
    viewModel: AnalyticsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
    AnalyticsDetailScreen(
        state = state,
        preferences = preferences,
        onNavigateBack = onNavigateBack,
        onTripClick = onTripClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDetailScreen(
    state: AnalyticsUiState,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onTripClick: (Long) -> Unit,
) {
    Scaffold(
        containerColor = DashboardBlack,
        // 외부 AppBottomBar 가 이미 navigationBarsPadding 으로 bottom 인셋 처리 중이라
        // 내부 Scaffold 는 인셋 재주입 금지 (기본값 systemBars 는 bottom 까지 포함해 이중 패딩 유발).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("상세 분석", color = DigitalWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = DigitalWhite,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DashboardDarkGray,
                    titleContentColor = DigitalWhite,
                    navigationIconContentColor = DigitalWhite,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.loading) return@Column
            if (state.isEmpty) {
                Text(
                    text = "분석할 주행 기록이 부족합니다.\n몇 번 주행 후 다시 열어보세요.",
                    style = SpeedometerTextStyle.Body1RegularStyle(),
                    color = UnitGray,
                    modifier = Modifier.padding(top = 64.dp),
                )
                return@Column
            }

            AnalyticsSummaryHeader(
                thisMonth = state.thisMonth,
                total = state.total,
                speedUnit = preferences.speedUnit,
                distanceUnit = preferences.distanceUnit,
            )

            state.weekComparison?.let { comparison ->
                SectionCard(
                    title = "이번 주 vs 지난 주",
                    subtitle = "주요 지표의 변화",
                ) {
                    WeekComparisonRow(
                        comparison = comparison,
                        speedUnit = preferences.speedUnit,
                        distanceUnit = preferences.distanceUnit,
                    )
                }
            }

            if (state.weeklyTrend.isNotEmpty()) {
                SectionCard(
                    title = "주간 추이",
                    subtitle = "최근 8주 총 주행 거리",
                ) {
                    WeeklyTrendLineChart(
                        trend = state.weeklyTrend,
                        distanceUnit = preferences.distanceUnit,
                    )
                }
            }

            SectionCard(
                title = "요일별 평균 속도",
                subtitle = "요일마다의 주행 패턴",
            ) {
                WeekdayBarChart(data = state.weekday, speedUnit = preferences.speedUnit)
            }

            SectionCard(
                title = "시간대별 주행 빈도",
                subtitle = "하루 중 언제 가장 많이 달리는지",
            ) {
                TimeOfDayHistogram(data = state.hour)
            }

            SectionCard(
                title = "속도 구간별 누적 거리",
                subtitle = "안전·주의·위험·극한 구간 비중",
            ) {
                SpeedZonePie(
                    stat = state.speedZones,
                    distanceUnit = preferences.distanceUnit,
                )
            }

            SectionCard(
                title = "이번 달 주행",
                subtitle = monthTitle(),
            ) {
                MonthHeatmap(
                    dayStats = state.monthDays,
                    distanceUnit = preferences.distanceUnit,
                )
            }

            SectionCard(
                title = "드라이빙 인사이트",
                subtitle = "당신의 주행 패턴 요약",
            ) {
                val insights = AnalyticsInsightBuilder.build(
                    weekday = state.weekday,
                    hour = state.hour,
                    comparison = state.weekComparison,
                    speedUnit = preferences.speedUnit,
                )
                DrivingInsightRow(insights = insights)
            }

            SectionCard(
                title = "누적 마일스톤",
                subtitle = "다음 목표까지의 여정",
            ) {
                MilestoneRow(
                    totalDistanceMeters = state.total.totalDistanceMeters,
                    distanceUnit = preferences.distanceUnit,
                )
            }

            SectionCard(
                title = "개인 기록",
                subtitle = "탭해서 해당 주행 상세로",
            ) {
                PersonalRecordGrid(
                    topDistance = state.topDistanceTrip,
                    topMaxSpeed = state.topMaxSpeedTrip,
                    topDuration = state.topDurationTrip,
                    topOverspeed = state.topOverspeedTrip,
                    speedUnit = preferences.speedUnit,
                    onTripClick = onTripClick,
                )
            }
        }
    }
}

private fun monthTitle(): String {
    val cal = Calendar.getInstance()
    val fmt = SimpleDateFormat("yyyy년 M월", Locale.KOREAN)
    return fmt.format(cal.time)
}
