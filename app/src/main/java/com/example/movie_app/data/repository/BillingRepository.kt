package com.example.movie_app.data.repository

import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    fun buyPremium(productId: String, onResult: (Boolean) -> Unit)
    fun restorePurchase(onResult: (Boolean) -> Unit)
    fun isPremium(): StateFlow<Boolean>
}
