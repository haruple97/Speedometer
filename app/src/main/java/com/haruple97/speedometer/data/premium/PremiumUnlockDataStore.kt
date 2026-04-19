package com.haruple97.speedometer.data.premium

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/** 프리미엄 언락 상태 전용 DataStore — 설정(settings) 과 분리해 격리. */
val Context.premiumUnlockDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "premium_unlocks",
)
