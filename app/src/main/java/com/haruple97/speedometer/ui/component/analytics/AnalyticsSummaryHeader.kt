package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.R
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.util.formatDurationMinutes
import com.haruple97.speedometer.util.formatOneDecimal
import kotlin.math.roundToInt

/**
 * 분석 화면 최상단 요약 카드.
 *
 * - 큰 숫자: 이번 달 누적 거리
 * - 보조 지표 3개: 주행 횟수 · 평균 속도 · 총 시간
 * - 하단 얇은 텍스트: 누적 총 거리/시간
 */
@Composable
fun AnalyticsSummaryHeader(
    thisMonth: TripAggregate,
    total: TripAggregate,
    speedUnit: SpeedUnit,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val monthKm = thisMonth.totalDistanceMeters / 1000f
    val monthDisplayDistance = distanceUnit.fromKm(monthKm)
    val avgSpeedValue = if (thisMonth.tripCount > 0) {
        speedUnit.fromKmh(thisMonth.maxSpeedKmh).roundToInt()
    } else 0
    val monthMinutes = thisMonth.totalDurationMs / 60_000
    val monthHoursLabel = formatDurationMinutes(monthMinutes)

    val totalKm = total.totalDistanceMeters / 1000f
    val totalDisplayDistance = distanceUnit.fromKm(totalKm)
    val totalHours = (total.totalDurationMs / 3_600_000L).toInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DashboardDarkGray)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.analytics_summary_this_month),
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text(
                text = formatDistanceDisplay(monthDisplayDistance),
                style = SpeedometerTextStyle.Data1Style().copy(color = DigitalWhite),
                color = DigitalWhite,
            )
            Text(
                text = " ${distanceUnit.label}",
                style = SpeedometerTextStyle.H3Style(),
                color = UnitGray,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MetricColumn(
                label = stringResource(R.string.analytics_summary_metric_count),
                value = stringResource(R.string.compare_count_value, thisMonth.tripCount),
            )
            MetricColumn(
                label = stringResource(R.string.analytics_summary_metric_duration),
                value = monthHoursLabel,
            )
            MetricColumn(
                label = stringResource(R.string.analytics_summary_metric_top_this_month),
                value = if (thisMonth.maxSpeedKmh > 0f) "$avgSpeedValue ${speedUnit.label}" else "—",
            )
        }

        Text(
            text = stringResource(
                R.string.analytics_summary_total,
                formatDistanceDisplay(totalDisplayDistance),
                distanceUnit.label,
                totalHours,
            ),
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = GaugeSafe,
        )
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
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

@Composable
private fun formatDistanceDisplay(value: Float): String =
    if (value >= 100f) value.roundToInt().toString() else formatOneDecimal(value)
