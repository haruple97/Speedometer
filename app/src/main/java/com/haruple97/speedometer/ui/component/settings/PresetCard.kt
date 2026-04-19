package com.haruple97.speedometer.ui.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedPreset
import com.haruple97.speedometer.ui.theme.AccentAmber
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeCritical
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun PresetCard(
    preset: SpeedPreset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = preset.accentColor()
    Column(
        modifier = modifier
            .width(112.dp)
            .height(128.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DashboardDarkGray)
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = preset.icon(),
            contentDescription = preset.displayName,
            tint = accent,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = preset.displayName,
            style = SpeedometerTextStyle.Body1Style(),
            color = DigitalWhite,
            textAlign = TextAlign.Center,
        )
        Text(
            text = preset.description,
            style = SpeedometerTextStyle.CaptionRegularStyle(),
            color = UnitGray,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

internal fun SpeedPreset.accentColor(): Color = when (this) {
    SpeedPreset.WALK -> GaugeSafe
    SpeedPreset.BIKE -> AccentAmber
    SpeedPreset.DRIVE -> GaugeCaution
    SpeedPreset.EXPRESS -> GaugeCritical
}

internal fun SpeedPreset.icon(): ImageVector = when (this) {
    SpeedPreset.WALK -> Icons.AutoMirrored.Filled.DirectionsRun
    SpeedPreset.BIKE -> Icons.AutoMirrored.Filled.DirectionsBike
    SpeedPreset.DRIVE -> Icons.Filled.DirectionsCar
    SpeedPreset.EXPRESS -> Icons.Filled.Train
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PresetCardPreview() {
    SpeedometerTheme {
        Column(
            modifier = Modifier
                .background(DashboardBlack)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SpeedPreset.entries.forEach { preset ->
                PresetCard(preset = preset, onClick = {})
            }
        }
    }
}
