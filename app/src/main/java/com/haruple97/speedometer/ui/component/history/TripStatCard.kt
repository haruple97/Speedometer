package com.haruple97.speedometer.ui.component.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun TripStatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DashboardDarkGray)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = SpeedometerTextStyle.Data2Style(),
                color = DigitalWhite,
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    style = SpeedometerTextStyle.H4RegularStyle(),
                    color = UnitGray,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Composable
fun TripStatGrid(
    cards: List<Triple<String, String, String>>,
    modifier: Modifier = Modifier,
) {
    // 2 x N/2 그리드. 내용이 정확히 4개라고 가정.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        cards.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { (label, value, unit) ->
                    TripStatCard(
                        label = label,
                        value = value,
                        unit = unit,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    // 공백 유지 (홀수 개 대비)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
