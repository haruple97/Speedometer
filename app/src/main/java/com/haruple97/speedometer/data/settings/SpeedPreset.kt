package com.haruple97.speedometer.data.settings

enum class SpeedPreset(
    val displayName: String,
    val description: String,
    val maxSpeedKmh: Float,
    val overspeedEnabled: Boolean,
    val overspeedThresholdKmh: Float,
    val hudMode: Boolean,
    val keepScreenOn: Boolean,
) {
    WALK(
        displayName = "도보",
        description = "가벼운 산책·러닝",
        maxSpeedKmh = 20f,
        overspeedEnabled = false,
        overspeedThresholdKmh = 15f,
        hudMode = false,
        keepScreenOn = true,
    ),
    BIKE(
        displayName = "자전거",
        description = "라이딩·로드바이크",
        maxSpeedKmh = 60f,
        overspeedEnabled = false,
        overspeedThresholdKmh = 40f,
        hudMode = false,
        keepScreenOn = true,
    ),
    DRIVE(
        displayName = "드라이브",
        description = "승용차 주행·과속 경고",
        maxSpeedKmh = 200f,
        overspeedEnabled = true,
        overspeedThresholdKmh = 110f,
        hudMode = true,
        keepScreenOn = true,
    ),
    EXPRESS(
        displayName = "고속주행",
        description = "고속열차·모터스포츠",
        maxSpeedKmh = 350f,
        overspeedEnabled = false,
        overspeedThresholdKmh = 300f,
        hudMode = false,
        keepScreenOn = true,
    ),
}
