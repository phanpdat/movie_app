package com.example.movie_app.data.repository

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    fun buyPremium(activity: Activity, productId: String, onResult: (Boolean) -> Unit)
    fun restorePurchase(onResult: (Boolean) -> Unit)
    fun isPremium(): StateFlow<Boolean>
    fun getProductPrice(productId: String): StateFlow<String?>
    fun queryPurchases()
    val purchaseEvent: StateFlow<PurchaseResult?>
}

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}
