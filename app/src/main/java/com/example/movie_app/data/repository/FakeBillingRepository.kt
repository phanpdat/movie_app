package com.example.movie_app.data.repository

import com.example.movie_app.util.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FakeBillingRepository(
    private val userPreferences: UserPreferences
) : BillingRepository {

    override fun buyPremium(productId: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            userPreferences.setPremiumStatus(true)
            onResult(true)
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
}
