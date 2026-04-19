package com.haruple97.speedometer.data.premium

/**
 * Rewarded Interstitial 광고 보상으로 언락되는 프리미엄 기능 키.
 *
 * - 시간 기반(24h 등): [AdvancedAnalytics], [CustomGaugeTheme]
 * - 카운트 기반(N회 사용권): [TripShareImage]
 */
enum class PremiumFeature(val storageKey: String) {
    AdvancedAnalytics("advanced_analytics"),
    TripShareImage("trip_share_image"),
    CustomGaugeTheme("custom_gauge_theme"),
}
