package com.haruple97.speedometer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun DigitalSpeedDisplay(
    speed: Int,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val displayLarge = MaterialTheme.typography.displayLarge
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = speed.toString(),
            style = displayLarge.copy(fontSize = displayLarge.fontSize * scale),
            color = DigitalWhite
        )
        Text(
            text = "km/h",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp * scale
            ),
            color = UnitGray
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun DigitalSpeedDisplayPreview() {
    SpeedometerTheme {
        DigitalSpeedDisplay(
            speed = 88,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}
