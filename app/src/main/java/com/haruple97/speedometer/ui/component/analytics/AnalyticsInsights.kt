package com.haruple97.speedometer.ui.component.analytics

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.haruple97.speedometer.R

/**
 * [AnalyticsInsightBuilder] 가 만든 토큰들을 현재 로케일의 완성형 문장 리스트로 렌더한다.
 *
 * 요일 표기는 2단계 합성:
 *   weekday_short_labels[idx]  →  weekday_with_suffix  →  insight_fastest_weekday 의 %1$s
 * 한국어 "월요일", 일본어 "月曜日", 영어 "Mon" 등 모양이 다른 합성을 같은 키 구조로 다룰 수 있다.
 */
@Composable
fun rememberInsightSentences(tokens: List<InsightToken>): List<String> {
    val weekdayLabels = stringArrayResource(R.array.weekday_short_labels)
    return tokens.map { token ->
        when (token) {
            is InsightToken.PeakBucket -> stringResource(
                R.string.insight_peak_bucket,
                stringResource(bucketLabelRes(token.bucket)),
                token.hour,
                token.tripCount,
            )
            is InsightToken.FastestWeekday -> {
                val short = weekdayLabels.getOrElse(token.dayOfWeek) { "" }
                val day = stringResource(R.string.weekday_with_suffix, short)
                stringResource(R.string.insight_fastest_weekday, day, token.avgDisplay, token.unitLabel)
            }
            is InsightToken.WeeklyDistanceUp ->
                stringResource(R.string.insight_weekly_distance_up, token.percent)
            is InsightToken.WeeklyDistanceDown ->
                stringResource(R.string.insight_weekly_distance_down, token.percent)
        }
    }
}

@StringRes
private fun bucketLabelRes(b: TimeBucket): Int = when (b) {
    TimeBucket.DAWN -> R.string.time_bucket_dawn
    TimeBucket.COMMUTE_MORNING -> R.string.time_bucket_commute_morning
    TimeBucket.DAYTIME -> R.string.time_bucket_daytime
    TimeBucket.COMMUTE_EVENING -> R.string.time_bucket_commute_evening
    TimeBucket.EVENING -> R.string.time_bucket_evening
    TimeBucket.LATE_NIGHT -> R.string.time_bucket_late_night
}
