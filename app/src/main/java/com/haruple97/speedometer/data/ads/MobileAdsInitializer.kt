package com.haruple97.speedometer.data.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MobileAds SDK 를 앱 전역에서 1회만 초기화하는 싱글턴.
 *
 * UMP 동의 플로우가 `canRequestAds()` 를 true 로 돌린 뒤에 호출해야 한다.
 * 초기화 상태는 [initialized] 로 Compose 에 노출 — AdBanner 등 UI 쪽에서
 * 준비 여부를 구독해 렌더링 타이밍을 조절한다.
 */
object MobileAdsInitializer {

    private val _initialized = MutableStateFlow(false)
    val initialized: StateFlow<Boolean> = _initialized.asStateFlow()

    fun initialize(context: Context) {
        if (_initialized.value) return
        MobileAds.initialize(context.applicationContext) {
            _initialized.value = true
        }
    }
}
