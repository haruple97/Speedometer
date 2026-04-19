package com.haruple97.speedometer.ui.component.history

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.haruple97.speedometer.R

/**
 * Native Advanced 광고 카드 Composable 래퍼.
 *
 * AdMob 네이티브는 클릭 추적을 위해 `NativeAdView` 루트 + 등록된 자산 자식 뷰 구조를 요구하므로
 * Compose 내에서 직접 렌더할 수 없다. 다크톤 카드 XML (`R.layout.native_ad_card`) 를
 * 인플레이트한 뒤 각 자산을 바인딩하고 `setNativeAd` 로 연결.
 *
 * 자산이 null 인 경우 해당 뷰는 GONE 처리해 레이아웃 붕괴 방지.
 */
@Composable
fun NativeAdCard(
    ad: NativeAd,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.native_ad_card, null) as NativeAdView
            bindNativeAd(view, ad)
            view
        },
        update = { view -> bindNativeAd(view, ad) },
    )
}

private fun bindNativeAd(adView: NativeAdView, ad: NativeAd) {
    val headline = adView.findViewById<TextView>(R.id.native_ad_headline).also {
        it.text = ad.headline
    }
    adView.headlineView = headline

    val iconView = adView.findViewById<ImageView>(R.id.native_ad_icon)
    val iconDrawable = ad.icon?.drawable
    if (iconDrawable != null) {
        iconView.setImageDrawable(iconDrawable)
        iconView.visibility = View.VISIBLE
        adView.iconView = iconView
    } else {
        iconView.visibility = View.GONE
    }

    val bodyView = adView.findViewById<TextView>(R.id.native_ad_body)
    if (ad.body != null) {
        bodyView.text = ad.body
        bodyView.visibility = View.VISIBLE
        adView.bodyView = bodyView
    } else {
        bodyView.visibility = View.GONE
    }

    // 본문 Row 전체가 공백이면 숨김
    val bodyRow = adView.findViewById<View>(R.id.native_ad_body_row)
    bodyRow.visibility = if (iconDrawable == null && ad.body == null) View.GONE else View.VISIBLE

    val mediaView = adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(R.id.native_ad_media)
    val mediaContent = ad.mediaContent
    if (mediaContent != null) {
        mediaView.mediaContent = mediaContent
        mediaView.visibility = View.VISIBLE
        adView.mediaView = mediaView
    } else {
        mediaView.visibility = View.GONE
    }

    val ctaView = adView.findViewById<Button>(R.id.native_ad_cta)
    if (ad.callToAction != null) {
        ctaView.text = ad.callToAction
        ctaView.visibility = View.VISIBLE
        adView.callToActionView = ctaView
    } else {
        ctaView.visibility = View.GONE
    }

    // 마지막에 연결 — 반드시 자산 등록 후에 호출.
    adView.setNativeAd(ad)
}
