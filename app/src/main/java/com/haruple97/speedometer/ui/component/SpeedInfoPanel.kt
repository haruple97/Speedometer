package com.haruple97.speedometer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.ui.theme.AccentAmber
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.GaugeDanger
import com.haruple97.speedometer.ui.theme.GpsActive
import com.haruple97.speedometer.ui.theme.GpsInactive
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.TickLight
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun SpeedInfoPanel(
    maxSpeedKmh: Float,
    gpsAccuracy: Float?,
    isGpsActive: Boolean,
    isRecording: Boolean = false,
    speedUnit: SpeedUnit = SpeedUnit.KMH,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 기록 중 인디케이터 — 자동 기록이 Running/Stopping 일 때 ● REC 표시.
        // 항상 자리를 예약해서 점멸 시 다른 항목 위치가 흔들리지 않도록 alpha 로 토글.
        Row(
            modifier = Modifier.alpha(if (isRecording) 1f else 0f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GaugeDanger)
            )
            Text(
                text = "REC",
                style = SpeedometerTextStyle.CaptionRegularStyle(),
                color = GaugeDanger
            )
        }

        // 최고 속도
        Text(
            text = "MAX  ${speedUnit.fromKmh(maxSpeedKmh).toInt()} ${speedUnit.label}",
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = AccentAmber
        )

        // GPS 정확도
        Text(
            text = if (gpsAccuracy != null) "±${gpsAccuracy.toInt()}m" else "--",
            style = SpeedometerTextStyle.CaptionRegularStyle(),
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
                style = SpeedometerTextStyle.CaptionRegularStyle(),
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
            maxSpeedKmh = 142f,
            gpsAccuracy = 3f,
            isGpsActive = true,
            isRecording = true,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}
