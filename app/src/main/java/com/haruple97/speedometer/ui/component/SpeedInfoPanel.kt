package com.haruple97.speedometer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.AccentAmber
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.GpsActive
import com.haruple97.speedometer.ui.theme.GpsInactive
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.TickLight
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun SpeedInfoPanel(
    maxSpeed: Float,
    gpsAccuracy: Float?,
    isGpsActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 최고 속도
        Text(
            text = "MAX  ${maxSpeed.toInt()} km/h",
            style = MaterialTheme.typography.bodySmall,
            color = AccentAmber
        )

        // GPS 정확도
        Text(
            text = if (gpsAccuracy != null) "±${gpsAccuracy.toInt()}m" else "--",
            style = MaterialTheme.typography.bodySmall,
            color = UnitGray
        )

        // GPS 상태 점
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isGpsActive) GpsActive else GpsInactive)
            )
            Text(
                text = "GPS",
                style = MaterialTheme.typography.bodySmall,
                color = TickLight
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SpeedInfoPanelPreview() {
    SpeedometerTheme {
        SpeedInfoPanel(
            maxSpeed = 142f,
            gpsAccuracy = 3f,
            isGpsActive = true,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}
