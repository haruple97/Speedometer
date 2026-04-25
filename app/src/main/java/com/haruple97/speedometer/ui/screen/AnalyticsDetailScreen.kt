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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.R
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.ui.component.analytics.AnalyticsInsightBuilder
import com.haruple97.speedometer.ui.component.analytics.AnalyticsSummaryHeader
import com.haruple97.speedometer.ui.component.analytics.rememberInsightSentences
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
                title = { Text(stringResource(R.string.analytics_title), color = DigitalWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
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
                    text = stringResource(R.string.analytics_empty),
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
                    title = stringResource(R.string.analytics_section_compare),
                    subtitle = stringResource(R.string.analytics_section_compare_sub),
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
                    title = stringResource(R.string.analytics_section_trend),
                    subtitle = stringResource(R.string.analytics_section_trend_sub),
                ) {
                    WeeklyTrendLineChart(
                        trend = state.weeklyTrend,
                        distanceUnit = preferences.distanceUnit,
                    )
                }
            }

            SectionCard(
                title = stringResource(R.string.analytics_section_weekday),
                subtitle = stringResource(R.string.analytics_section_weekday_sub),
            ) {
                WeekdayBarChart(data = state.weekday, speedUnit = preferences.speedUnit)
            }

            SectionCard(
                title = stringResource(R.string.analytics_section_hour),
                subtitle = stringResource(R.string.analytics_section_hour_sub),
            ) {
                TimeOfDayHistogram(data = state.hour)
            }

            SectionCard(
                title = stringResource(R.string.analytics_section_zones),
                subtitle = stringResource(R.string.analytics_section_zones_sub),
            ) {
                SpeedZonePie(
                    stat = state.speedZones,
                    distanceUnit = preferences.distanceUnit,
                )
            }

            SectionCard(
                title = stringResource(R.string.analytics_section_month),
                subtitle = monthTitle(R.string.format_date_year_month),
            ) {
                MonthHeatmap(
                    dayStats = state.monthDays,
                    distanceUnit = preferences.distanceUnit,
                )
            }

            SectionCard(
                title = stringResource(R.string.analytics_section_insight),
                subtitle = stringResource(R.string.analytics_section_insight_sub),
            ) {
                val tokens = remember(state.weekday, state.hour, state.weekComparison, preferences.speedUnit) {
                    AnalyticsInsightBuilder.build(
                        weekday = state.weekday,
                        hour = state.hour,
                        comparison = state.weekComparison,
                        speedUnit = preferences.speedUnit,
                    )
                }
                val insights = rememberInsightSentences(tokens)
                DrivingInsightRow(insights = insights)
            }

            SectionCard(
                title = stringResource(R.string.analytics_section_milestone),
                subtitle = stringResource(R.string.analytics_section_milestone_sub),
            ) {
                MilestoneRow(
                    totalDistanceMeters = state.total.totalDistanceMeters,
                    distanceUnit = preferences.distanceUnit,
                )
            }

            SectionCard(
                title = stringResource(R.string.analytics_section_records),
                subtitle = stringResource(R.string.analytics_section_records_sub),
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

@Composable
private fun monthTitle(@androidx.annotation.StringRes patternRes: Int): String {
    val pattern = stringResource(patternRes)
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val cal = Calendar.getInstance()
    val fmt = remember(pattern, locale) { SimpleDateFormat(pattern, locale) }
    return fmt.format(cal.time)
}
