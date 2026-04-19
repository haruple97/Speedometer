package com.haruple97.speedometer.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: SpeedometerDatabase? = null

    fun get(context: Context): SpeedometerDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SpeedometerDatabase::class.java,
                "speedometer.db",
            ).build().also { instance = it }
        }
}
