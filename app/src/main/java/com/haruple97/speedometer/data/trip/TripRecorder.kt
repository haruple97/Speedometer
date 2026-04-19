package com.haruple97.speedometer.data.trip

import com.haruple97.speedometer.data.model.SpeedData
import com.haruple97.speedometer.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 속도 샘플 스트림을 관찰해 자동으로 트립을 잘라 Room에 기록한다.
 *
 * 상태 기계:
 *   Idle → Pending(>5 km/h 관측) → Running(5 km/h 유지 3초 후 Trip insert)
 *   Running → Stopping(<3 km/h 관측) → Idle(60초 유지 시 종료) / Running(복귀)
 *
 * 모든 샘플 인입을 직렬화하기 위해 Mutex를 사용. Room 쓰기는 IO 디스패처(Room 내부)가 처리.
 *
 * 과속 임계값은 **트립 시작 시점의 설정을 스냅샷**으로 저장해서, 이후 사용자가
 * 설정을 바꿔도 이 트립의 `isOverspeed` 판정과 `overspeedThresholdKmh` 는
 * 기록 당시 기준을 유지한다.
 */
class TripRecorder(
    private val dao: TripDao,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
) {

    private companion object {
        const val START_SPEED_KMH = 5f
        const val STOP_SPEED_KMH = 3f
        const val START_HOLD_MS = 3_000L
        const val STOP_HOLD_MS = 60_000L
        const val SAMPLE_BATCH = 30
        /** 이 값을 초과하는 정확도의 샘플은 거리 누적에서 제외 (속도/시계열은 유지). */
        const val MAX_ACCURACY_METERS_FOR_DISTANCE = 20f
    }

    private sealed interface State {
        data object Idle : State
        data class Pending(val sinceMs: Long) : State
        data class Running(
            val tripId: Long,
            val startedAt: Long,
            val overspeedEnabled: Boolean,
            val thresholdKmh: Float,
            var lastSampleMs: Long,
            var distanceMeters: Float,
            var maxSpeedKmh: Float,
            var sumSpeed: Double,
            var sampleCount: Int,
            var overspeedEventCount: Int,
            var lastWasOverspeed: Boolean,
            val buffer: MutableList<TripSampleEntity>,
        ) : State
        data class Stopping(val running: Running, val sinceMs: Long) : State
    }

    private var state: State = State.Idle
    private val mutex = Mutex()

    @Volatile
    private var autoRecordingEnabled: Boolean = true

    fun onSample(sample: SpeedData) {
        // timestamp == 0 인 초기 기본 SpeedData 는 무시
        if (sample.timestamp == 0L) return
        // 빠른 경로: 플래그 OFF 이면 코루틴/락 비용 없이 즉시 리턴
        if (!autoRecordingEnabled) return
        scope.launch {
            mutex.withLock {
                if (!autoRecordingEnabled) return@withLock
                advance(sample)
            }
        }
    }

    /**
     * 자동 기록 토글. OFF 로 전환 시 진행 중 트립(Running/Stopping)을 그 시점까지
     * 마무리해 저장하고 Idle 로 돌린다. ON 은 플래그만 갱신하고 다음 샘플부터 자연 진입.
     */
    fun setAutoRecordingEnabled(enabled: Boolean) {
        scope.launch {
            mutex.withLock {
                if (autoRecordingEnabled == enabled) return@withLock
                autoRecordingEnabled = enabled
                if (!enabled) {
                    val s = state
                    val running = when (s) {
                        is State.Running -> s
                        is State.Stopping -> s.running
                        else -> null
                    } ?: return@withLock
                    closeTrip(running, running.lastSampleMs)
                    state = State.Idle
                }
            }
        }
    }

    /** 앱 종료/ViewModel clear 시 진행 중인 트립 마무리 */
    fun finalizeIfRunning() {
        scope.launch {
            mutex.withLock {
                val s = state
                val running = when (s) {
                    is State.Running -> s
                    is State.Stopping -> s.running
                    else -> null
                } ?: return@withLock
                closeTrip(running, running.lastSampleMs)
                state = State.Idle
            }
        }
    }

    private suspend fun advance(sample: SpeedData) {
        val now = sample.timestamp
        val speed = sample.speedKmh

        when (val s = state) {
            is State.Idle -> {
                if (speed > START_SPEED_KMH) state = State.Pending(now)
            }
            is State.Pending -> {
                if (speed <= START_SPEED_KMH) {
                    state = State.Idle
                } else if (now - s.sinceMs >= START_HOLD_MS) {
                    startRunning(since = s.sinceMs, now = now, initialSpeed = speed)
                }
            }
            is State.Running -> {
                accumulate(s, sample)
                if (speed < STOP_SPEED_KMH) state = State.Stopping(s, now)
            }
            is State.Stopping -> {
                accumulate(s.running, sample)
                if (speed >= STOP_SPEED_KMH) {
                    state = s.running
                } else if (now - s.sinceMs >= STOP_HOLD_MS) {
                    closeTrip(s.running, now)
                    state = State.Idle
                }
            }
        }
    }

    private suspend fun startRunning(since: Long, now: Long, initialSpeed: Float) {
        val prefs = settingsRepository.preferencesFlow.first()
        val enabled = prefs.overspeedEnabled
        val threshold = prefs.overspeedThreshold
        val snapshotThreshold = if (enabled) threshold else 0f
        val firstIsOverspeed = enabled && initialSpeed > threshold

        val tripId = dao.insertTrip(
            TripEntity(
                startedAt = since,
                endedAt = 0L,
                distanceMeters = 0f,
                maxSpeedKmh = 0f,
                avgSpeedKmh = 0f,
                sampleCount = 0,
                overspeedEventCount = 0,
                overspeedThresholdKmh = snapshotThreshold,
            )
        )
        state = State.Running(
            tripId = tripId,
            startedAt = since,
            overspeedEnabled = enabled,
            thresholdKmh = threshold,
            lastSampleMs = now,
            distanceMeters = 0f,
            maxSpeedKmh = initialSpeed,
            sumSpeed = initialSpeed.toDouble(),
            sampleCount = 1,
            overspeedEventCount = if (firstIsOverspeed) 1 else 0,
            lastWasOverspeed = firstIsOverspeed,
            buffer = mutableListOf(
                TripSampleEntity(
                    tripId = tripId,
                    timestampMs = now,
                    speedKmh = initialSpeed,
                    isOverspeed = firstIsOverspeed,
                )
            ),
        )
    }

    private suspend fun accumulate(running: State.Running, sample: SpeedData) {
        running.lastSampleMs = sample.timestamp

        // 정확도가 나쁜 샘플은 거리 누적만 스킵 (속도/샘플은 그대로 반영)
        val accuracy = sample.accuracyMeters
        val acceptDistance = accuracy == null || accuracy <= MAX_ACCURACY_METERS_FOR_DISTANCE
        if (acceptDistance) {
            running.distanceMeters += sample.deltaDistanceMeters.coerceAtLeast(0f)
        }

        if (sample.speedKmh > running.maxSpeedKmh) running.maxSpeedKmh = sample.speedKmh
        running.sumSpeed += sample.speedKmh
        running.sampleCount += 1

        val nowOverspeed = running.overspeedEnabled && sample.speedKmh > running.thresholdKmh
        if (nowOverspeed && !running.lastWasOverspeed) {
            running.overspeedEventCount += 1
        }
        running.lastWasOverspeed = nowOverspeed

        running.buffer.add(
            TripSampleEntity(
                tripId = running.tripId,
                timestampMs = sample.timestamp,
                speedKmh = sample.speedKmh,
                isOverspeed = nowOverspeed,
            )
        )
        if (running.buffer.size >= SAMPLE_BATCH) {
            val batch = running.buffer.toList()
            running.buffer.clear()
            dao.insertSamples(batch)
        }
    }

    private suspend fun closeTrip(running: State.Running, endedAt: Long) {
        if (running.buffer.isNotEmpty()) {
            dao.insertSamples(running.buffer.toList())
            running.buffer.clear()
        }
        val avg = if (running.sampleCount > 0) (running.sumSpeed / running.sampleCount).toFloat() else 0f
        val snapshotThreshold = if (running.overspeedEnabled) running.thresholdKmh else 0f
        dao.updateTrip(
            TripEntity(
                id = running.tripId,
                startedAt = running.startedAt,
                endedAt = endedAt,
                distanceMeters = running.distanceMeters,
                maxSpeedKmh = running.maxSpeedKmh,
                avgSpeedKmh = avg,
                sampleCount = running.sampleCount,
                overspeedEventCount = running.overspeedEventCount,
                overspeedThresholdKmh = snapshotThreshold,
            )
        )
    }
}
