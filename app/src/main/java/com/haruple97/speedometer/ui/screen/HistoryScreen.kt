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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.haruple97.speedometer.ui.component.history.SwipeDeleteBackground
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
    val rawTrips by historyViewModel.trips.collectAsStateWithLifecycle()
    val visibleTrips by historyViewModel.visibleTrips.collectAsStateWithLifecycle()
    val lastPendingTripId by historyViewModel.lastPendingTripId.collectAsStateWithLifecycle()
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
    val summary by historyViewModel.summary.collectAsStateWithLifecycle()
    val selectedPeriod by historyViewModel.selectedPeriod.collectAsStateWithLifecycle()
    val nativeAds by historyViewModel.nativeAds.collectAsStateWithLifecycle()
    val analyticsUnlockState by historyViewModel.analyticsUnlockState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showUnlockPrompt by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    HistoryScreen(
        visibleTrips = visibleTrips,
        rawTripsEmpty = rawTrips.isEmpty(),
        preferences = preferences,
        summary = summary,
        selectedPeriod = selectedPeriod,
        nativeAds = nativeAds,
        analyticsUnlockState = analyticsUnlockState,
        lastPendingTripId = lastPendingTripId,
        snackbarHostState = snackbarHostState,
        onPeriodChange = historyViewModel::selectPeriod,
        onTripSelected = onTripSelected,
        onSwipeDelete = historyViewModel::requestDeleteTrip,
        onUndoDelete = historyViewModel::undoDelete,
        onSnackbarDismissed = historyViewModel::clearLastPending,
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
            title = stringResource(R.string.analytics_unlock_title),
            message = stringResource(R.string.analytics_unlock_message),
            confirmText = stringResource(R.string.analytics_unlock_confirm),
            dismissText = stringResource(R.string.analytics_unlock_dismiss),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    visibleTrips: List<TripEntity>,
    rawTripsEmpty: Boolean,
    preferences: UserPreferences,
    summary: TripAggregate,
    selectedPeriod: SummaryPeriod,
    nativeAds: List<NativeAd>,
    analyticsUnlockState: UnlockState,
    lastPendingTripId: Long?,
    snackbarHostState: SnackbarHostState,
    onPeriodChange: (SummaryPeriod) -> Unit,
    onTripSelected: (Long) -> Unit,
    onSwipeDelete: (Long) -> Unit,
    onUndoDelete: (Long) -> Unit,
    onSnackbarDismissed: () -> Unit,
    onAnalyticsCardClick: () -> Unit,
) {
    // Snackbar 트리거 — pending trip 이 새로 생기면 한 번 띄우고 결과를 ViewModel 로 회신.
    val deleteDoneMessage = stringResource(R.string.history_delete_done)
    val undoLabel = stringResource(R.string.action_undo)
    LaunchedEffect(lastPendingTripId) {
        val id = lastPendingTripId ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = deleteDoneMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short,
            withDismissAction = false,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> onUndoDelete(id)
            SnackbarResult.Dismissed -> onSnackbarDismissed()
        }
    }

    Scaffold(
        containerColor = DashboardBlack,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        if (rawTripsEmpty) {
            HistoryEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DashboardBlack)
                    .padding(innerPadding)
                    .padding(32.dp),
            )
        } else {
            val listItems = buildHistoryListItems(visibleTrips, nativeAds)
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
                            SwipeDeletableTripRow(
                                trip = item.trip,
                                distanceUnit = preferences.distanceUnit,
                                speedUnit = preferences.speedUnit,
                                onClick = { onTripSelected(item.trip.id) },
                                onSwipeDelete = { onSwipeDelete(item.trip.id) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LazyItemScope.SwipeDeletableTripRow(
    trip: TripEntity,
    speedUnit: com.haruple97.speedometer.data.settings.SpeedUnit,
    distanceUnit: com.haruple97.speedometer.data.settings.DistanceUnit,
    onClick: () -> Unit,
    onSwipeDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeDeleteBackground(state = dismissState) },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        modifier = Modifier.animateItem(),
    ) {
        TripListItem(
            trip = trip,
            distanceUnit = distanceUnit,
            speedUnit = speedUnit,
            onClick = onClick,
        )
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
                text = stringResource(R.string.history_empty_title),
                style = SpeedometerTextStyle.H3Style(),
                color = DigitalWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.history_empty_body),
                style = SpeedometerTextStyle.H4RegularStyle(),
                color = UnitGray,
                textAlign = TextAlign.Center,
            )
        }
    }
}
