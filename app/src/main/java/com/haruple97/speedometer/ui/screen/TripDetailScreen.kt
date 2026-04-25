package com.haruple97.speedometer.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haruple97.speedometer.R
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.settings.UserPreferences
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.ui.component.history.SpeedTimelineChart
import com.haruple97.speedometer.ui.component.history.TripStatGrid
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.NeedleRed
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.util.formatOneDecimal
import com.haruple97.speedometer.util.rememberDateFormat
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
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val sharePattern = stringResource(R.string.format_date_month_day)
    val shareTemplate = stringResource(R.string.trip_detail_share_template)
    val shareChooserTitle = stringResource(R.string.trip_detail_share_chooser)

    LaunchedEffect(tripId) { viewModel.load(tripId) }

    TripDetailScreen(
        state = state,
        preferences = preferences,
        onNavigateBack = onNavigateBack,
        onDelete = {
            viewModel.delete(tripId, onDeleted = onNavigateBack)
        },
        onShare = { trip ->
            val text = buildShareText(trip, preferences.speedUnit, sharePattern, locale, shareTemplate)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, shareChooserTitle))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    state: TripDetailUiState,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit,
    onShare: (TripEntity) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val trip = state.trip

    Scaffold(
        containerColor = DashboardBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trip_detail_title), color = DigitalWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
                            tint = DigitalWhite,
                        )
                    }
                },
                actions = {
                    if (trip != null) {
                        IconButton(onClick = { onShare(trip) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.action_share),
                                tint = DigitalWhite,
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = DigitalWhite,
                            )
                        }
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
        if (trip == null) {
            Text(
                text = if (state.isLoading) "" else stringResource(R.string.trip_detail_not_found),
                style = SpeedometerTextStyle.Body1RegularStyle(),
                color = UnitGray,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(24.dp),
            )
            return@Scaffold
        }

        val unit = preferences.speedUnit
        val dateFormat = rememberDateFormat(R.string.format_date_month_day_weekday)
        val timeFormat = rememberDateFormat(R.string.format_time_hm)
        val startedAt = trip.startedAt
        val endedAt = if (trip.endedAt == 0L) startedAt else trip.endedAt
        val durationMin = ((endedAt - startedAt) / 60_000f).roundToInt().coerceAtLeast(0)
        val distanceKm = trip.distanceMeters / 1000f
        val distanceDisplay = if (unit == SpeedUnit.MPH) distanceKm * 0.621371f else distanceKm
        val distanceText = formatOneDecimal(distanceDisplay)
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
                    text = stringResource(
                        R.string.format_time_range,
                        timeFormat.format(Date(startedAt)),
                        timeFormat.format(Date(endedAt)),
                    ),
                    style = SpeedometerTextStyle.Body1RegularStyle(),
                    color = UnitGray,
                )
            }

            TripStatGrid(
                cards = listOf(
                    Triple(stringResource(R.string.trip_detail_metric_distance), distanceText, distanceUnit),
                    Triple(
                        stringResource(R.string.trip_detail_metric_duration),
                        durationMin.toString(),
                        stringResource(R.string.unit_minute_short),
                    ),
                    Triple(
                        stringResource(R.string.trip_detail_metric_max_speed),
                        unit.fromKmh(trip.maxSpeedKmh).roundToInt().toString(),
                        unit.label,
                    ),
                    Triple(
                        stringResource(R.string.trip_detail_metric_avg_speed),
                        unit.fromKmh(trip.avgSpeedKmh).roundToInt().toString(),
                        unit.label,
                    ),
                ),
            )

            SpeedTimelineChart(
                samples = state.samples,
                maxSpeedKmh = trip.maxSpeedKmh,
                speedUnit = unit,
                overspeedThresholdKmh = trip.overspeedThresholdKmh.takeIf { it > 0f },
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = DashboardDarkGray,
            titleContentColor = DigitalWhite,
            textContentColor = DigitalWhite,
            title = {
                Text(
                    text = stringResource(R.string.trip_detail_delete_title),
                    style = SpeedometerTextStyle.H3Style(),
                    color = DigitalWhite,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.trip_detail_delete_body),
                    style = SpeedometerTextStyle.Body1RegularStyle(),
                    color = UnitGray,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.action_delete), color = NeedleRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = GaugeSafe)
                }
            },
        )
    }
}

private fun buildShareText(
    trip: TripEntity,
    unit: SpeedUnit,
    datePattern: String,
    locale: Locale,
    template: String,
): String {
    val dateFormat = SimpleDateFormat(datePattern, locale)
    val startedAt = trip.startedAt
    val endedAt = if (trip.endedAt == 0L) startedAt else trip.endedAt
    val durationMin = ((endedAt - startedAt) / 60_000f).roundToInt().coerceAtLeast(0)
    val durationFormatted = if (durationMin >= 60) {
        "${durationMin / 60}h ${durationMin % 60}m"
    } else {
        "${durationMin}m"
    }
    val distanceKm = trip.distanceMeters / 1000f
    val distanceDisplay = if (unit == SpeedUnit.MPH) distanceKm * 0.621371f else distanceKm
    val distanceLabel = if (unit == SpeedUnit.MPH) "mi" else "km"
    return template.format(
        /* %1$s date */ dateFormat.format(Date(startedAt)),
        /* %2$s distance */ String.format(locale, "%.1f", distanceDisplay),
        /* %3$s distanceUnit */ distanceLabel,
        /* %4$s duration */ durationFormatted,
        /* %5$s top */ unit.fromKmh(trip.maxSpeedKmh).roundToInt().toString(),
        /* %6$s topUnit */ unit.label,
        /* %7$s avg */ unit.fromKmh(trip.avgSpeedKmh).roundToInt().toString(),
        /* %8$s avgUnit */ unit.label,
    )
}
