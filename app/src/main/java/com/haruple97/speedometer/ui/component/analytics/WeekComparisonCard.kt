package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.ComparisonPair
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import kotlin.math.roundToInt

/**
 * 이번 주 vs 지난주 비교 — 거리/시간/주행횟수 3지표 숫자 + % 변화.
 */
@Composable
fun WeekComparisonRow(
    comparison: ComparisonPair,
    speedUnit: SpeedUnit,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val current = comparison.current
    val distanceDisplay = distanceUnit.fromKm(current.totalDistanceMeters / 1000f)
    val minutes = current.totalDurationMs / 60_000

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ComparisonItem(
            label = "거리",
            primary = "${formatDistance(distanceDisplay)} ${distanceUnit.label}",
            percentChange = comparison.distancePercentChange(),
        )
        ComparisonItem(
            label = "시간",
            primary = formatMinutes(minutes),
            percentChange = comparison.durationPercentChange(),
        )
        ComparisonItem(
            label = "주행",
            primary = "${current.tripCount}회",
            percentChange = comparison.tripCountPercentChange(),
        )
    }
}

@Composable
private fun ComparisonItem(
    label: String,
    primary: String,
    percentChange: Float?,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
        )
        Text(
            text = primary,
            style = SpeedometerTextStyle.Body1Style(),
            color = DigitalWhite,
        )
        if (percentChange != null) {
            Text(
                text = formatPercent(percentChange),
                style = SpeedometerTextStyle.CaptionStyle(),
                color = changeColor(percentChange),
            )
        } else {
            Text(
                text = "— ",
                style = SpeedometerTextStyle.CaptionRegularStyle(),
                color = UnitGray,
            )
        }
    }
}

private fun formatDistance(value: Float): String =
    if (value >= 100f) value.roundToInt().toString() else "%.1f".format(value)

private fun formatMinutes(m: Long): String {
    if (m <= 0) return "0분"
    val h = m / 60
    val rem = m % 60
    return if (h > 0) "${h}시간 ${rem}분" else "${rem}분"
}

private fun formatPercent(value: Float): String {
    val arrow = when {
        value > 0.5f -> "▲"
        value < -0.5f -> "▼"
        else -> "="
    }
    return "$arrow ${value.roundToInt()}%"
}

private fun changeColor(value: Float): Color = when {
    value > 0.5f -> GaugeCaution
    value < -0.5f -> GaugeSafe
    else -> UnitGray
}
