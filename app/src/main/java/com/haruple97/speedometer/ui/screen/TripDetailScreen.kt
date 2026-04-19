package com.haruple97.speedometer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.ui.component.history.SpeedTimelineChart
import com.haruple97.speedometer.ui.component.history.TripStatGrid
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.viewmodel.SettingsViewModel
import com.haruple97.speedometer.viewmodel.TripDetailUiState
import com.haruple97.speedometer.viewmodel.TripDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TripDetailRoute(
    tripId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TripDetailViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) { viewModel.load(tripId) }

    TripDetailScreen(
        state = state,
        preferences = preferences,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    state: TripDetailUiState,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = DashboardBlack,
        topBar = {
            TopAppBar(
                title = { Text("기록 상세", color = DigitalWhite) },
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
        val trip = state.trip
        if (trip == null) {
            Text(
                text = if (state.isLoading) "" else "기록을 찾을 수 없습니다",
                style = SpeedometerTextStyle.Body1RegularStyle(),
                color = UnitGray,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(24.dp),
            )
            return@Scaffold
        }

        val unit = preferences.speedUnit
        val dateFormat = remember { SimpleDateFormat("M월 d일 (E)", Locale.KOREAN) }
        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.KOREAN) }
        val startedAt = trip.startedAt
        val endedAt = if (trip.endedAt == 0L) startedAt else trip.endedAt
        val durationMin = ((endedAt - startedAt) / 60_000f).roundToInt().coerceAtLeast(0)
        val distanceKm = trip.distanceMeters / 1000f
        val distanceText = if (unit == SpeedUnit.MPH) {
            "%.1f".format(distanceKm * 0.621371f)
        } else {
            "%.1f".format(distanceKm)
        }
        val distanceUnit = if (unit == SpeedUnit.MPH) "mi" else "km"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column {
                Text(
                    text = dateFormat.format(Date(startedAt)),
                    style = SpeedometerTextStyle.H3Style(),
                    color = DigitalWhite,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${timeFormat.format(Date(startedAt))} – ${timeFormat.format(Date(endedAt))}",
                    style = SpeedometerTextStyle.Body1RegularStyle(),
                    color = UnitGray,
                )
            }

            TripStatGrid(
                cards = listOf(
                    Triple("거리", distanceText, distanceUnit),
                    Triple("주행 시간", durationMin.toString(), "분"),
                    Triple(
                        "최고 속도",
                        unit.fromKmh(trip.maxSpeedKmh).roundToInt().toString(),
                        unit.label,
                    ),
                    Triple(
                        "평균 속도",
                        unit.fromKmh(trip.avgSpeedKmh).roundToInt().toString(),
                        unit.label,
                    ),
                ),
            )

            SpeedTimelineChart(
                samples = state.samples,
                maxSpeedKmh = trip.maxSpeedKmh,
                speedUnit = unit,
                overspeedThresholdKmh = if (preferences.overspeedEnabled) preferences.overspeedThreshold else null,
            )
        }
    }
}

