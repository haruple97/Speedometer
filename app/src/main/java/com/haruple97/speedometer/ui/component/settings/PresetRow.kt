package com.haruple97.speedometer.ui.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.SpeedPreset
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.SpeedometerTheme

@Composable
fun PresetRow(
    onPresetClick: (SpeedPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(SpeedPreset.entries.toList(), key = { it.name }) { preset ->
            PresetCard(
                preset = preset,
                onClick = { onPresetClick(preset) },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PresetRowPreview() {
    SpeedometerTheme {
        PresetRow(
            onPresetClick = {},
            modifier = Modifier.background(DashboardBlack),
        )
    }
}
