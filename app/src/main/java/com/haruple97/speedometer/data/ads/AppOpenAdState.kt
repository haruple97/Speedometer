package com.haruple97.speedometer.data.ads

import android.content.Context
import androidx.core.content.edit

/**
 * App Open 광고의 빈도 제어·신규 설치 쿨다운을 위한 경량 상태 저장소.
 *
 * 라이프사이클 콜백(`ProcessLifecycleOwner.ON_START`) 에서 **동기적으로 즉시** 광고 노출
 * 여부를 결정해야 하므로 DataStore 비동기 I/O 대신 SharedPreferences 를 직접 사용.
 */
class AppOpenAdState(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var firstInstallMs: Long
        get() = prefs.getLong(KEY_FIRST_INSTALL_MS, 0L)
        set(value) = prefs.edit { putLong(KEY_FIRST_INSTALL_MS, value) }

    var launchCount: Int
        get() = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_LAUNCH_COUNT, value) }

    var lastShownMs: Long
        get() = prefs.getLong(KEY_LAST_SHOWN_MS, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_SHOWN_MS, value) }

    private companion object {
        const val PREF_NAME = "app_open_ad_state"
        const val KEY_FIRST_INSTALL_MS = "first_install_ms"
        const val KEY_LAUNCH_COUNT = "launch_count"
        const val KEY_LAST_SHOWN_MS = "last_shown_ms"
    }
}
