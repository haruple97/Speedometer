package com.haruple97.speedometer.data.trip

import android.content.Context
import com.haruple97.speedometer.data.database.DatabaseProvider
import com.haruple97.speedometer.util.AppScope

object TripRecorderProvider {
    @Volatile
    private var instance: TripRecorder? = null

    fun get(context: Context): TripRecorder =
        instance ?: synchronized(this) {
            instance ?: TripRecorder(
                dao = DatabaseProvider.get(context).tripDao(),
                scope = AppScope.scope,
            ).also { instance = it }
        }
}
