package com.hcn2048.ads

import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hcn2048.util.ActivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AdMob implementation. Interstitial and rewarded ads are preloaded as soon as consent
 * allows, and reloaded right after each display to keep eCPM high.
 */
class AdManagerImpl(
    private val context: Context,
    private val adIds: AdIds,
) : AdManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _privacyOptionsRequired = MutableStateFlow(false)
    override val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null
    private var loadingInterstitial = false
    private var loadingRewarded = false
    private var adsInitialized = false

    init {
        scope.launch {
            AdsConsent.adsReady.collect { ready ->
                if (ready && !adsInitialized) {
                    adsInitialized = true
                    _privacyOptionsRequired.value = AdsConsent.privacyOptionsRequired()
                    preload()
                }
            }
        }
    }

    override fun preload() {
        if (!adsInitialized) return
        loadInterstitial()
        loadRewarded()
    }

    override fun showInterstitial(onDismiss: () -> Unit) {
        val ad = interstitial
        val activity = ActivityProvider.current
        if (ad == null || activity == null) {
            loadInterstitial()
            onDismiss()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                loadInterstitial()
                onDismiss()
            }
        }
        ad.show(activity)
    }

    override fun showRewarded(
        onReward: () -> Unit,
        onDismiss: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        val ad = rewarded
        val activity = ActivityProvider.current
        if (ad == null || activity == null) {
            loadRewarded()
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                loadRewarded()
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewarded = null
                loadRewarded()
                onDismiss()
            }
        }
        ad.show(activity) { onReward() }
    }

    override fun showPrivacyOptionsForm() {
        val activity = ActivityProvider.current ?: return
        if (AdsConsent.privacyOptionsRequired()) {
            AdsConsent.showPrivacyOptionsForm(activity)
        }
    }

    private fun loadInterstitial() {
        if (interstitial != null || loadingInterstitial) return
        loadingInterstitial = true
        InterstitialAd.load(
            context,
            adIds.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadingInterstitial = false
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingInterstitial = false
                    interstitial = null
                }
            },
        )
    }

    private fun loadRewarded() {
        if (rewarded != null || loadingRewarded) return
        loadingRewarded = true
        RewardedAd.load(
            context,
            adIds.rewarded,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadingRewarded = false
                    rewarded = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingRewarded = false
                    rewarded = null
                }
            },
        )
    }
}
