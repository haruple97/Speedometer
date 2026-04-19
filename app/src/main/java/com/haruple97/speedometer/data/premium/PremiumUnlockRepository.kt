package com.haruple97.speedometer.data.premium

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 프리미엄 기능 언락 상태 관리.
 *
 * 시간 기반: `unlockedUntilMs` Long 저장. 현재 시각 < 저장값 이면 언락.
 * 카운트 기반: `remainingUses` Int 저장. `consume()` 호출로 차감, 0 이면 잠김.
 *
 * 동일한 저장소에 두 종류 공존 — 기능별로 시간 또는 카운트 키 한쪽만 사용.
 */
class PremiumUnlockRepository(private val context: Context) {

    private fun untilKey(feature: PremiumFeature) =
        longPreferencesKey("${feature.storageKey}_unlocked_until_ms")

    private fun usesKey(feature: PremiumFeature) =
        intPreferencesKey("${feature.storageKey}_remaining_uses")

    suspend fun isUnlocked(feature: PremiumFeature): Boolean =
        observeUnlockState(feature).first().isActive

    /** 시간 기반 언락. [durationMs] 만큼 현재 시각 이후까지 언락. */
    suspend fun unlockForDuration(feature: PremiumFeature, durationMs: Long) {
        val until = System.currentTimeMillis() + durationMs
        context.premiumUnlockDataStore.edit { prefs ->
            prefs[untilKey(feature)] = until
        }
    }

    /** 카운트 기반 언락. 기존 잔여분에 [uses] 더함. */
    suspend fun grantUses(feature: PremiumFeature, uses: Int) {
        context.premiumUnlockDataStore.edit { prefs ->
            val current = prefs[usesKey(feature)] ?: 0
            prefs[usesKey(feature)] = current + uses
        }
    }

    /** 카운트 기반 1회 차감. 잔여 0 일 땐 no-op. */
    suspend fun consume(feature: PremiumFeature) {
        context.premiumUnlockDataStore.edit { prefs ->
            val current = prefs[usesKey(feature)] ?: 0
            if (current > 0) prefs[usesKey(feature)] = current - 1
        }
    }

    fun observeUnlockState(feature: PremiumFeature): Flow<UnlockState> =
        context.premiumUnlockDataStore.data.map { prefs ->
            val until = prefs[untilKey(feature)] ?: 0L
            val uses = prefs[usesKey(feature)] ?: 0
            val now = System.currentTimeMillis()
            when {
                until > now -> UnlockState.UnlockedUntil(until)
                uses > 0 -> UnlockState.UsesRemaining(uses)
                else -> UnlockState.Locked
            }
        }
}

sealed class UnlockState {
    data object Locked : UnlockState()
    data class UnlockedUntil(val untilMs: Long) : UnlockState()
    data class UsesRemaining(val count: Int) : UnlockState()

    val isActive: Boolean
        get() = this !is Locked
}
