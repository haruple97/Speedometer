package com.haruple97.speedometer.data.ads

import android.app.Activity
import android.app.Application
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.haruple97.speedometer.data.trip.TripRecorder
import com.haruple97.speedometer.util.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Rewarded 광고 관리자 (프리미엄 기능 언락용 opt-in 보상형).
 *
 * - 앱 전역 싱글턴, Application.onCreate 에서 attach.
 * - MobileAds SDK 초기화 후 프리로드.
 * - `showForReward(activity, onRewardEarned)` 호출 시 준비된 광고 즉시 노출, 없으면 실패.
 * - 주행 중([TripRecorder.isActive]) 엔 표시 금지 가드.
 * - 표시 후 dismiss 에서 다음 광고 프리로드.
 */
class RewardedAdManager(
    private val application: Application,
    private val adUnitId: String,
    private val tripRecorder: TripRecorder,
) {

    private var loadedAd: RewardedAd? = null
    private var isLoading: Boolean = false
    private var isShowing: Boolean = false

    fun attach() {
        // SDK 의 RewardedAd.load 는 main UI 스레드에서만 호출 가능.
        AppScope.scope.launch(Dispatchers.Main) {
            MobileAdsInitializer.initialized.filter { it }.first()
            loadIfNeeded()
        }
    }

    /** 광고를 준비된 경우 즉시 노출. 완료 시 [onRewardEarned] 호출. */
    fun showForReward(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit = {},
    ) {
        if (isShowing) {
            onUnavailable()
            return
        }
        if (tripRecorder.isActive()) {
            // 주행 중엔 광고 금지 — 호출자에게 실패 알림
            onUnavailable()
            return
        }
        val ad = loadedAd
        if (ad == null) {
            onUnavailable()
            loadIfNeeded()
            return
        }

        var rewardConsumed = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowing = true
            }

            override fun onAdDismissedFullScreenContent() {
                loadedAd = null
                isShowing = false
                loadIfNeeded()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadedAd = null
                isShowing = false
                onUnavailable()
                loadIfNeeded()
            }
        }

        ad.show(activity, OnUserEarnedRewardListener {
            if (!rewardConsumed) {
                rewardConsumed = true
                onRewardEarned()
            }
        })
    }

    private fun loadIfNeeded() {
        if (isLoading || loadedAd != null) return
        isLoading = true
        RewardedAd.load(
            application,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadedAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadedAd = null
                    isLoading = false
                }
            },
        )
    }
}
