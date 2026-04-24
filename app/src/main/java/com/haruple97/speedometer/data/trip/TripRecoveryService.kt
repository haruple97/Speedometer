package com.haruple97.speedometer.data.trip

/**
 * 앱 시작 시 `endedAt == 0` 인 "dangling" 트립을 복구.
 *
 * 비정상 종료(OS kill, 크래시)로 `finalizeIfRunning` 이 완료되지 못한 경우를 대비해
 * 마지막 샘플 시각을 endedAt 으로 채우고, sample 테이블에서 max/avg/count 를 재계산해
 * 0 으로만 남아 있던 stats 를 살린다. distance 는 sample 에 없어 복구 불가하므로 기존 값 유지
 * (TripRecorder 의 batch 영속화로 보통 마지막 batch 시점까지의 값이 들어 있다).
 */
class TripRecoveryService(private val dao: TripDao) {

    suspend fun recoverDanglingTrips() {
        val dangling = dao.findDanglingTrips()
        for (trip in dangling) {
            val agg = dao.aggregateSamples(trip.id)
            val endedAt = if (agg.lastTimestampMs > 0) agg.lastTimestampMs else trip.startedAt
            // sample 이 하나도 없는 트립은 stats 변경 없이 endedAt 만 패치 (기존 동작 유지).
            // sample 이 있으면 max/avg/count 를 sample 에서 재계산해 채운다.
            val updated = if (agg.sampleCount > 0) {
                trip.copy(
                    endedAt = endedAt,
                    maxSpeedKmh = agg.maxSpeedKmh,
                    avgSpeedKmh = agg.avgSpeedKmh,
                    sampleCount = agg.sampleCount,
                )
            } else {
                trip.copy(endedAt = endedAt)
            }
            dao.updateTrip(updated)
        }
    }
}
