package com.haruple97.speedometer.data.location

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOn
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.haruple97.speedometer.data.model.SpeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class DefaultLocationRepository(
    context: Context,
    scope: CoroutineScope,
) : LocationRepository {

    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
    private val rawSpeedFlow: Flow<SpeedData> = callbackFlow {
        var lastFixElapsedMs: Long? = null
        var requested = false
        fun invalidate() {
            lastFixElapsedMs = null
            lastLocation = null
            filter.reset()
            // timestamp=0: 신호 상실을 정지 주행 샘플로 기록하지 않는다.
            trySend(SpeedData())
        }
        invalidate()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val now = SystemClock.elapsedRealtime()
                val fixElapsedMs = location.elapsedRealtimeNanos / 1_000_000L
                if (!LocationManagerCompat.isLocationEnabled(locationManager) ||
                    !isLocationFresh(now, fixElapsedMs)) return
                if (lastFixElapsedMs != null && !isLocationFresh(now, lastFixElapsedMs)) invalidate()
                if (lastFixElapsedMs != null && fixElapsedMs <= lastFixElapsedMs!!) return
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
                lastFixElapsedMs = fixElapsedMs

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

        fun requestUpdates() {
            if (requested || !LocationManagerCompat.isLocationEnabled(locationManager) ||
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) return
            requested = true
            try {
                fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
                    .addOnFailureListener {
                        requested = false
                        invalidate()
                    }
            } catch (_: SecurityException) {
                requested = false
                invalidate()
            }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                    invalidate()
                } else {
                    requestUpdates()
                }
            }
        }
        ContextCompat.registerReceiver(appContext, receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
        requestUpdates()
        val watchdog = launch {
            while (true) {
                delay(1_000L)
                if (lastFixElapsedMs != null &&
                    !isLocationFresh(SystemClock.elapsedRealtime(), lastFixElapsedMs)) invalidate()
                requestUpdates()
            }
        }
        awaitClose {
            watchdog.cancel()
            appContext.unregisterReceiver(receiver)
            fusedClient.removeLocationUpdates(callback)
            lastLocation = null
            filter.reset()
        }
    }.flowOn(Dispatchers.Main.immediate)

    override val speedFlow: Flow<SpeedData> = rawSpeedFlow.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000L, replayExpirationMillis = 0L),
        replay = 1,
    )

    override fun startTracking() {
        // Flow 수집 시 자동으로 시작됨 (callbackFlow)
    }

    override fun stopTracking() {
        // Flow 수집 취소 시 awaitClose에서 자동 정리
        lastLocation = null
        filter.reset()
    }
}
