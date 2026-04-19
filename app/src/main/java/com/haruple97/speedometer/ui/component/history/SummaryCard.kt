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
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.SummaryPeriod
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
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
                    Text(value.label)
                }
            }
        }

        val empty = summary.tripCount == 0
        val distanceText = if (empty) "—" else formatDistance(summary.totalDistanceMeters, distanceUnit)
        val durationText = if (empty) "—" else formatDuration(summary.totalDurationMs)
        val maxSpeedText = if (empty) "—" else speedUnit.fromKmh(summary.maxSpeedKmh).roundToInt().toString()
        val tripCountText = if (empty) "—" else summary.tripCount.toString()

        TripStatGrid(
            cards = listOf(
                Triple("총 거리", distanceText, if (empty) "" else distanceUnit.label),
                Triple("주행 시간", durationText, ""),
                Triple("최고 속도", maxSpeedText, if (empty) "" else speedUnit.label),
                Triple("주행 횟수", tripCountText, if (empty) "" else "회"),
            ),
        )
    }
}

private fun formatDistance(meters: Float, unit: DistanceUnit): String {
    val km = meters / 1000f
    val display = unit.fromKm(km)
    return if (display >= 100f) display.roundToInt().toString() else "%.1f".format(display)
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "—"
    val totalMinutes = durationMs / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}
