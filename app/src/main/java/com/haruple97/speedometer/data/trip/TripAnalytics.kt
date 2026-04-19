package com.haruple97.speedometer.data.trip

/**
 * 고급 주행 분석 화면용 집계 데이터 모음.
 *
 * 모든 거리는 미터, 속도는 km/h (km/h·mph 변환은 UI 층에서).
 * SQL GROUP BY 로 생성된 행은 Room 이 자동 매핑.
 */

data class WeekdayStat(
    val dayOfWeek: Int,            // 0=일요일 ~ 6=토요일 (sqlite strftime %w)
    val avgSpeedKmh: Float,
    val totalDistanceMeters: Float,
    val tripCount: Int,
)

data class HourStat(
    val hour: Int,                 // 0..23
    val avgSpeedKmh: Float,
    val totalDistanceMeters: Float,
    val tripCount: Int,
)

data class DayStat(
    val dayEpochMs: Long,          // 해당 날짜의 0시(epoch ms)
    val totalDistanceMeters: Float,
    val tripCount: Int,
)

/**
 * 속도 구간별 누적 거리. v1 에선 트립의 avgSpeed 를 기준으로 근사
 * (샘플 단위 정교화는 v1.5).
 */
data class SpeedZoneStat(
    val safeMeters: Float,         // < 120 km/h
    val cautionMeters: Float,      // 120 ≤ v < 200
    val dangerMeters: Float,       // 200 ≤ v < 280
    val criticalMeters: Float,     // ≥ 280
) {
    val totalMeters: Float get() = safeMeters + cautionMeters + dangerMeters + criticalMeters
}

/** 주간 단위(7일 버킷) 집계. [weekStartMs] 는 그 주 시작 자정(epoch ms). */
data class WeekStat(
    val weekStartMs: Long,
    val totalDistanceMeters: Float,
    val totalDurationMs: Long,
    val tripCount: Int,
)

/** 이번 주 vs 지난주 같은 기간 비교. null 은 해당 기간 데이터 없음. */
data class ComparisonPair(
    val current: TripAggregate,
    val previous: TripAggregate,
) {
    fun distancePercentChange(): Float? = percentChange(previous.totalDistanceMeters, current.totalDistanceMeters)
    fun durationPercentChange(): Float? = percentChange(previous.totalDurationMs.toFloat(), current.totalDurationMs.toFloat())
    fun maxSpeedPercentChange(): Float? = percentChange(previous.maxSpeedKmh, current.maxSpeedKmh)
    fun tripCountPercentChange(): Float? = percentChange(previous.tripCount.toFloat(), current.tripCount.toFloat())

    private fun percentChange(prev: Float, curr: Float): Float? {
        if (prev <= 0f) return null
        return ((curr - prev) / prev) * 100f
    }
}
