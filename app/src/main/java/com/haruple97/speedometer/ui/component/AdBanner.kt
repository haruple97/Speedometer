package com.haruple97.speedometer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.haruple97.speedometer.data.ads.MobileAdsInitializer
import com.haruple97.speedometer.ui.theme.DashboardDarkGray

/**
 * AdMob Adaptive Banner Compose 래퍼.
 *
 * - 실측 폭(`BoxWithConstraints.maxWidth`) 으로 Adaptive 사이즈 계산.
 * - SDK 초기화 전엔 빈 영역으로 자리만 유지(레이아웃 점프 방지).
 * - [DisposableEffect] 로 AdView.destroy() 보장.
 */
@Composable
fun AdBanner(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val initialized by MobileAdsInitializer.initialized.collectAsStateWithLifecycle()

    var adView by remember { mutableStateOf<AdView?>(null) }
    DisposableEffect(Unit) {
        onDispose { adView?.destroy() }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(DashboardDarkGray),
    ) {
        if (!initialized) return@BoxWithConstraints

        val widthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adSize = remember(widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
            factory = { ctx ->
                AdView(ctx).also { view ->
                    view.setAdSize(adSize)
                    view.adUnitId = adUnitId
                    view.loadAd(AdRequest.Builder().build())
                    adView = view
                }
            },
        )
    }
}
