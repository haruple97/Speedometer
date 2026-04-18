package com.haruple97.speedometer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun DigitalSpeedDisplay(
    speed: Int,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val data1 = SpeedometerTextStyle.Data1Style()
    val h4 = SpeedometerTextStyle.H4RegularStyle()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = speed.toString(),
            style = data1.copy(fontSize = data1.fontSize * scale),
            color = DigitalWhite
        )
        Text(
            text = "km/h",
            style = h4.copy(fontSize = h4.fontSize * scale),
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
