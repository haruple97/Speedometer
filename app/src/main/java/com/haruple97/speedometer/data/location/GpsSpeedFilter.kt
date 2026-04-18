package com.haruple97.speedometer.data.location

import android.location.Location
import android.os.Build
import kotlin.math.abs

/**
 * GPS 속도 스파이크/노이즈 필터.
 *
 * 방어선:
 *  1) Sanity cap   — 음수 또는 [maxPlausibleSpeedKmh] 초과 거부
 *  2) Accuracy gate — horizontal / speed accuracy 가 임계 초과면 거부
 *  3) Acceleration gate — 직전 수락값 대비 가속도가 물리적 한계 초과면 거부
 *  4) EMA smoothing — 통과한 값은 지수가중이동평균으로 다듬어 방출
 */
class GpsSpeedFilter(
    /**
     * 최대 허용 속도(km/h). 이 값을 초과하면 즉시 거부.
     *
     * 350 으로 잡은 이유:
     *  - 게이지 상한이 350 km/h 이므로, 그 이상은 어차피 표시 불가.
     *  - 일반 승용차 최고 속도(≈ 250–300 km/h) + 여유.
     *  - GPS 튐으로 들어오는 2000 km/h 수준의 극단적 스파이크를
     *    다른 게이트가 모두 놓쳐도 잡아내는 "최후의 방어선".
     *  - 버스/오토바이/일반 주행 시나리오 기준이라 더 낮춰도 되지만,
     *    정상적인 고속 주행을 오탐으로 잘라내지 않도록 넉넉히 둠.
     */
    private val maxPlausibleSpeedKmh: Float = 350f,

    /**
     * 수평 위치 정확도(horizontal accuracy) 임계값(m).
     * [Location.accuracy] 가 이 값을 넘으면 저품질 fix 로 보고 거부.
     *
     * 30 으로 잡은 이유:
     *  - 개활지/도로 위에서 Fused Location 의 일반적 accuracy 는 3–15 m.
     *  - 도심 빌딩숲(urban canyon)·고가 아래·터널 진입 직후에는
     *    multipath 때문에 30 m 이상으로 악화되면서 위치·속도가 같이 튄다.
     *  - 즉 30 m 초과는 "속도도 못 믿는 상태" 의 선행 신호에 가깝다.
     *  - 너무 낮추면(예: 10) GPS 가 막 깨어난 시점의 합리적 fix 까지 버리게 됨.
     */
    private val maxAccuracyMeters: Float = 30f,

    /**
     * 속도 정확도(speed accuracy, 1σ) 임계값(m/s). Android O(API 26) 이상.
     * [Location.speedAccuracyMetersPerSecond] 가 이 값을 넘으면 거부.
     *
     * 5 m/s (≈ 18 km/h) 로 잡은 이유:
     *  - 정상 GPS fix 의 speed accuracy 는 보통 0.3–1.5 m/s.
     *  - 속도 오차가 ±18 km/h 수준이면 숫자 자체가 신뢰 불가.
     *  - 기기에서 speedAccuracy 를 제공하지 않으면 이 게이트는 자동 스킵
     *    (maxAccuracy / acceleration 게이트가 대신 커버).
     */
    private val maxSpeedAccuracyMps: Float = 5f,

    /**
     * 직전 수락값 대비 가속도 물리 한계(m/s²).
     * |Δv| / Δt 가 이 값을 넘는 샘플은 "물리적으로 불가능" 으로 보고 거부.
     *
     * 7 m/s² (≈ 0.71 G) 로 잡은 이유:
     *  - 일반 승용차 풀 가속/풀 브레이킹이 약 3–5 m/s².
     *  - 스포츠카 급제동이 약 10 m/s² (≈ 1 G).
     *  - 버스는 훨씬 낮아 1–2 m/s² 수준.
     *  - 7 은 "실제 주행에서는 거의 도달 불가 + 스파이크는 쉽게 초과" 지점.
     *    예: 30 → 2000 km/h / 1 s ≈ 547 m/s² → 자명하게 거부.
     *  - 이 게이트가 실전에서 GPS 튐을 가장 많이 잡아줌.
     */
    private val maxAccelerationMps2: Float = 7f,

    /**
     * EMA(지수가중이동평균) 계수.
     * smoothed = α·raw + (1−α)·smoothed_prev
     *
     * 0.35 로 잡은 이유:
     *  - α 가 클수록 반응은 빠르지만 노이즈 억제가 약함(=raw 에 가까움).
     *  - α 가 작을수록 부드럽지만 바늘이 실제 속도를 느리게 따라감.
     *  - 0.35 는 "1 Hz 업데이트 기준, 3–4 샘플(≈ 3–4초) 에 걸쳐 안정화"
     *    정도의 중간값. 게이지 spring 애니메이션과 겹쳐도 둔감해 보이지 않음.
     *  - 실주행 체감상 반응이 둔하면 0.5 로, 바늘이 여전히 떨리면 0.25 로 튜닝.
     */
    private val smoothingAlpha: Float = 0.35f,
) {
    private var lastAcceptedSpeedMps: Float? = null
    private var lastAcceptedTimestampMs: Long? = null
    private var smoothedSpeedMps: Float = 0f

    /** 통과 시 필터링된 m/s, 거부 시 null. */
    fun filter(rawSpeedMps: Float, location: Location): Float? {
        if (rawSpeedMps < 0f) return null
        if (rawSpeedMps * 3.6f > maxPlausibleSpeedKmh) return null

        if (location.hasAccuracy() && location.accuracy > maxAccuracyMeters) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            location.hasSpeedAccuracy() &&
            location.speedAccuracyMetersPerSecond > maxSpeedAccuracyMps
        ) return null

        val prevV = lastAcceptedSpeedMps
        val prevTs = lastAcceptedTimestampMs
        if (prevV != null && prevTs != null) {
            val dt = (location.time - prevTs) / 1000f
            if (dt > 0f && abs(rawSpeedMps - prevV) / dt > maxAccelerationMps2) return null
        }

        smoothedSpeedMps =
            if (lastAcceptedSpeedMps == null) rawSpeedMps
            else smoothingAlpha * rawSpeedMps + (1f - smoothingAlpha) * smoothedSpeedMps

        lastAcceptedSpeedMps = rawSpeedMps
        lastAcceptedTimestampMs = location.time
        return smoothedSpeedMps
    }

    fun reset() {
        lastAcceptedSpeedMps = null
        lastAcceptedTimestampMs = null
        smoothedSpeedMps = 0f
    }
}
