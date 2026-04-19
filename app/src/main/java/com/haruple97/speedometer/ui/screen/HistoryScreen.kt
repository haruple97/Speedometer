package com.haruple97.speedometer.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.nativead.NativeAd
import com.haruple97.speedometer.R
import com.haruple97.speedometer.SpeedometerApplication
import com.haruple97.speedometer.data.premium.UnlockState
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.data.trip.SummaryPeriod
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.ui.component.history.AnalyticsAccessCard
import com.haruple97.speedometer.ui.component.history.NativeAdCard
import com.haruple97.speedometer.ui.component.history.SummaryCard
import com.haruple97.speedometer.ui.component.history.TripListDivider
import com.haruple97.speedometer.ui.component.history.TripListItem
import com.haruple97.speedometer.ui.component.premium.UnlockPromptDialog
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.viewmodel.HistoryViewModel
import com.haruple97.speedometer.viewmodel.SettingsViewModel

private const val FIRST_AD_AFTER = 1  // 첫 트립 뒤부터 광고 노출
private const val AD_INTERVAL = 5     // 이후 5개 트립마다 광고

@Composable
fun HistoryRoute(
    onTripSelected: (Long) -> Unit,
    onAnalyticsOpen: () -> Unit,
    historyViewModel: HistoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val trips by historyViewModel.trips.collectAsStateWithLifecycle()
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
    val summary by historyViewModel.summary.collectAsStateWithLifecycle()
    val selectedPeriod by historyViewModel.selectedPeriod.collectAsStateWithLifecycle()
    val nativeAds by historyViewModel.nativeAds.collectAsStateWithLifecycle()
    val analyticsUnlockState by historyViewModel.analyticsUnlockState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showUnlockPrompt by remember { mutableStateOf(false) }

    HistoryScreen(
        trips = trips,
        preferences = preferences,
        summary = summary,
        selectedPeriod = selectedPeriod,
        nativeAds = nativeAds,
        analyticsUnlockState = analyticsUnlockState,
        onPeriodChange = historyViewModel::selectPeriod,
        onTripSelected = onTripSelected,
        onAnalyticsCardClick = {
            if (analyticsUnlockState is UnlockState.UnlockedUntil) {
                onAnalyticsOpen()
            } else {
                showUnlockPrompt = true
            }
        },
    )

    if (showUnlockPrompt) {
        UnlockPromptDialog(
            title = "상세 분석 열어볼까요?",
            message = "짧은 광고를 보면 24시간 동안 요일·시간대 주행 패턴 · 속도 구간 분석 · 월간 히트맵 · 개인 기록까지 전부 확인할 수 있어요.",
            confirmText = "광고 보기",
            dismissText = "다음에",
            onConfirm = {
                showUnlockPrompt = false
                val activity = context.findActivity() ?: return@UnlockPromptDialog
                val manager = (activity.application as SpeedometerApplication)
                    .rewardedAdManager
                manager.showForReward(
                    activity = activity,
                    onRewardEarned = {
                        historyViewModel.grantAnalyticsUnlock()
                        onAnalyticsOpen()
                    },
                    onUnavailable = {
                        // 광고 준비 안 됐거나 주행 중 — 사용자엔 조용히 무시, 다음 기회에 자동 제안 가능
                    },
                )
            },
            onDismiss = { showUnlockPrompt = false },
        )
    }
}

/** Compose Context (보통 ContextWrapper) 에서 Activity 를 거슬러 찾음. */
private fun android.content.Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun HistoryScreen(
    trips: List<TripEntity>,
    preferences: UserPreferences,
    summary: TripAggregate,
    selectedPeriod: SummaryPeriod,
    nativeAds: List<NativeAd>,
    analyticsUnlockState: UnlockState,
    onPeriodChange: (SummaryPeriod) -> Unit,
    onTripSelected: (Long) -> Unit,
    onAnalyticsCardClick: () -> Unit,
) {
    Scaffold(
        containerColor = DashboardBlack,
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        if (trips.isEmpty()) {
            HistoryEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DashboardBlack)
                    .padding(innerPadding)
                    .padding(32.dp),
            )
        } else {
            val listItems = buildHistoryListItems(trips, nativeAds)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DashboardBlack)
                    .padding(innerPadding),
            ) {
                item(key = "summary") {
                    SummaryCard(
                        summary = summary,
                        period = selectedPeriod,
                        onPeriodChange = onPeriodChange,
                        speedUnit = preferences.speedUnit,
                        distanceUnit = preferences.distanceUnit,
                    )
                }
                item(key = "analytics_access") {
                    AnalyticsAccessCard(
                        unlockState = analyticsUnlockState,
                        onClick = onAnalyticsCardClick,
                    )
                    TripListDivider()
                }
                items(
                    items = listItems,
                    key = { item ->
                        when (item) {
                            is HistoryListItem.Trip -> "trip_${item.trip.id}"
                            is HistoryListItem.Ad -> "ad_${item.slot}"
                        }
                    },
                    contentType = { item ->
                        when (item) {
                            is HistoryListItem.Trip -> "trip"
                            is HistoryListItem.Ad -> "ad"
                        }
                    },
                ) { item ->
                    when (item) {
                        is HistoryListItem.Trip -> {
                            TripListItem(
                                trip = item.trip,
                                speedUnit = preferences.speedUnit,
                                onClick = { onTripSelected(item.trip.id) },
                            )
                            TripListDivider()
                        }

                        is HistoryListItem.Ad -> {
                            NativeAdCard(ad = item.ad)
                            TripListDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun buildHistoryListItems(
    trips: List<TripEntity>,
    ads: List<NativeAd>,
): List<HistoryListItem> = buildList {
    trips.forEachIndexed { index, trip ->
        add(HistoryListItem.Trip(trip))
        val position = index + 1
        val afterFirst = position - FIRST_AD_AFTER
        if (ads.isNotEmpty() && afterFirst >= 0 && afterFirst % AD_INTERVAL == 0) {
            val slot = afterFirst / AD_INTERVAL
            add(HistoryListItem.Ad(slot = slot, ad = ads[slot % ads.size]))
        }
    }
}

private sealed class HistoryListItem {
    data class Trip(val trip: TripEntity) : HistoryListItem()
    data class Ad(val slot: Int, val ad: NativeAd) : HistoryListItem()
}

@Composable
private fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_history_chart),
                contentDescription = null,
                tint = UnitGray,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "아직 기록이 없습니다",
                style = SpeedometerTextStyle.H3Style(),
                color = DigitalWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "주행이 감지되면 자동으로 기록됩니다",
                style = SpeedometerTextStyle.H4RegularStyle(),
                color = UnitGray,
                textAlign = TextAlign.Center,
            )
        }
    }
}
