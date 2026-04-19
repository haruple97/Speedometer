package com.haruple97.speedometer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.R
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.data.trip.SummaryPeriod
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.ui.component.history.SummaryCard
import com.haruple97.speedometer.ui.component.history.TripListDivider
import com.haruple97.speedometer.ui.component.history.TripListItem
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.viewmodel.HistoryViewModel
import com.haruple97.speedometer.viewmodel.SettingsViewModel

@Composable
fun HistoryRoute(
    onNavigateBack: () -> Unit,
    onTripSelected: (Long) -> Unit,
    historyViewModel: HistoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val trips by historyViewModel.trips.collectAsStateWithLifecycle()
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
    val summary by historyViewModel.summary.collectAsStateWithLifecycle()
    val selectedPeriod by historyViewModel.selectedPeriod.collectAsStateWithLifecycle()

    HistoryScreen(
        trips = trips,
        preferences = preferences,
        summary = summary,
        selectedPeriod = selectedPeriod,
        onPeriodChange = historyViewModel::selectPeriod,
        onNavigateBack = onNavigateBack,
        onTripSelected = onTripSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    trips: List<TripEntity>,
    preferences: UserPreferences,
    summary: TripAggregate,
    selectedPeriod: SummaryPeriod,
    onPeriodChange: (SummaryPeriod) -> Unit,
    onNavigateBack: () -> Unit,
    onTripSelected: (Long) -> Unit,
) {
    Scaffold(
        containerColor = DashboardBlack,
        topBar = {
            TopAppBar(
                title = { Text(text = "기록", color = DigitalWhite) },
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
        if (trips.isEmpty()) {
            HistoryEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DashboardBlack)
                    .padding(innerPadding)
                    .padding(32.dp),
            )
        } else {
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
                    TripListDivider()
                }
                items(items = trips, key = { it.id }) { trip ->
                    TripListItem(
                        trip = trip,
                        speedUnit = preferences.speedUnit,
                        onClick = { onTripSelected(trip.id) },
                    )
                    TripListDivider()
                }
            }
        }
    }
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
