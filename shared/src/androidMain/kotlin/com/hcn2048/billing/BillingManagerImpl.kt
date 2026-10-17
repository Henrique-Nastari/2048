package com.hcn2048.billing

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.hcn2048.util.ActivityProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Play Billing 9 implementation of the single non-consumable "remove ads" product.
 *
 * Flow: query product details (localized price) + query existing purchases on connect;
 * launching the purchase UI needs a resumed activity (kept by [ActivityProvider]).
 * Purchases are acknowledged immediately so they are never auto-refunded.
 */
class BillingManagerImpl(context: Context) : BillingManager {

    private val _owned = MutableStateFlow(false)
    override val owned: StateFlow<Boolean> = _owned.asStateFlow()

    private val _priceLabel = MutableStateFlow<String?>(null)
    override val priceLabel: StateFlow<String?> = _priceLabel.asStateFlow()

    private var productDetails: ProductDetails? = null
    private var pendingResult: ((BillingOutcome) -> Unit)? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) purchases.forEach(::handlePurchase)
                pendingResult?.invoke(BillingOutcome.Success)
            }

            BillingClient.BillingResponseCode.USER_CANCELED ->
                pendingResult?.invoke(BillingOutcome.Canceled)

            else ->
                pendingResult?.invoke(BillingOutcome.Error(result.debugMessage))
        }
        pendingResult = null
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .enableAutoServiceReconnection()
        .build()

    init {
        connect()
    }

    override fun refresh() {
        if (client.isReady) {
            queryProductDetails()
            queryExistingPurchases()
        } else {
            connect()
        }
    }

    override fun purchaseRemoveAds(onResult: (BillingOutcome) -> Unit) {
        val activity = ActivityProvider.current
        if (activity == null) {
            onResult(BillingOutcome.Error("No resumed activity"))
            return
        }
        val details = productDetails
        if (details == null) {
            queryProductDetails()
            onResult(BillingOutcome.Unavailable)
            return
        }
        val offerToken = details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        if (!offerToken.isNullOrBlank()) {
            productParamsBuilder.setOfferToken(offerToken)
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()
        pendingResult = onResult
        val launchResult = client.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            pendingResult = null
            onResult(BillingOutcome.Error(launchResult.debugMessage))
        }
    }

    override fun restorePurchases(onResult: (BillingOutcome) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach(::handlePurchase)
                val ownedNow = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        it.products.contains(PRODUCT_ID)
                }
                onResult(if (ownedNow) BillingOutcome.Success else BillingOutcome.NotFound)
            } else {
                onResult(BillingOutcome.Error(result.debugMessage))
            }
        }
    }

    private fun connect() {
        client.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryProductDetails()
                        queryExistingPurchases()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Automatic service reconnection is enabled; nothing to do.
                }
            },
        )
    }

    private fun queryProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        client.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = queryResult.productDetailsList.firstOrNull()
                _priceLabel.value = productDetails
                    ?.oneTimePurchaseOfferDetailsList
                    ?.firstOrNull()
                    ?.formattedPrice
            }
        }
    }

    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach(::handlePurchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.products.contains(PRODUCT_ID)) return
        _owned.value = true
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            client.acknowledgePurchase(params) { }
        }
    }

    companion object {
        const val PRODUCT_ID = "remove_ads"
    }
}
