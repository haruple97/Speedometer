package com.haruple97.speedometer.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.haruple97.speedometer.data.model.SpeedData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DefaultLocationRepository(context: Context) : LocationRepository {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L
    )
        .setMinUpdateIntervalMillis(500L)
        .build()

    private var lastLocation: Location? = null
    private val filter = GpsSpeedFilter()

    @SuppressLint("MissingPermission")
    override val speedFlow: Flow<SpeedData> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val prev = lastLocation
                val deltaDistance = if (prev != null) prev.distanceTo(location) else 0f

                val rawSpeedMps = if (location.hasSpeed()) {
                    location.speed
                } else {
                    // Fallback: 두 지점간 거리/시간으로 속도 계산
                    if (prev != null) {
                        val timeDiff = (location.time - prev.time) / 1000f
                        if (timeDiff > 0) deltaDistance / timeDiff else 0f
                    } else {
                        0f
                    }
                }

                val filteredMps = filter.filter(rawSpeedMps, location) ?: return
                lastLocation = location

                trySend(
                    SpeedData(
                        speedKmh = filteredMps * 3.6f,
                        accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
                        isGpsActive = true,
                        timestamp = location.time,
                        deltaDistanceMeters = deltaDistance,
                    )
                )
            }
        }

        fusedClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    override fun startTracking() {
        // Flow 수집 시 자동으로 시작됨 (callbackFlow)
    }

    override fun stopTracking() {
        // Flow 수집 취소 시 awaitClose에서 자동 정리
        lastLocation = null
        filter.reset()
    }
}
