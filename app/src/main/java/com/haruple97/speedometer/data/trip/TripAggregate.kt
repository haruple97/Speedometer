package com.haruple97.speedometer.data.trip

import java.util.Calendar

data class TripAggregate(
    val totalDistanceMeters: Float,
    val totalDurationMs: Long,
    val maxSpeedKmh: Float,
    val tripCount: Int,
) {
    companion object {
        val EMPTY = TripAggregate(0f, 0L, 0f, 0)
    }
}

enum class SummaryPeriod(val label: String) {
    THIS_WEEK("이번주"),
    THIS_MONTH("이번달"),
    ALL("전체");

    /** 기간의 시작 시각(epoch ms)을 반환. ALL 이면 0. */
    fun fromEpochMs(nowMs: Long): Long {
        if (this == ALL) return 0L
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        when (this) {
            THIS_WEEK -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                // Calendar 의 월요일 첫 주 기준으로 해당 주의 시작으로 이동
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = (dayOfWeek - Calendar.MONDAY + 7) % 7
                cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            }
            THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
            }
            ALL -> Unit
        }
        return cal.timeInMillis
    }
}
