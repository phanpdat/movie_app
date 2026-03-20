package com.example.movie_app.data.repository

import android.app.Activity
import com.example.movie_app.util.Constants
import com.example.movie_app.util.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FakeBillingRepository(
    private val userPreferences: UserPreferences
) : BillingRepository {

    private val _purchaseEvent = MutableStateFlow<PurchaseResult?>(null)
    override val purchaseEvent: StateFlow<PurchaseResult?> = _purchaseEvent

    override fun buyPremium(activity: Activity, productId: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            userPreferences.setPremiumStatus(true)
            onResult(true)
            _purchaseEvent.value = PurchaseResult.Success
        }
    }

    override fun restorePurchase(onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
            val result = userPreferences.isPremium.value
            onResult(result)
        }
    }

    override fun isPremium(): StateFlow<Boolean> = userPreferences.isPremium
    
    override fun queryPurchases() {
        // Mock
    }

    override fun getProductPrice(productId: String): StateFlow<String?> {
        return MutableStateFlow(
            when(productId) {
                Constants.PRODUCT_WEEKLY -> "$1.99"
                Constants.PRODUCT_MONTHLY -> "$4.99"
                Constants.PRODUCT_YEARLY -> "$19.99"
                else -> "$4.99"
            }
        )
    }
}
