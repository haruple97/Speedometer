package com.haruple97.speedometer.ui.component.analytics

import com.haruple97.speedometer.data.settings.SpeedUnit
import com.haruple97.speedometer.data.trip.ComparisonPair
import com.haruple97.speedometer.data.trip.HourStat
import com.haruple97.speedometer.data.trip.WeekdayStat
import kotlin.math.roundToInt

private val DAY_LABELS = arrayOf("일", "월", "화", "수", "목", "금", "토")

/** 집계 데이터를 보고 자연어 문장 1~3개를 추출한다. 룰 기반(단순). */
object AnalyticsInsightBuilder {

    fun build(
        weekday: List<WeekdayStat>,
        hour: List<HourStat>,
        comparison: ComparisonPair?,
        speedUnit: SpeedUnit,
    ): List<String> {
        val sentences = mutableListOf<String>()

        // 1) 가장 활동적인 시간대
        hour.maxByOrNull { it.tripCount }?.takeIf { it.tripCount >= 3 }?.let { peak ->
            val bucket = timeBucketOf(peak.hour)
            sentences += "주로 $bucket (${peak.hour}시 전후) 에 ${peak.tripCount}회 주행하셨어요."
        }

        // 2) 가장 빠른 요일
        weekday.maxByOrNull { it.avgSpeedKmh }?.takeIf { it.tripCount >= 2 }?.let { fastest ->
            val label = DAY_LABELS.getOrNull(fastest.dayOfWeek) ?: return@let
            val display = speedUnit.fromKmh(fastest.avgSpeedKmh).roundToInt()
            sentences += "${label}요일에 가장 빠른 속도 (평균 $display ${speedUnit.label}) 를 기록하셨어요."
        }

        // 3) 이번 주 vs 지난주 변화
        comparison?.distancePercentChange()?.takeIf { kotlin.math.abs(it) > 5f }?.let { pct ->
            val direction = if (pct > 0) "늘었" else "줄었"
            sentences += "지난 주 대비 주행 거리가 ${kotlin.math.abs(pct).roundToInt()}% ${direction}어요."
        }

        return sentences.take(3)
    }

    private fun timeBucketOf(hour: Int): String = when (hour) {
        in 4..7 -> "새벽/이른 아침"
        in 8..9 -> "출근 시간대"
        in 10..15 -> "낮 시간대"
        in 16..18 -> "퇴근 시간대"
        in 19..22 -> "저녁 시간대"
        else -> "심야 시간대"
    }
}
