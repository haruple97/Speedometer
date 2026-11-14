package com.haruple97.speedometer

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LifecycleEventObserver
import android.Manifest
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.haruple97.speedometer.data.ads.AdConsentManager
import com.haruple97.speedometer.data.ads.MobileAdsInitializer
import com.haruple97.speedometer.data.database.DatabaseProvider
import com.haruple97.speedometer.data.location.LocationRepositoryProvider
import com.haruple97.speedometer.data.settings.SettingsRepository
import com.haruple97.speedometer.data.trip.TripRecorderProvider
import com.haruple97.speedometer.data.trip.TripRecoveryService
import com.haruple97.speedometer.util.AppScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.navigation.SpeedometerAppNavHost
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray

class MainActivity : ComponentActivity() {

    private val isInPipMode = mutableStateOf(false)

    private val pipParams = PictureInPictureParams.Builder()
        .setAspectRatio(Rational(1, 1))
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        observeKeepScreenOn()
        observeAutoRecording()
        recoverDanglingTrips()
        initializeAds()
        setContent {
            SpeedometerTheme {
                LocationPermissionGate(isInPipMode = isInPipMode.value)
            }
        }
    }

    /** UMP 동의 수집 이후 MobileAds 초기화. canRequestAds 가 true 로 확정돼야 광고 요청 허용됨. */
    private fun initializeAds() {
        AdConsentManager(this).gatherConsent {
            MobileAdsInitializer.initialize(applicationContext)
        }
    }

    private fun observeAutoRecording() {
        val settingsRepo = SettingsRepository(applicationContext)
        val recorder = TripRecorderProvider.get(applicationContext)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsRepo.preferencesFlow
                    .map { it.autoRecordingEnabled }
                    .distinctUntilChanged()
                    .collect { recorder.setAutoRecordingEnabled(it) }
            }
        }
    }

    private fun recoverDanglingTrips() {
        val dao = DatabaseProvider.get(applicationContext).tripDao()
        AppScope.scope.launch {
            TripRecoveryService(dao).recoverDanglingTrips()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // finalize 는 AppScope 에서 돌기 때문에 Activity 파괴 후에도 Room write 완료 가능
            TripRecorderProvider.get(applicationContext).finalizeIfRunning()
        }
    }

    private fun observeKeepScreenOn() {
        val repository = SettingsRepository(applicationContext)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.preferencesFlow
                    .map { it.keepScreenOn }
                    .distinctUntilChanged()
                    .collect { enabled ->
                        if (enabled) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (hasPreciseLocation()) enterPictureInPictureMode(pipParams)
    }

    private fun appOpenAdManager() =
        (application as SpeedometerApplication).appOpenAdManager

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        val wasInPip = isInPipMode.value
        isInPipMode.value = isInPictureInPictureMode
        // PiP 종료 직후 복귀 세션은 App Open 광고 보호.
        if (wasInPip && !isInPictureInPictureMode) {
            appOpenAdManager().markJustReturnedFromPip()
        }
    }
}

private fun android.content.Context.hasPreciseLocation(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@Composable
private fun LocationPermissionGate(isInPipMode: Boolean) {
    val activity = androidx.compose.ui.platform.LocalContext.current as MainActivity
    val requestHistory = remember { activity.getSharedPreferences("location_permission", 0) }
    var hasPermission by remember { mutableStateOf(activity.hasPreciseLocation()) }
    var needsSettings by remember { mutableStateOf(false) }

    fun refreshPermission() {
        val granted = activity.hasPreciseLocation()
        if (granted && !hasPermission) {
            (activity.applicationContext as SpeedometerApplication)
                .appOpenAdManager.markPermissionJustGranted()
        }
        hasPermission = granted
        needsSettings = !granted && requestHistory.getBoolean("requested", false) &&
            !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { refreshPermission() }

    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshPermission()
        }
        activity.lifecycle.addObserver(observer)
        refreshPermission()
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && !requestHistory.getBoolean("requested", false)) {
            requestHistory.edit().putBoolean("requested", true).apply()
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }

    if (hasPermission) {
        // 권한 승인 후 구독해야 최초 승인 직후에도 위치 요청이 정상 시작된다.
        LaunchedEffect(activity) {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val repo = LocationRepositoryProvider.get(activity.applicationContext)
                val recorder = TripRecorderProvider.get(activity.applicationContext)
                repo.speedFlow.collect { recorder.onSample(it) }
            }
        }
        SpeedometerAppNavHost(isInPipMode = isInPipMode)
    } else {
        PermissionRequestScreen(
            needsSettings = needsSettings,
            onRequestPermission = {
                if (needsSettings) {
                    activity.startActivity(Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${activity.packageName}"),
                    ))
                } else {
                    requestHistory.edit().putBoolean("requested", true).apply()
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ))
                }
            }
        )
    }
}

@Composable
private fun PermissionRequestScreen(
    needsSettings: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBlack)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = SpeedometerTextStyle.Data2Style(),
            color = DigitalWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(if (needsSettings) R.string.perm_settings_body else R.string.perm_body),
            style = SpeedometerTextStyle.H4RegularStyle(),
            color = UnitGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = GaugeSafe
            )
        ) {
            Text(
                text = stringResource(if (needsSettings) R.string.perm_settings_cta else R.string.perm_grant_cta),
                color = DashboardBlack
            )
        }
    }
}
