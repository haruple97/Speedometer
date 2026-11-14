package com.haruple97.speedometer.data.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** UMP가 제공하는 현재 동의 상태로 광고 요청과 개인정보 옵션 진입점을 결정한다. */
class AdConsentManager(private val activity: Activity) {
    private val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

    companion object {
        private val _privacyOptionsRequired = MutableStateFlow(false)
        val privacyOptionsRequired = _privacyOptionsRequired.asStateFlow()
        var isFormShowing = false
            private set
    }

    private fun refreshPrivacyOptions() {
        _privacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun gatherConsent(onReady: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(activity, params, {
            refreshPrivacyOptions()
            isFormShowing = true
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                isFormShowing = false
                refreshPrivacyOptions()
                if (consentInformation.canRequestAds()) onReady()
            }
        }, {
            refreshPrivacyOptions()
            // 오류가 나면 SDK가 보유한 이전 세션의 동의 상태로 판단한다.
            if (consentInformation.canRequestAds()) onReady()
        })
    }

    fun showPrivacyOptions(onDismissed: (FormError?) -> Unit) {
        if (isFormShowing) return
        isFormShowing = true
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            isFormShowing = false
            refreshPrivacyOptions()
            if (consentInformation.canRequestAds()) MobileAdsInitializer.initialize(activity)
            onDismissed(error)
        }
    }
}
