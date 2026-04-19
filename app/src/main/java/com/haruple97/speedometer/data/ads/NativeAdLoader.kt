package com.haruple97.speedometer.data.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Native Advanced 광고를 여러 개 한 번에 로드해 Flow 로 노출.
 *
 * - `AdLoader.loadAds(request, count)` 로 일괄 요청 (서버가 성공한 만큼만 돌려줌).
 * - 로드 완료된 개수 만큼 점진적으로 [ads] StateFlow 에 반영.
 * - 호스트(VM)에서 수명 주기 끝날 때 [destroy] 호출 → 내부 NativeAd 들 모두 destroy.
 */
class NativeAdLoader(
    private val context: Context,
    private val adUnitId: String,
    private val count: Int = 5,
) {

    private val _ads = MutableStateFlow<List<NativeAd>>(emptyList())
    val ads: StateFlow<List<NativeAd>> = _ads.asStateFlow()

    private var destroyed: Boolean = false

    fun load() {
        if (destroyed) return
        if (_ads.value.isNotEmpty()) return  // 이미 캐시됨
        val loaded = mutableListOf<NativeAd>()

        val loader = AdLoader.Builder(context.applicationContext, adUnitId)
            .forNativeAd { nativeAd ->
                if (destroyed) {
                    nativeAd.destroy()
                    return@forNativeAd
                }
                loaded += nativeAd
                _ads.value = loaded.toList()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // 로드 실패는 무시. 이미 받은 광고가 있으면 그대로 쓰고,
                    // 아예 없으면 리스트는 빈 상태 → 광고 슬롯은 자연스럽게 스킵된다.
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        loader.loadAds(AdRequest.Builder().build(), count)
    }

    fun destroy() {
        destroyed = true
        _ads.value.forEach { it.destroy() }
        _ads.value = emptyList()
    }
}
