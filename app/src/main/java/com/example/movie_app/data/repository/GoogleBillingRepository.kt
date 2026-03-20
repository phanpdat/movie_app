package com.example.movie_app.data.repository

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.example.movie_app.util.Constants
import com.example.movie_app.util.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoogleBillingRepository private constructor(private val context: Context) : BillingRepository, PurchasesUpdatedListener {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: GoogleBillingRepository? = null

        fun getInstance(context: Context): GoogleBillingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoogleBillingRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val TAG = "GoogleBilling"
    private val userPreferences: UserPreferences = UserPreferences.getInstance(context)

    private val _productPrices = mutableMapOf<String, MutableStateFlow<String?>>()
    private val _purchaseEvent = MutableStateFlow<PurchaseResult?>(null)
    override val purchaseEvent: StateFlow<PurchaseResult?> = _purchaseEvent.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        startConnection()
    }

    override fun isPremium(): StateFlow<Boolean> = userPreferences.isPremium

    override fun getProductPrice(productId: String): StateFlow<String?> {
        return _productPrices.getOrPut(productId) { MutableStateFlow(null) }.asStateFlow()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Setup OK")
                    queryPurchases()
                    fetchProductPrices()
                }
            }

            override fun onBillingServiceDisconnected() {
                startConnection()
            }
        })
    }

    private fun fetchProductPrices() {
        val productIds = listOf(
            Constants.PRODUCT_WEEKLY,
            Constants.PRODUCT_MONTHLY,
            Constants.PRODUCT_YEARLY
        )

        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { details ->
                    val price = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    val stateFlow = _productPrices.getOrPut(details.productId) { MutableStateFlow(null) }
                    stateFlow.value = price
                    Log.d(TAG,price.toString());
                }
            }
        }
    }

    override fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                userPreferences.setPremiumStatus(hasPremium)
            }
        }
    }

    override fun buyPremium(activity: Activity, productId: String, onResult: (Boolean) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""

                val billingParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                        )
                    )
                    .build()

                val flowResult = billingClient.launchBillingFlow(activity, billingParams)
                if (flowResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    onResult(false)
                    _purchaseEvent.value = PurchaseResult.Error("Billing flow failed: ${flowResult.debugMessage}")
                } else {
                    onResult(true)
                }
            } else {
                onResult(false)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                    _purchaseEvent.value = PurchaseResult.Success
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled the purchase")
                _purchaseEvent.value = PurchaseResult.Cancelled
            }
            else -> {
                Log.e(TAG, "Billing error: ${billingResult.debugMessage}")
                _purchaseEvent.value = PurchaseResult.Error(billingResult.debugMessage)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    userPreferences.setPremiumStatus(true)
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            userPreferences.setPremiumStatus(true)
        }
    }

    override fun restorePurchase(onResult: (Boolean) -> Unit) {
        queryPurchases()
        onResult(true)
    }
}
