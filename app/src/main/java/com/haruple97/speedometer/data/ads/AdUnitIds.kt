package com.haruple97.speedometer.data.ads

import com.haruple97.speedometer.BuildConfig

/**
 * 광고 단위 ID 중앙 관리.
 *
 * Debug 빌드에선 Google 공식 테스트 ID 를 사용해 **실 계정에 자기 광고 찍어 계정 정지**
 * 리스크를 피한다. Release 빌드에서만 AdMob 콘솔에서 발급받은 실 유닛 ID 를 사용.
 */
object AdUnitIds {
    /** 계기판 상단 Adaptive Banner (Main_Top_Banner). */
    val bannerMain: String =
        if (BuildConfig.DEBUG) TEST_BANNER else "ca-app-pub-3898269947993948/2710891025"

    /** 기록 리스트 사이 Native Advanced (record_list). */
    val nativeRecordList: String =
        if (BuildConfig.DEBUG) TEST_NATIVE else "ca-app-pub-3898269947993948/5240838136"

    /** 앱 warm resume 시 전체화면 App Open (app_opening). */
    val appOpen: String =
        if (BuildConfig.DEBUG) TEST_APP_OPEN else "ca-app-pub-3898269947993948/7720721302"

    // https://developers.google.com/admob/android/test-ads
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
}
