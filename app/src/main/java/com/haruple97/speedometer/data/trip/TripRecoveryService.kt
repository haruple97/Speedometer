package com.haruple97.speedometer.data.trip

/**
 * 앱 시작 시 `endedAt == 0` 인 "dangling" 트립을 복구.
 *
 * 비정상 종료(OS kill, 크래시)로 `finalizeIfRunning` 이 완료되지 못한 경우를 대비해
 * 마지막 샘플 시각을 endedAt 으로 채워 넣는다. 샘플이 하나도 없으면 startedAt 을 사용.
 */
class TripRecoveryService(private val dao: TripDao) {

    suspend fun recoverDanglingTrips() {
        val dangling = dao.findDanglingTrips()
        for (trip in dangling) {
            val endedAt = dao.lastSampleTimestamp(trip.id) ?: trip.startedAt
            dao.updateTrip(trip.copy(endedAt = endedAt))
        }
    }
}
