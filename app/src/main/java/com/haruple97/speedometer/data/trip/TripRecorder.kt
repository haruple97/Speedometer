package com.haruple97.speedometer.data.trip

import com.haruple97.speedometer.data.model.SpeedData
import kotlinx.coroutines.CoroutineScope
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
 */
class TripRecorder(
    private val dao: TripDao,
    private val scope: CoroutineScope,
) {

    private companion object {
        const val START_SPEED_KMH = 5f
        const val STOP_SPEED_KMH = 3f
        const val START_HOLD_MS = 3_000L
        const val STOP_HOLD_MS = 60_000L
        const val SAMPLE_BATCH = 30
    }

    private sealed interface State {
        data object Idle : State
        data class Pending(val sinceMs: Long) : State
        data class Running(
            val tripId: Long,
            val startedAt: Long,
            var lastSampleMs: Long,
            var distanceMeters: Float,
            var maxSpeedKmh: Float,
            var sumSpeed: Double,
            var sampleCount: Int,
            val buffer: MutableList<TripSampleEntity>,
        ) : State
        data class Stopping(val running: Running, val sinceMs: Long) : State
    }

    private var state: State = State.Idle
    private val mutex = Mutex()

    fun onSample(sample: SpeedData) {
        // timestamp == 0 인 초기 기본 SpeedData 는 무시
        if (sample.timestamp == 0L) return
        scope.launch {
            mutex.withLock { advance(sample) }
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
                    val tripId = dao.insertTrip(
                        TripEntity(
                            startedAt = s.sinceMs,
                            endedAt = 0L,
                            distanceMeters = 0f,
                            maxSpeedKmh = 0f,
                            avgSpeedKmh = 0f,
                            sampleCount = 0,
                            overspeedEventCount = 0,
                        )
                    )
                    state = State.Running(
                        tripId = tripId,
                        startedAt = s.sinceMs,
                        lastSampleMs = now,
                        distanceMeters = 0f,
                        maxSpeedKmh = speed,
                        sumSpeed = speed.toDouble(),
                        sampleCount = 1,
                        buffer = mutableListOf(
                            TripSampleEntity(tripId = tripId, timestampMs = now, speedKmh = speed)
                        ),
                    )
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

    private suspend fun accumulate(running: State.Running, sample: SpeedData) {
        running.lastSampleMs = sample.timestamp
        running.distanceMeters += sample.deltaDistanceMeters.coerceAtLeast(0f)
        if (sample.speedKmh > running.maxSpeedKmh) running.maxSpeedKmh = sample.speedKmh
        running.sumSpeed += sample.speedKmh
        running.sampleCount += 1
        running.buffer.add(
            TripSampleEntity(
                tripId = running.tripId,
                timestampMs = sample.timestamp,
                speedKmh = sample.speedKmh,
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
        dao.updateTrip(
            TripEntity(
                id = running.tripId,
                startedAt = running.startedAt,
                endedAt = endedAt,
                distanceMeters = running.distanceMeters,
                maxSpeedKmh = running.maxSpeedKmh,
                avgSpeedKmh = avg,
                sampleCount = running.sampleCount,
                overspeedEventCount = 0,
            )
        )
    }
}
