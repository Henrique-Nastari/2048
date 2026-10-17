package com.hcn2048.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import org.koin.core.context.GlobalContext

/**
 * AdMob banner. Only composed after consent + SDK initialization, so it never
 * requests ads too early (which would silently fail).
 */
@Composable
actual fun AdBanner(modifier: Modifier) {
    val adsReady by AdsConsent.adsReady.collectAsState()
    if (!adsReady) return
    val adIds: AdIds = remember { GlobalContext.get().get() }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = adIds.banner
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
