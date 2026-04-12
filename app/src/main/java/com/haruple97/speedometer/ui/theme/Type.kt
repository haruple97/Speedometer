package com.haruple97.speedometer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DashboardFontFamily = FontFamily.Default

val Typography = Typography(
    // 디지털 속도 표시 (중앙 큰 숫자)
    displayLarge = TextStyle(
        fontFamily = DashboardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,
        lineHeight = 104.sp,
        letterSpacing = 4.sp
    ),
    // 보조 속도 표시 (최고속도 등)
    displayMedium = TextStyle(
        fontFamily = DashboardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 2.sp
    ),
    // "km/h" 단위 텍스트
    titleMedium = TextStyle(
        fontFamily = DashboardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 2.sp
    ),
    // 게이지 눈금 숫자
    labelMedium = TextStyle(
        fontFamily = DashboardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    // 보조 정보 텍스트 (GPS 정확도 등)
    bodySmall = TextStyle(
        fontFamily = DashboardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // 일반 본문
    bodyLarge = TextStyle(
        fontFamily = DashboardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
