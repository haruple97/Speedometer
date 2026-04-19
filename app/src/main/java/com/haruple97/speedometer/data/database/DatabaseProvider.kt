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
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
}
