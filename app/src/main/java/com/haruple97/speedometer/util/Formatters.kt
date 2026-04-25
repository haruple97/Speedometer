package com.haruple97.speedometer.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.haruple97.speedometer.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/** 현재 Compose Context 의 첫 로케일. system 기본 폴백. */
@Composable
fun currentComposeLocale(): Locale {
    val cfg = LocalConfiguration.current
    return cfg.locales[0] ?: Locale.getDefault()
}

/**
 * 로케일과 포맷 패턴을 모두 strings.xml 의 키로 받아 SimpleDateFormat 을 만든다.
 * 패턴 자체가 언어별 리소스이므로 ko 는 "M월 d일", en 은 "MMM d" 등으로 자동 분기.
 */
@Composable
fun rememberDateFormat(@StringRes patternRes: Int): SimpleDateFormat {
    val pattern = stringResource(patternRes)
    val locale = currentComposeLocale()
    return remember(pattern, locale) { SimpleDateFormat(pattern, locale) }
}

/**
 * 소수점 1자리 숫자 포맷. NumberFormat 을 통해 로케일별 소수 기호 (".", ",") 를 자동 적용.
 * 100 이상이면 정수로 반올림 (게이지/요약 디자인 일관성).
 */
@Composable
fun formatOneDecimal(value: Float): String {
    val locale = currentComposeLocale()
    val nf = remember(locale) {
        NumberFormat.getInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
    }
    val rounded = remember(locale) { NumberFormat.getInstance(locale) }
    return if (value >= 100f) rounded.format(value.toLong()) else nf.format(value)
}

/**
 * Long 정수의 로케일 안전 포맷 (천 단위 구분자 자동).
 * en "1,234" / es "1.234" / ru "1 234" / hi "1,234".
 */
@Composable
fun formatInt(value: Long): String {
    val locale = currentComposeLocale()
    val nf = remember(locale) { NumberFormat.getInstance(locale) }
    return nf.format(value)
}

/**
 * "1h 23m" / "23m" / "0m" 형태의 지속시간 포맷. 음수/0 은 zero 키로 반환.
 *
 * @param durationMs 밀리초
 */
@Composable
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return stringResource(R.string.duration_zero)
    val totalMin = durationMs / 60_000L
    val h = (totalMin / 60).toInt()
    val m = (totalMin % 60).toInt()
    return if (h > 0) stringResource(R.string.duration_h_m, h, m)
    else stringResource(R.string.duration_m, m)
}

/**
 * 분 단위 Long 입력용 (totalMs 가 아니라 분이 이미 계산된 경우).
 */
@Composable
fun formatDurationMinutes(totalMinutes: Long): String {
    if (totalMinutes <= 0L) return stringResource(R.string.duration_zero)
    val h = (totalMinutes / 60).toInt()
    val m = (totalMinutes % 60).toInt()
    return if (h > 0) stringResource(R.string.duration_h_m, h, m)
    else stringResource(R.string.duration_m, m)
}
