package com.haruple97.speedometer.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE trips ADD COLUMN overspeedThresholdKmh REAL NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE trip_samples ADD COLUMN isOverspeed INTEGER NOT NULL DEFAULT 0"
        )
    }
}
