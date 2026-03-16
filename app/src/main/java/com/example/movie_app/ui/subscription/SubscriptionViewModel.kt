package com.example.movie_app.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_app.data.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    val isPremium: StateFlow<Boolean> = billingRepository.isPremium()

    fun buyPremium(productId: String) {
        _purchaseState.value = PurchaseState.Loading
        billingRepository.buyPremium(productId) { success ->
            if (success) {
                _purchaseState.value = PurchaseState.Success
            } else {
                _purchaseState.value = PurchaseState.Error("Purchase failed")
            }
        }
    }

    fun restorePurchase() {
        _purchaseState.value = PurchaseState.Loading
        billingRepository.restorePurchase { success ->
            if (success) {
                _purchaseState.value = PurchaseState.Success
            } else {
                _purchaseState.value = PurchaseState.Error("No previous purchase found")
            }
        }
    }

    fun resetState() {
        _purchaseState.value = PurchaseState.Idle
    }
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    object Success : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
