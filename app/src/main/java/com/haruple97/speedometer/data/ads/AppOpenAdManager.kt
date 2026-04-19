package com.haruple97.speedometer.data.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.haruple97.speedometer.BuildConfig
import com.haruple97.speedometer.data.trip.TripRecorder
import com.haruple97.speedometer.util.AppScope
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * App Open 광고 관리자.
 *
 * warm resume(`ProcessLifecycleOwner.ON_START`) 시 전체화면 광고를 띄우되, 주행·PiP·신규 설치
 * 등 UX 손상이 큰 맥락에서는 엄격한 가드로 건너뛴다. 가드 규약·아키텍처는 플랜 문서 참고.
 */
class AppOpenAdManager(
    private val application: Application,
    private val adUnitId: String,
    private val tripRecorder: TripRecorder,
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private companion object {
        // Google 정책: App Open 광고는 4시간 TTL
        const val AD_EXPIRATION_MS = 4L * 60 * 60 * 1000

        // Release 가드: 배포 환경에서 UX 를 보호하는 보수적 값
        const val PROD_INSTALL_COOLDOWN_MS = 24L * 60 * 60 * 1000
        const val PROD_MIN_LAUNCH_COUNT = 3
        const val PROD_MIN_GAP_BETWEEN_ADS_MS = 30L * 60 * 1000

        // Debug 가드: 테스트·검증을 쉽게 하도록 완화
        const val DEBUG_INSTALL_COOLDOWN_MS = 0L
        const val DEBUG_MIN_LAUNCH_COUNT = 0
        const val DEBUG_MIN_GAP_BETWEEN_ADS_MS = 60L * 1000  // 1분
    }

    private val installCooldownMs =
        if (BuildConfig.DEBUG) DEBUG_INSTALL_COOLDOWN_MS else PROD_INSTALL_COOLDOWN_MS
    private val minLaunchCount =
        if (BuildConfig.DEBUG) DEBUG_MIN_LAUNCH_COUNT else PROD_MIN_LAUNCH_COUNT
    private val minGapBetweenAdsMs =
        if (BuildConfig.DEBUG) DEBUG_MIN_GAP_BETWEEN_ADS_MS else PROD_MIN_GAP_BETWEEN_ADS_MS

    private val state = AppOpenAdState(application)

    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var adLoadedAtMs: Long = 0L
    private var isLoadingAd: Boolean = false
    private var isShowingAd: Boolean = false

    private var isFirstStart: Boolean = true
    private var justReturnedFromPip: Boolean = false
    private var permissionGrantedThisSession: Boolean = false

    /** Application.onCreate 에서 1회 호출. 이후 프로세스 수명 동안 리스너 유지. */
    fun attach() {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        val now = System.currentTimeMillis()
        if (state.firstInstallMs == 0L) state.firstInstallMs = now
        state.launchCount = state.launchCount + 1

        // MobileAds SDK 초기화 완료 후 첫 프리로드.
        // AppOpenAd.load 는 main UI 스레드에서만 호출 가능하므로 Dispatchers.Main 명시.
        AppScope.scope.launch(Dispatchers.Main) {
            MobileAdsInitializer.initialized.filter { it }.first()
            loadAdIfNeeded()
        }
    }

    /** 위치 권한 허용 직후 호출 — 이번 세션 내 다음 warm resume 광고 한 번 보호. */
    fun markPermissionJustGranted() {
        permissionGrantedThisSession = true
    }

    /** PiP → 일반 화면 복귀 직후 호출 — 다음 warm resume 광고 보호. */
    fun markJustReturnedFromPip() {
        justReturnedFromPip = true
    }

    // region ActivityLifecycleCallbacks — currentActivity 추적
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }
    // endregion

    // region ProcessLifecycleOwner — ON_START (전체 앱 포그라운드 복귀)
    override fun onStart(owner: LifecycleOwner) {
        if (isFirstStart) {
            // cold start 는 UX 보호를 위해 스킵. 프리로드만.
            isFirstStart = false
            loadAdIfNeeded()
            return
        }
        showAdIfAvailable()
    }
    // endregion

    private fun loadAdIfNeeded() {
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        AppOpenAd.load(
            application,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    adLoadedAtMs = System.currentTimeMillis()
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    // 로드 실패 시 조용히 실패 — 다음 기회에 다시 시도
                    isLoadingAd = false
                }
            },
        )
    }

    private fun isAdAvailable(): Boolean {
        appOpenAd ?: return false
        val elapsed = System.currentTimeMillis() - adLoadedAtMs
        return elapsed < AD_EXPIRATION_MS
    }

    private fun showAdIfAvailable() {
        if (isShowingAd) return

        if (!canShow()) {
            // 가드 실패 — transient 플래그 리셋 후 프리로드만.
            resetTransientFlags()
            loadAdIfNeeded()
            return
        }

        if (!isAdAvailable()) {
            // 광고가 아직 준비 안 됐으면 앱 진입 지연 없이 프리로드 후 리턴.
            loadAdIfNeeded()
            return
        }

        val activity = currentActivity ?: return
        val ad = appOpenAd ?: return

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                state.lastShownMs = System.currentTimeMillis()
                resetTransientFlags()
                loadAdIfNeeded()  // 다음 warm resume 을 위한 프리로드
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingAd = false
                resetTransientFlags()
                loadAdIfNeeded()
            }
        }
        ad.show(activity)
    }

    private fun canShow(): Boolean {
        val now = System.currentTimeMillis()
        if (state.launchCount <= minLaunchCount) return false
        if (now - state.firstInstallMs < installCooldownMs) return false
        if (now - state.lastShownMs < minGapBetweenAdsMs) return false
        if (permissionGrantedThisSession) return false
        if (justReturnedFromPip) return false
        if (tripRecorder.isActive()) return false
        return true
    }

    private fun resetTransientFlags() {
        permissionGrantedThisSession = false
        justReturnedFromPip = false
    }
}
