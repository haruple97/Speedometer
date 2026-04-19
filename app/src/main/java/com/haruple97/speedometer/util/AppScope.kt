package com.haruple97.speedometer.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 프로세스 수명을 따르는 CoroutineScope.
 *
 * VM/Activity 생멸과 무관하게 유지되므로 위치 공유 플로우, TripRecorder 등
 * 앱 전역 싱글턴들이 이 스코프를 공유한다.
 */
object AppScope {
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
