package com.haruple97.speedometer.data.trip

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_samples",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("tripId")],
)
data class TripSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val timestampMs: Long,
    val speedKmh: Float,
    /** 수집 시점 기준, 트립의 과속 임계값(스냅샷)을 초과했는지 여부. */
    val isOverspeed: Boolean = false,
)
