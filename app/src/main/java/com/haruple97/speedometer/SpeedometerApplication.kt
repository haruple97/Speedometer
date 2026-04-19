package com.haruple97.speedometer

import android.app.Application
import com.haruple97.speedometer.data.ads.AdUnitIds
import com.haruple97.speedometer.data.ads.AppOpenAdManager
import com.haruple97.speedometer.data.ads.RewardedAdManager
import com.haruple97.speedometer.data.trip.TripRecorderProvider

/**
 * 앱 전역 싱글턴(App Open · Rewarded 광고 매니저 등)을 이 지점에서 일원화 관리.
 *
 * Application 수명 동안 유지되어야 하는 컴포넌트들은 여기서 생성·attach.
 */
class SpeedometerApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    lateinit var rewardedAdManager: RewardedAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        val tripRecorder = TripRecorderProvider.get(this)

        appOpenAdManager = AppOpenAdManager(
            application = this,
            adUnitId = AdUnitIds.appOpen,
            tripRecorder = tripRecorder,
        ).also { it.attach() }

        rewardedAdManager = RewardedAdManager(
            application = this,
            adUnitId = AdUnitIds.rewardedPremium,
            tripRecorder = tripRecorder,
        ).also { it.attach() }
    }
}
