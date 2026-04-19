package com.haruple97.speedometer.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.NeedleRed
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun DigitalSpeedDisplay(
    speedKmh: Float,
    speedUnit: SpeedUnit = SpeedUnit.KMH,
    isOverspeed: Boolean = false,
    scale: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val baseStyle = SpeedometerTextStyle.Data1Style()
    val digitColor by animateColorAsState(
        targetValue = if (isOverspeed) NeedleRed else DigitalWhite,
        label = "overspeedDigitColor",
    )
    val displayValue = speedUnit.fromKmh(speedKmh).toInt()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = displayValue.toString(),
            style = baseStyle.copy(fontSize = baseStyle.fontSize * scale),
            color = digitColor
        )
        Text(
            text = speedUnit.label,
            style = SpeedometerTextStyle.H4RegularStyle().copy(
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
            speedKmh = 88f,
            speedUnit = SpeedUnit.KMH,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, name = "Overspeed")
@Composable
private fun DigitalSpeedDisplayOverspeedPreview() {
    SpeedometerTheme {
        DigitalSpeedDisplay(
            speedKmh = 130f,
            speedUnit = SpeedUnit.KMH,
            isOverspeed = true,
            modifier = Modifier.background(DashboardBlack)
        )
    }
}
