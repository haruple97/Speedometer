package com.haruple97.speedometer.ui.component.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.R
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.SummaryPeriod
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.util.formatDuration
import com.haruple97.speedometer.util.formatOneDecimal
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCard(
    summary: TripAggregate,
    period: SummaryPeriod,
    onPeriodChange: (SummaryPeriod) -> Unit,
    speedUnit: SpeedUnit,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val options = SummaryPeriod.entries

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = value == period,
                    onClick = { onPeriodChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size,
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = GaugeSafe,
                        activeContentColor = DashboardBlack,
                        inactiveContainerColor = DashboardBlack,
                        inactiveContentColor = DigitalWhite,
                        activeBorderColor = GaugeSafe,
                        inactiveBorderColor = GaugeTrack,
                    ),
                ) {
                    Text(stringResource(value.labelRes))
                }
            }
        }

        val empty = summary.tripCount == 0
        val distanceText = if (empty) "—" else formatDistanceDisplay(summary.totalDistanceMeters, distanceUnit)
        val durationText = if (empty) "—" else formatDuration(summary.totalDurationMs)
        val maxSpeedText = if (empty) "—" else speedUnit.fromKmh(summary.maxSpeedKmh).roundToInt().toString()
        val tripCountText = if (empty) "—" else summary.tripCount.toString()

        TripStatGrid(
            cards = listOf(
                Triple(stringResource(R.string.history_summary_distance), distanceText, if (empty) "" else distanceUnit.label),
                Triple(stringResource(R.string.history_summary_duration), durationText, ""),
                Triple(stringResource(R.string.history_summary_max_speed), maxSpeedText, if (empty) "" else speedUnit.label),
                Triple(stringResource(R.string.history_summary_count), tripCountText, if (empty) "" else stringResource(R.string.history_summary_count_unit)),
            ),
        )
    }
}

@Composable
private fun formatDistanceDisplay(meters: Float, unit: DistanceUnit): String {
    val km = meters / 1000f
    val display = unit.fromKm(km)
    return if (display >= 100f) display.roundToInt().toString() else formatOneDecimal(display)
}
