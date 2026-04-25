package com.haruple97.speedometer.ui.component.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.R
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.util.formatOneDecimal
import com.haruple97.speedometer.util.rememberDateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun TripListItem(
    trip: TripEntity,
    speedUnit: SpeedUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = rememberDateFormat(R.string.format_date_month_day_short)
    val timeFormat = rememberDateFormat(R.string.format_time_hm)

    val startMs = trip.startedAt
    val endMs = if (trip.endedAt == 0L) startMs else trip.endedAt
    val durationMin = ((endMs - startMs) / 60_000f).roundToInt().coerceAtLeast(0)
    val distanceKm = trip.distanceMeters / 1000f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DashboardDarkGray)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateFormat.format(Date(startMs)),
                style = SpeedometerTextStyle.Body1Style(),
                color = DigitalWhite,
            )
            Text(
                text = stringResource(
                    R.string.format_time_range,
                    timeFormat.format(Date(startMs)),
                    timeFormat.format(Date(endMs)),
                ),
                style = SpeedometerTextStyle.CaptionRegularStyle(),
                color = UnitGray,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Metric(
                label = stringResource(R.string.history_item_metric_distance),
                value = "${formatOneDecimal(distanceKm)} km",
            )
            Metric(
                label = stringResource(R.string.history_item_metric_time),
                value = stringResource(R.string.duration_m, durationMin),
            )
            Metric(
                label = stringResource(R.string.history_item_metric_top),
                value = "${speedUnit.fromKmh(trip.maxSpeedKmh).roundToInt()} ${speedUnit.label}",
            )
        }
    }
}

@Composable
fun TripListDivider() {
    HorizontalDivider(
        modifier = Modifier.height(1.dp),
        color = GaugeTrack,
    )
}

@Composable
private fun Metric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
        )
        Text(
            text = value,
            style = SpeedometerTextStyle.Body1Style(),
            color = DigitalWhite,
        )
    }
}
