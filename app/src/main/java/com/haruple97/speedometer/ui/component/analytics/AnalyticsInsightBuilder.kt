package com.haruple97.speedometer.ui.component.analytics

import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.ComparisonPair
import com.haruple97.speedometer.data.trip.HourStat
import com.haruple97.speedometer.data.trip.WeekdayStat
import kotlin.math.abs
import kotlin.math.roundToInt

/** 인사이트 한 줄을 표현하는 토큰. Composable 에서 stringResource 로 렌더된다. */
sealed interface InsightToken {
    data class PeakBucket(
        val bucket: TimeBucket,
        val hour: Int,
        val tripCount: Int,
    ) : InsightToken

    data class FastestWeekday(
        /** Calendar.DAY_OF_WEEK 기준 0=일 ~ 6=토 (WeekdayStat.dayOfWeek 와 동일). */
        val dayOfWeek: Int,
        val avgDisplay: Int,
        val unitLabel: String,
    ) : InsightToken

    data class WeeklyDistanceUp(val percent: Int) : InsightToken
    data class WeeklyDistanceDown(val percent: Int) : InsightToken
}

/** AnalyticsInsightBuilder 가 분류하는 시간대 버킷. */
enum class TimeBucket {
    DAWN,             // 04:00–07:59
    COMMUTE_MORNING,  // 08:00–09:59
    DAYTIME,          // 10:00–15:59
    COMMUTE_EVENING,  // 16:00–18:59
    EVENING,          // 19:00–22:59
    LATE_NIGHT,       // 23:00–03:59
}

/** 집계 데이터를 보고 자연어 인사이트 토큰 1~3개를 추출한다. 룰 기반(단순). */
object AnalyticsInsightBuilder {

    fun build(
        weekday: List<WeekdayStat>,
        hour: List<HourStat>,
        comparison: ComparisonPair?,
        speedUnit: SpeedUnit,
    ): List<InsightToken> = buildList {
        // 1) 가장 활동적인 시간대
        hour.maxByOrNull { it.tripCount }?.takeIf { it.tripCount >= 3 }?.let { peak ->
            add(InsightToken.PeakBucket(bucketOf(peak.hour), peak.hour, peak.tripCount))
        }

        // 2) 가장 빠른 요일
        weekday.maxByOrNull { it.avgSpeedKmh }?.takeIf { it.tripCount >= 2 }?.let { fastest ->
            add(
                InsightToken.FastestWeekday(
                    dayOfWeek = fastest.dayOfWeek,
                    avgDisplay = speedUnit.fromKmh(fastest.avgSpeedKmh).roundToInt(),
                    unitLabel = speedUnit.label,
                )
            )
        }

        // 3) 이번 주 vs 지난주 변화
        comparison?.distancePercentChange()?.takeIf { abs(it) > 5f }?.let { pct ->
            val absPct = abs(pct).roundToInt()
            add(
                if (pct > 0) InsightToken.WeeklyDistanceUp(absPct)
                else InsightToken.WeeklyDistanceDown(absPct)
            )
        }
    }.take(3)

    private fun bucketOf(hour: Int): TimeBucket = when (hour) {
        in 4..7 -> TimeBucket.DAWN
        in 8..9 -> TimeBucket.COMMUTE_MORNING
        in 10..15 -> TimeBucket.DAYTIME
        in 16..18 -> TimeBucket.COMMUTE_EVENING
        in 19..22 -> TimeBucket.EVENING
        else -> TimeBucket.LATE_NIGHT
    }
}
