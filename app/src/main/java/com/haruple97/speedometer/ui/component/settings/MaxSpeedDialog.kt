package com.haruple97.speedometer.ui.component.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun MaxSpeedDialog(
    currentKmh: Float,
    speedUnit: SpeedUnit,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    val range = when (speedUnit) {
        SpeedUnit.KMH -> 100f..400f
        SpeedUnit.MPH -> 60f..250f
    }
    val stepIncrement = 10
    SliderDialog(
        title = "최대 속도",
        unit = speedUnit.label,
        valueRange = range,
        stepIncrement = stepIncrement,
        currentDisplay = speedUnit.fromKmh(currentKmh),
        onDismiss = onDismiss,
        onConfirm = { display -> onConfirm(speedUnit.toKmh(display)) },
    )
}

@Composable
fun OverspeedThresholdDialog(
    currentKmh: Float,
    speedUnit: SpeedUnit,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    val range = when (speedUnit) {
        SpeedUnit.KMH -> 30f..250f
        SpeedUnit.MPH -> 20f..160f
    }
    val stepIncrement = 5
    SliderDialog(
        title = "과속 경고 임계값",
        unit = speedUnit.label,
        valueRange = range,
        stepIncrement = stepIncrement,
        currentDisplay = speedUnit.fromKmh(currentKmh),
        onDismiss = onDismiss,
        onConfirm = { display -> onConfirm(speedUnit.toKmh(display)) },
    )
}

@Composable
private fun SliderDialog(
    title: String,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    stepIncrement: Int,
    currentDisplay: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    val steps = ((valueRange.endInclusive - valueRange.start).toInt() / stepIncrement) - 1
    var draft by remember { mutableFloatStateOf(currentDisplay.coerceIn(valueRange)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DashboardDarkGray,
        titleContentColor = DigitalWhite,
        textContentColor = DigitalWhite,
        title = {
            Text(
                text = title,
                style = SpeedometerTextStyle.H3Style(),
                color = DigitalWhite,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "${draft.toInt()} $unit",
                    style = SpeedometerTextStyle.Data2Style(),
                    color = GaugeSafe,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                Slider(
                    value = draft,
                    onValueChange = { draft = it },
                    valueRange = valueRange,
                    steps = steps,
                    colors = SliderDefaults.colors(
                        thumbColor = GaugeSafe,
                        activeTrackColor = GaugeSafe,
                        inactiveTrackColor = GaugeTrack,
                        activeTickColor = GaugeSafe,
                        inactiveTickColor = GaugeTrack,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }) {
                Text("확인", color = GaugeSafe)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = UnitGray)
            }
        },
    )
}
