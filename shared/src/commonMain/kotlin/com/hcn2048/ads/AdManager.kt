package com.hcn2048.ads

import kotlinx.coroutines.flow.StateFlow

/**
 * Ads facade implemented per platform (AdMob on Android).
 * All calls are safe no-ops when ads are not available.
 */
interface AdManager {

    /** Whether the privacy options entry point must be visible (GDPR/EEA users). */
    val privacyOptionsRequired: StateFlow<Boolean>

    /** Starts loading formats ahead of time. */
    fun preload()

    fun showInterstitial(onDismiss: () -> Unit = {})

    fun showRewarded(
        onReward: () -> Unit,
        onDismiss: () -> Unit = {},
        onUnavailable: () -> Unit = {},
    )

    /** Opens the UMP privacy options form when required. */
    fun showPrivacyOptionsForm()
}
