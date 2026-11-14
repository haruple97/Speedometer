package com.haruple97.speedometer.data.location

/** 단조 시계 기준으로 5초 이상 갱신되지 않은 위치는 유효한 속도로 표시하지 않는다. */
internal fun isLocationFresh(nowElapsedMs: Long, fixElapsedMs: Long?): Boolean =
    fixElapsedMs != null && nowElapsedMs - fixElapsedMs in 0L until 5_000L
