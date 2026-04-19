package com.haruple97.speedometer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.haruple97.speedometer.data.trip.TripDao
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.data.trip.TripSampleEntity

@Database(
    entities = [TripEntity::class, TripSampleEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SpeedometerDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
}
