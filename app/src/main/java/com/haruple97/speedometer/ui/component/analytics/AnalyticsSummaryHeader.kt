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
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
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
        speedUnit.fromKmh(thisMonth.maxSpeedKmh).roundToInt()  // 요약엔 최고값으로 단순화
    } else 0
    val monthMinutes = thisMonth.totalDurationMs / 60_000
    val monthHours = monthMinutes / 60
    val monthHoursLabel = if (monthHours > 0) "${monthHours}h ${monthMinutes % 60}m" else "${monthMinutes}분"

    val totalKm = total.totalDistanceMeters / 1000f
    val totalDisplayDistance = distanceUnit.fromKm(totalKm)
    val totalHours = total.totalDurationMs / 3_600_000L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DashboardDarkGray)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "이번 달",
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text(
                text = formatDistance(monthDisplayDistance),
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
            MetricColumn("주행 횟수", "${thisMonth.tripCount}회")
            MetricColumn("주행 시간", monthHoursLabel)
            MetricColumn("이번 달 최고", if (thisMonth.maxSpeedKmh > 0f) "$avgSpeedValue ${speedUnit.label}" else "—")
        }

        Text(
            text = "누적 총 ${formatDistance(totalDisplayDistance)} ${distanceUnit.label} · ${totalHours}시간 운전",
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

private fun formatDistance(value: Float): String =
    if (value >= 100f) value.roundToInt().toString() else "%.1f".format(value)
