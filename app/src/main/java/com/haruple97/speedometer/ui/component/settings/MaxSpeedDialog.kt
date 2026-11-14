package com.haruple97.speedometer.ui.component.settings

import kotlin.math.roundToInt
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.R
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
        SpeedUnit.KMH -> 10f..400f
        SpeedUnit.MPH -> 5f..250f
    }
    val stepIncrement = 5
    SliderDialog(
        title = stringResource(R.string.dialog_max_speed_title),
        unit = speedUnit.label,
        valueRange = range,
        stepIncrement = stepIncrement,
        currentDisplay = speedUnit.fromKmh(currentKmh),
        onDismiss = onDismiss,
        onConfirm = { display ->
            onConfirm(if (display == speedUnit.fromKmh(currentKmh)) currentKmh else speedUnit.toKmh(display))
        },
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
        SpeedUnit.KMH -> 5f..400f
        SpeedUnit.MPH -> 5f..250f
    }
    val stepIncrement = 5
    SliderDialog(
        title = stringResource(R.string.dialog_overspeed_threshold_title),
        unit = speedUnit.label,
        valueRange = range,
        stepIncrement = stepIncrement,
        currentDisplay = speedUnit.fromKmh(currentKmh),
        onDismiss = onDismiss,
        onConfirm = { display ->
            onConfirm(if (display == speedUnit.fromKmh(currentKmh)) currentKmh else speedUnit.toKmh(display))
        },
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
    // 단위 전환 직후에도 저장된 값을 편집창의 경계로 잘라내지 않는다.
    val editableRange = minOf(valueRange.start, currentDisplay)..maxOf(valueRange.endInclusive, currentDisplay)
    val steps = ((editableRange.endInclusive - editableRange.start).toInt() / stepIncrement - 1).coerceAtLeast(0)
    var draft by remember(currentDisplay) { mutableFloatStateOf(currentDisplay) }

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
                    text = stringResource(R.string.dialog_value_with_unit, draft.roundToInt(), unit),
                    style = SpeedometerTextStyle.Data2Style(),
                    color = GaugeSafe,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                Slider(
                    value = draft,
                    onValueChange = { draft = it },
                    valueRange = editableRange,
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
                Text(stringResource(R.string.action_ok), color = GaugeSafe)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = UnitGray)
            }
        },
    )
}
