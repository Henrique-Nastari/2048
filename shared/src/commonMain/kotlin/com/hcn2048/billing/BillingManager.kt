package com.hcn2048.billing

import kotlinx.coroutines.flow.StateFlow

/** Result of a purchase or restore attempt, mapped to user-facing messages by the UI. */
sealed interface BillingOutcome {
    data object Success : BillingOutcome
    data object Canceled : BillingOutcome
    data object NotFound : BillingOutcome

    /** Product details were not loaded yet (transient); user can retry. */
    data object Unavailable : BillingOutcome

    data class Error(val debugMessage: String?) : BillingOutcome
}

/**
 * In-app purchases facade implemented per platform (Play Billing on Android).
 * The only product is the non-consumable "remove ads" one-time purchase.
 */
interface BillingManager {

    /** True when the remove-ads product is owned (also restored across installs). */
    val owned: StateFlow<Boolean>

    /** Localized price for the remove-ads product, when available. */
    val priceLabel: StateFlow<String?>

    /** Re-queries product details and existing purchases. */
    fun refresh()

    fun purchaseRemoveAds(onResult: (BillingOutcome) -> Unit)

    fun restorePurchases(onResult: (BillingOutcome) -> Unit)
}
