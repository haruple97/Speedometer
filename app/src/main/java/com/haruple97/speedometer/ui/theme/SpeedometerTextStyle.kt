package com.haruple97.speedometer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

object SpeedometerTextStyle {

    // 디지털 속도 표시 — 중앙 큰 숫자
    @Composable
    fun Data1Style(): TextStyle = TextStyle(
        fontSize = dpToSp(70.dp),
        fontWeight = FontWeight.Bold,
        letterSpacing = dpToSp(4.dp),
    )

    // 보조 대형 숫자 — 최고속도 등
    @Composable
    fun Data2Style(): TextStyle = TextStyle(
        fontSize = dpToSp(36.dp),
        fontWeight = FontWeight.SemiBold,
        letterSpacing = dpToSp(2.dp),
    )

    @Composable fun H1Style(): TextStyle = heading(24.dp, FontWeight.Bold)
    @Composable fun H1RegularStyle(): TextStyle = heading(24.dp, FontWeight.Normal)

    @Composable fun H2Style(): TextStyle = heading(20.dp, FontWeight.Bold)
    @Composable fun H2RegularStyle(): TextStyle = heading(20.dp, FontWeight.Normal)

    @Composable fun H3Style(): TextStyle = heading(18.dp, FontWeight.Bold)
    @Composable fun H3RegularStyle(): TextStyle = heading(18.dp, FontWeight.Normal)

    @Composable fun H4Style(): TextStyle = heading(16.dp, FontWeight.Bold)
    @Composable fun H4RegularStyle(): TextStyle = heading(16.dp, FontWeight.Normal)

    @Composable fun Body1Style(): TextStyle = body(14.dp, FontWeight.Bold)
    @Composable fun Body1RegularStyle(): TextStyle = body(14.dp, FontWeight.Normal)

    @Composable fun Body2Style(): TextStyle = body(13.dp, FontWeight.Medium)
    @Composable fun Body2RegularStyle(): TextStyle = body(13.dp, FontWeight.Normal)

    @Composable fun CaptionStyle(): TextStyle = body(12.dp, FontWeight.Bold)
    @Composable fun CaptionRegularStyle(): TextStyle = body(12.dp, FontWeight.Normal)

    @Composable
    private fun heading(size: Dp, weight: FontWeight) = TextStyle(
        fontSize = dpToSp(size),
        fontWeight = weight,
    )

    @Composable
    private fun body(size: Dp, weight: FontWeight) = TextStyle(
        fontSize = dpToSp(size),
        fontWeight = weight,
        letterSpacing = dpToSp(0.4.dp),
    )

    @Composable
    fun dpToSp(dp: Dp): TextUnit = with(LocalDensity.current) { dp.toSp() }
}
