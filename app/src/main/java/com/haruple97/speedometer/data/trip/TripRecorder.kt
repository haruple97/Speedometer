package com.haruple97.speedometer.data.trip

import com.haruple97.speedometer.data.model.SpeedData
import com.haruple97.speedometer.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 속도 샘플 스트림을 관찰해 트립을 Room 에 기록한다. 자동 / 수동 두 모드를 지원.
 *
 * **자동 모드** (`autoRecordingEnabled = true`)
 *   Idle → Pending(>5 km/h 관측) → Running(5 km/h 유지 3초 후 Trip insert)
 *   Running → Stopping(<3 km/h 관측) → Idle(60초 유지 시 종료) / Running(복귀)
 *
 * **수동 모드** (`autoRecordingEnabled = false`)
 *   사용자가 [startManualRecording] 으로 즉시 Running 진입, [stopManualRecording] 으로 종료.
 *   자동 전이(Pending/Stopping) 는 일어나지 않으며, 정지 60초 룰이 적용되지 않는다.
 *
 * 모든 샘플 인입을 직렬화하기 위해 Mutex 를 사용. Room 쓰기는 IO 디스패처(Room 내부)가 처리.
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
            /** 수동 시작된 트립이면 true — 자동 종료(Stopping) 전이를 건너뛴다. */
            val isManual: Boolean,
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
        set(value) {
            field = value
            _activeFlow.value = value is State.Running || value is State.Stopping
        }

    private val _activeFlow = MutableStateFlow(false)
    /** UI 인디케이터(REC) 등에서 구독하는 활성 상태. Running/Stopping 일 때 true. */
    val activeFlow: StateFlow<Boolean> = _activeFlow.asStateFlow()

    /** 트립이 진행 중(Running 또는 Stopping 상태)인지 여부. 광고 가드 등에서 사용. */
    fun isActive(): Boolean = state is State.Running || state is State.Stopping
    private val mutex = Mutex()

    @Volatile
    private var autoRecordingEnabled: Boolean = true

    fun onSample(sample: SpeedData) {
        // timestamp == 0 인 초기 기본 SpeedData 는 무시
        if (sample.timestamp == 0L) return
        // 빠른 경로: 자동 모드 OFF 이고 수동 트립도 진행 중이지 않으면 즉시 리턴.
        // 수동 모드에서 trip 이 활성일 때는 sample 을 계속 누적해야 한다.
        if (!autoRecordingEnabled && state !is State.Running) return
        scope.launch {
            mutex.withLock {
                if (!autoRecordingEnabled && state !is State.Running) return@withLock
                advance(sample)
            }
        }
    }

    /**
     * 자동 기록 토글. 모드 전환 시 진행 중 트립(자동/수동)을 항상 마무리해 저장하고 Idle 로 돌린다.
     * 한 트립 row 안에 자동/수동 구간이 섞이지 않도록 명시적으로 끊는다.
     */
    fun setAutoRecordingEnabled(enabled: Boolean) {
        scope.launch {
            mutex.withLock {
                if (autoRecordingEnabled == enabled) return@withLock
                autoRecordingEnabled = enabled
                val s = state
                val running = when (s) {
                    is State.Running -> s
                    is State.Stopping -> s.running
                    else -> null
                }
                if (running != null) {
                    closeTrip(running, running.lastSampleMs.coerceAtLeast(running.startedAt))
                    state = State.Idle
                }
            }
        }
    }

    /**
     * 수동 모드에서 사용자가 "기록 시작" 버튼을 눌렀을 때 호출. 즉시 Running 으로 진입한다.
     * 자동 모드일 때 호출되면 무시 — 자동 모드는 onSample 의 상태 머신이 알아서 처리한다.
     * 이미 활성 트립(Running/Stopping)이 있으면 중복 시작을 막기 위해 무시한다.
     */
    fun startManualRecording() {
        scope.launch {
            mutex.withLock {
                if (autoRecordingEnabled) return@withLock
                if (state is State.Running || state is State.Stopping) return@withLock
                val now = System.currentTimeMillis()
                startRunning(since = now, now = now, initialSpeed = 0f, isManual = true)
            }
        }
    }

    /**
     * 수동 모드에서 사용자가 "기록 중지" 버튼을 눌렀을 때 호출. 진행 중 수동 트립을 마무리한다.
     * 자동 모드 트립은 이 메서드로 종료되지 않는다 (정책: 자동/수동 책임 분리).
     */
    fun stopManualRecording() {
        scope.launch {
            mutex.withLock {
                val s = state as? State.Running ?: return@withLock
                if (!s.isManual) return@withLock
                closeTrip(s, s.lastSampleMs.coerceAtLeast(s.startedAt))
                state = State.Idle
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
                // 수동 모드에서는 자동 진입을 하지 않는다 (시작은 startManualRecording 으로만).
                if (autoRecordingEnabled && speed > START_SPEED_KMH) state = State.Pending(now)
            }
            is State.Pending -> {
                if (speed <= START_SPEED_KMH) {
                    state = State.Idle
                } else if (now - s.sinceMs >= START_HOLD_MS) {
                    startRunning(since = s.sinceMs, now = now, initialSpeed = speed, isManual = false)
                }
            }
            is State.Running -> {
                accumulate(s, sample)
                // 수동 트립은 정지 60초 룰을 따르지 않음 — stopManualRecording 으로만 종료.
                if (!s.isManual && speed < STOP_SPEED_KMH) state = State.Stopping(s, now)
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

    private suspend fun startRunning(since: Long, now: Long, initialSpeed: Float, isManual: Boolean) {
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
        // 수동 시작은 첫 sample 이 도착하기 전 시점이라 buffer 를 비우고 sampleCount=0 으로 시작.
        // 자동 시작은 Pending 단계에서 본 initialSpeed 를 첫 샘플로 push.
        state = State.Running(
            tripId = tripId,
            startedAt = since,
            overspeedEnabled = enabled,
            thresholdKmh = threshold,
            isManual = isManual,
            lastSampleMs = now,
            distanceMeters = 0f,
            maxSpeedKmh = if (isManual) 0f else initialSpeed,
            sumSpeed = if (isManual) 0.0 else initialSpeed.toDouble(),
            sampleCount = if (isManual) 0 else 1,
            overspeedEventCount = if (!isManual && firstIsOverspeed) 1 else 0,
            lastWasOverspeed = !isManual && firstIsOverspeed,
            buffer = if (isManual) {
                mutableListOf()
            } else {
                mutableListOf(
                    TripSampleEntity(
                        tripId = tripId,
                        timestampMs = now,
                        speedKmh = initialSpeed,
                        isOverspeed = firstIsOverspeed,
                    )
                )
            },
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
            // batch flush 시점마다 누적 stats 도 영속화 — 비정상 종료 시 데이터 손실 최소화.
            // endedAt 은 0 으로 유지해 dangling 판별을 깨지 않는다 (closeTrip 만이 endedAt 의 단일 권한자).
            persistRunningStats(running)
        }
    }

    /** Running 상태의 누적 stats 를 trips row 에 반영. endedAt=0 유지. */
    private suspend fun persistRunningStats(running: State.Running) {
        val avg = if (running.sampleCount > 0) (running.sumSpeed / running.sampleCount).toFloat() else 0f
        val snapshotThreshold = if (running.overspeedEnabled) running.thresholdKmh else 0f
        dao.updateTrip(
            TripEntity(
                id = running.tripId,
                startedAt = running.startedAt,
                endedAt = 0L,
                distanceMeters = running.distanceMeters,
                maxSpeedKmh = running.maxSpeedKmh,
                avgSpeedKmh = avg,
                sampleCount = running.sampleCount,
                overspeedEventCount = running.overspeedEventCount,
                overspeedThresholdKmh = snapshotThreshold,
            )
        )
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
