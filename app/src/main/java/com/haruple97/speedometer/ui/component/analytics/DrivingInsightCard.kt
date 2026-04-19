package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray

/** 자연어 인사이트 리스트 — 최대 3줄. 빈 리스트 시 기본 안내 문구. */
@Composable
fun DrivingInsightRow(
    insights: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (insights.isEmpty()) {
            Text(
                text = "더 많은 주행 기록이 쌓이면 맞춤 인사이트를 보여드릴게요.",
                style = SpeedometerTextStyle.Body2RegularStyle(),
                color = UnitGray,
            )
        } else {
            insights.forEach { sentence ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.Top) {
                    Text(
                        text = "• ",
                        style = SpeedometerTextStyle.Body1Style(),
                        color = GaugeSafe,
                    )
                    Text(
                        text = sentence,
                        style = SpeedometerTextStyle.Body1RegularStyle(),
                        color = DigitalWhite,
                    )
                }
            }
        }
    }
}
