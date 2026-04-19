package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import kotlin.math.roundToInt

/**
 * 누적 마일스톤 카드. 단계: 100 / 500 / 1,000 / 5,000 / 10,000 km.
 * - 현재 총 거리 기준 다음 단계까지 프로그레스 + 남은 거리
 * - 달성한 마일스톤 체크 뱃지 그리드
 */
@Composable
fun MilestoneRow(
    totalDistanceMeters: Float,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val milestonesKm = listOf(100, 500, 1_000, 5_000, 10_000)
    val totalKm = totalDistanceMeters / 1000f
    val currentDisplay = distanceUnit.fromKm(totalKm)

    val nextMilestone = milestonesKm.firstOrNull { it > totalKm }
    val lastAchieved = milestonesKm.lastOrNull { totalKm >= it } ?: 0
    val target = nextMilestone ?: milestonesKm.last()
    val progress = if (nextMilestone == null) 1f
    else ((totalKm - lastAchieved) / (target - lastAchieved).toFloat()).coerceIn(0f, 1f)
    val remainingKm = if (nextMilestone == null) 0f else (target - totalKm).coerceAtLeast(0f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "${formatDistance(currentDisplay)} ${distanceUnit.label}",
                style = SpeedometerTextStyle.Data2Style(),
                color = DigitalWhite,
            )
            if (nextMilestone != null) {
                Text(
                    text = "다음 ${distanceUnit.fromKm(target.toFloat()).roundToInt()} ${distanceUnit.label} 까지 " +
                        "${distanceUnit.fromKm(remainingKm).roundToInt()} ${distanceUnit.label}",
                    style = SpeedometerTextStyle.CaptionRegularStyle(),
                    color = UnitGray,
                )
            } else {
                Text(
                    text = "모든 마일스톤 달성!",
                    style = SpeedometerTextStyle.CaptionStyle(),
                    color = GaugeSafe,
                )
            }
        }

        // 프로그레스 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(GaugeTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(GaugeSafe),
            )
        }

        // 마일스톤 뱃지 그리드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            milestonesKm.forEach { km ->
                val achieved = totalKm >= km
                MilestoneBadge(
                    labelKm = km,
                    achieved = achieved,
                    distanceUnit = distanceUnit,
                )
            }
        }
    }
}

@Composable
private fun MilestoneBadge(labelKm: Int, achieved: Boolean, distanceUnit: DistanceUnit) {
    val bg = if (achieved) GaugeSafe else DashboardBlack
    val fg = if (achieved) DashboardBlack else UnitGray
    val display = distanceUnit.fromKm(labelKm.toFloat()).roundToInt()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = if (achieved) "✓ $display" else "$display",
                style = SpeedometerTextStyle.CaptionStyle(),
                color = fg,
            )
        }
        Text(
            text = distanceUnit.label,
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
        )
    }
}

private fun formatDistance(value: Float): String =
    if (value >= 100f) value.roundToInt().toString() else "%.1f".format(value)
