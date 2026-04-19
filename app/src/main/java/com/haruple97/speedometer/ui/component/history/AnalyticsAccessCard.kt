package com.haruple97.speedometer.ui.component.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.premium.UnlockState
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray

/**
 * 기록 탭 상단에 들어가는 "상세 분석" 접근 카드.
 * - Locked: 자물쇠 + "광고 보고 24시간 열기" CTA
 * - UnlockedUntil: "상세 분석 열기" + 남은 시간 표시
 */
@Composable
fun AnalyticsAccessCard(
    unlockState: UnlockState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DashboardDarkGray)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = "📊", style = SpeedometerTextStyle.Body1Style())
                Text(
                    text = "상세 분석",
                    style = SpeedometerTextStyle.Body1Style(),
                    color = DigitalWhite,
                )
            }
            Text(
                text = subtitleFor(unlockState),
                style = SpeedometerTextStyle.CaptionRegularStyle(),
                color = if (unlockState is UnlockState.UnlockedUntil) GaugeSafe else UnitGray,
            )
        }

        Box(contentAlignment = Alignment.CenterEnd) {
            if (unlockState is UnlockState.Locked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = UnitGray,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = GaugeSafe,
                )
            }
        }
    }
}

private fun subtitleFor(state: UnlockState): String = when (state) {
    is UnlockState.Locked -> "광고 시청하고 24시간 동안 상세 인사이트 열람"
    is UnlockState.UnlockedUntil -> {
        val remainingMs = state.untilMs - System.currentTimeMillis()
        val hours = remainingMs / 3_600_000
        val minutes = (remainingMs % 3_600_000) / 60_000
        "열람 중 · 남은 시간 ${hours}시간 ${minutes}분"
    }
    is UnlockState.UsesRemaining -> "잔여 ${state.count}회"
}
