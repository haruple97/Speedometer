package com.haruple97.speedometer.data.location

import android.content.Context
import com.haruple97.speedometer.util.AppScope

object LocationRepositoryProvider {
    @Volatile
    private var instance: LocationRepository? = null

    fun get(context: Context): LocationRepository =
        instance ?: synchronized(this) {
            instance ?: DefaultLocationRepository(
                context = context.applicationContext,
                scope = AppScope.scope,
            ).also { instance = it }
        }
}
