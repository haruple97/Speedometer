package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.R
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.NeedleGlow
import com.haruple97.speedometer.ui.theme.NeedleRed
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.util.formatDurationMinutes
import com.haruple97.speedometer.util.formatOneDecimal
import com.haruple97.speedometer.util.rememberDateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun PersonalRecordCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    primaryValue: String,
    secondaryLabel: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DashboardBlack)
            .alpha(if (enabled) 1f else 0.4f)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = SpeedometerTextStyle.CaptionRegularStyle(),
                color = UnitGray,
            )
        }
        Text(
            text = primaryValue,
            style = SpeedometerTextStyle.H3Style(),
            color = DigitalWhite,
        )
        Text(
            text = secondaryLabel ?: stringResource(R.string.record_no_data),
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
        )
    }
}

@Composable
fun PersonalRecordGrid(
    topDistance: TripEntity?,
    topMaxSpeed: TripEntity?,
    topDuration: TripEntity?,
    topOverspeed: TripEntity?,
    speedUnit: SpeedUnit,
    distanceUnit: DistanceUnit,
    onTripClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = rememberDateFormat(R.string.format_date_month_day)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PersonalRecordCard(
                icon = Icons.Filled.Route,
                iconTint = GaugeSafe,
                label = stringResource(R.string.record_longest_distance),
                primaryValue = topDistance?.let {
                    "${formatOneDecimal(distanceUnit.fromKm(it.distanceMeters / 1000f))} ${distanceUnit.label}"
                } ?: "—",
                secondaryLabel = topDistance?.let { dateFormat.format(Date(it.startedAt)) },
                enabled = topDistance != null,
                onClick = { topDistance?.let { onTripClick(it.id) } },
                modifier = Modifier.weight(1f),
            )
            PersonalRecordCard(
                icon = Icons.Filled.Speed,
                iconTint = NeedleGlow,
                label = stringResource(R.string.record_top_speed),
                primaryValue = topMaxSpeed?.let {
                    "${speedUnit.fromKmh(it.maxSpeedKmh).roundToInt()} ${speedUnit.label}"
                } ?: "—",
                secondaryLabel = topMaxSpeed?.let { dateFormat.format(Date(it.startedAt)) },
                enabled = topMaxSpeed != null,
                onClick = { topMaxSpeed?.let { onTripClick(it.id) } },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PersonalRecordCard(
                icon = Icons.Filled.Schedule,
                iconTint = GaugeCaution,
                label = stringResource(R.string.record_longest_duration),
                primaryValue = topDuration?.let {
                    val mins = (it.endedAt - it.startedAt) / 60_000
                    formatDurationMinutes(mins)
                } ?: "—",
                secondaryLabel = topDuration?.let { dateFormat.format(Date(it.startedAt)) },
                enabled = topDuration != null,
                onClick = { topDuration?.let { onTripClick(it.id) } },
                modifier = Modifier.weight(1f),
            )
            PersonalRecordCard(
                icon = Icons.Filled.Warning,
                iconTint = NeedleRed,
                label = stringResource(R.string.record_most_overspeed),
                primaryValue = topOverspeed?.let {
                    stringResource(R.string.record_overspeed_count, it.overspeedEventCount)
                } ?: "—",
                secondaryLabel = topOverspeed?.let { dateFormat.format(Date(it.startedAt)) },
                enabled = topOverspeed != null,
                onClick = { topOverspeed?.let { onTripClick(it.id) } },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
