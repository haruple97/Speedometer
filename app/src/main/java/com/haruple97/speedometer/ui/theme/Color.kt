package com.haruple97.speedometer.ui.theme

import androidx.compose.ui.graphics.Color

// 배경
val DashboardBlack = Color(0xFF0A0A0A)
val DashboardDarkGray = Color(0xFF1A1A1A)

// 게이지 트랙
val GaugeTrack = Color(0xFF2A2A2A)

// 게이지 구간별 색상
val GaugeSafe = Color(0xFF00E676)       // 0-120 km/h: 에메랄드 그린
val GaugeCaution = Color(0xFFFF9100)    // 120-200 km/h: 페라리 오렌지
val GaugeDanger = Color(0xFFFF1744)     // 200-280 km/h: 레이싱 레드
val GaugeCritical = Color(0xFFD50000)   // 280-350 km/h: 딥 레드

// 바늘
val NeedleRed = Color(0xFFFF1744)
val NeedleGlow = Color(0xFFFF5252)

// 텍스트
val DigitalWhite = Color(0xFFF5F5F5)
val TickLight = Color(0xFFB0B0B0)
val TickMajor = Color(0xFFE0E0E0)
val TickMinor = Color(0xFF4A4A4A)
val UnitGray = Color(0xFF757575)

// 강조 / 상태
val AccentAmber = Color(0xFFFFD600)
val GpsActive = Color(0xFF00E676)
val GpsInactive = Color(0xFFFF1744)
