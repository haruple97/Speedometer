package com.haruple97.speedometer.data.trip

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY startedAt DESC")
    fun observeAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTrip(id: Long): TripEntity?

    @Query("SELECT * FROM trip_samples WHERE tripId = :tripId ORDER BY timestampMs ASC")
    suspend fun getSamples(tripId: Long): List<TripSampleEntity>

    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Insert
    suspend fun insertSamples(samples: List<TripSampleEntity>)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: Long)

    @Query("SELECT * FROM trips WHERE endedAt = 0")
    suspend fun findDanglingTrips(): List<TripEntity>

    @Query("SELECT MAX(timestampMs) FROM trip_samples WHERE tripId = :tripId")
    suspend fun lastSampleTimestamp(tripId: Long): Long?

    @Query(
        """SELECT
            COALESCE(SUM(distanceMeters), 0) AS totalDistanceMeters,
            COALESCE(SUM(endedAt - startedAt), 0) AS totalDurationMs,
            COALESCE(MAX(maxSpeedKmh), 0) AS maxSpeedKmh,
            COUNT(*) AS tripCount
        FROM trips
        WHERE startedAt >= :fromMs AND endedAt > 0"""
    )
    suspend fun aggregate(fromMs: Long): TripAggregate

    // ===== 고급 분석용 쿼리 (v1) =====

    @Query(
        """SELECT CAST(strftime('%w', startedAt/1000, 'unixepoch', 'localtime') AS INTEGER) AS dayOfWeek,
                  AVG(avgSpeedKmh) AS avgSpeedKmh,
                  SUM(distanceMeters) AS totalDistanceMeters,
                  COUNT(*) AS tripCount
           FROM trips WHERE endedAt > 0
           GROUP BY dayOfWeek ORDER BY dayOfWeek"""
    )
    suspend fun aggregateByWeekday(): List<WeekdayStat>

    @Query(
        """SELECT CAST(strftime('%H', startedAt/1000, 'unixepoch', 'localtime') AS INTEGER) AS hour,
                  AVG(avgSpeedKmh) AS avgSpeedKmh,
                  SUM(distanceMeters) AS totalDistanceMeters,
                  COUNT(*) AS tripCount
           FROM trips WHERE endedAt > 0
           GROUP BY hour ORDER BY hour"""
    )
    suspend fun aggregateByHour(): List<HourStat>

    @Query(
        """SELECT (startedAt / 86400000) * 86400000 AS dayEpochMs,
                  SUM(distanceMeters) AS totalDistanceMeters,
                  COUNT(*) AS tripCount
           FROM trips WHERE endedAt > 0 AND startedAt >= :fromMs
           GROUP BY dayEpochMs ORDER BY dayEpochMs"""
    )
    suspend fun aggregateByDay(fromMs: Long): List<DayStat>

    @Query("SELECT * FROM trips WHERE endedAt > 0 ORDER BY distanceMeters DESC LIMIT 1")
    suspend fun topDistanceTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE endedAt > 0 ORDER BY maxSpeedKmh DESC LIMIT 1")
    suspend fun topMaxSpeedTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE endedAt > 0 ORDER BY (endedAt - startedAt) DESC LIMIT 1")
    suspend fun topDurationTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE endedAt > 0 AND overspeedEventCount > 0 ORDER BY overspeedEventCount DESC LIMIT 1")
    suspend fun topOverspeedTrip(): TripEntity?

    /**
     * 주간(7일) 버킷 집계 — 단순 epoch 나눗셈으로 버킷팅. 월요일 정렬은 아니지만 8주 추이에는 충분.
     */
    @Query(
        """SELECT (startedAt / 604800000) * 604800000 AS weekStartMs,
                  COALESCE(SUM(distanceMeters), 0) AS totalDistanceMeters,
                  COALESCE(SUM(endedAt - startedAt), 0) AS totalDurationMs,
                  COUNT(*) AS tripCount
           FROM trips WHERE endedAt > 0 AND startedAt >= :fromMs
           GROUP BY weekStartMs ORDER BY weekStartMs"""
    )
    suspend fun aggregateByWeek(fromMs: Long): List<WeekStat>

    /** 전체 누적 집계. */
    @Query(
        """SELECT
            COALESCE(SUM(distanceMeters), 0) AS totalDistanceMeters,
            COALESCE(SUM(endedAt - startedAt), 0) AS totalDurationMs,
            COALESCE(MAX(maxSpeedKmh), 0) AS maxSpeedKmh,
            COUNT(*) AS tripCount
        FROM trips WHERE endedAt > 0"""
    )
    suspend fun totalAggregate(): TripAggregate

    /** 임의 구간 집계 ([fromMs] 포함 ~ [untilMs] 미포함). */
    @Query(
        """SELECT
            COALESCE(SUM(distanceMeters), 0) AS totalDistanceMeters,
            COALESCE(SUM(endedAt - startedAt), 0) AS totalDurationMs,
            COALESCE(MAX(maxSpeedKmh), 0) AS maxSpeedKmh,
            COUNT(*) AS tripCount
        FROM trips WHERE endedAt > 0 AND startedAt >= :fromMs AND startedAt < :untilMs"""
    )
    suspend fun rangeAggregate(fromMs: Long, untilMs: Long): TripAggregate
}
