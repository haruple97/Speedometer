package com.haruple97.speedometer.ui.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedPreset
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun PresetConfirmDialog(
    preset: SpeedPreset,
    speedUnit: SpeedUnit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val accent = preset.accentColor()
    val unitLabel = speedUnit.label
    val maxDisplay = speedUnit.fromKmh(preset.maxSpeedKmh).toInt()
    val thresholdDisplay = speedUnit.fromKmh(preset.overspeedThresholdKmh).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DashboardDarkGray,
        titleContentColor = DigitalWhite,
        textContentColor = DigitalWhite,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = preset.icon(),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = "${preset.displayName} 프리셋",
                    style = SpeedometerTextStyle.H3Style(),
                    color = DigitalWhite,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "다음과 같이 설정이 변경됩니다.",
                    style = SpeedometerTextStyle.Body1RegularStyle(),
                    color = UnitGray,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GaugeTrack),
                )

                ChangeRow(label = "최대 속도", value = "$maxDisplay $unitLabel")
                ChangeRow(
                    label = "과속 경고",
                    value = if (preset.overspeedEnabled) "켜짐 · $thresholdDisplay $unitLabel" else "끔",
                )
                ChangeRow(label = "HUD 모드", value = if (preset.hudMode) "켜짐" else "끔")
                ChangeRow(
                    label = "화면 켜짐 유지",
                    value = if (preset.keepScreenOn) "켜짐" else "끔",
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("적용", color = accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = UnitGray)
            }
        },
    )
}

@Composable
private fun ChangeRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = SpeedometerTextStyle.Body1RegularStyle(),
            color = UnitGray,
        )
        Text(
            text = value,
            style = SpeedometerTextStyle.Body1Style(),
            color = DigitalWhite,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PresetConfirmDialogPreview() {
    SpeedometerTheme {
        Box(modifier = Modifier.background(DashboardBlack)) {
            PresetConfirmDialog(
                preset = SpeedPreset.DRIVE,
                speedUnit = SpeedUnit.KMH,
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}
