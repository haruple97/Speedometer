package com.haruple97.speedometer.data.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * UMP(User Messaging Platform) 래퍼.
 *
 * - EU/UK/브라질 같은 대상 지역 사용자에겐 GDPR·PDPA 동의 폼을 노출.
 * - 비대상 지역에선 `requestConsentInfoUpdate` 가 NOT_REQUIRED 로 즉시 반환해
 *   UI 노출 없이 진행.
 * - 동의가 성공했든 폼 로드가 실패했든, `canRequestAds()` 가 true 면 광고 요청은
 *   가능(비개인화 광고로 fallback). 그러므로 최대한 관대하게 onReady 를 호출.
 */
class AdConsentManager(private val activity: Activity) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    fun gatherConsent(onReady: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // 동의 정보 갱신 성공 → 필요 시 폼 표시 후 ready 콜
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    // formError 무시하고 가능하면 진행
                    if (consentInformation.canRequestAds()) onReady()
                }
            },
            {
                // 동의 정보 갱신 실패 — 이전 캐시 기준으로라도 canRequestAds true 면 진행
                if (consentInformation.canRequestAds()) onReady()
            },
        )
    }
}
