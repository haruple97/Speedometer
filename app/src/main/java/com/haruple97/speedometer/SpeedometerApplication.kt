package com.haruple97.speedometer

import android.app.Application
import com.haruple97.speedometer.data.ads.AdUnitIds
import com.haruple97.speedometer.data.ads.AppOpenAdManager
import com.haruple97.speedometer.data.trip.TripRecorderProvider

/**
 * 앱 전역 싱글턴(App Open 광고 매니저 등)을 이 지점에서 일원화 관리.
 *
 * App Open 광고는 `ProcessLifecycleOwner` / `ActivityLifecycleCallbacks` 를
 * Application 수준에서 구독해야 하므로 커스텀 Application 클래스가 필요하다.
 */
class SpeedometerApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager = AppOpenAdManager(
            application = this,
            adUnitId = AdUnitIds.appOpen,
            tripRecorder = TripRecorderProvider.get(this),
        ).also { it.attach() }
    }
}
