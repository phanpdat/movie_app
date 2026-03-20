package com.example.movie_app.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_app.data.repository.BillingRepository
import com.example.movie_app.data.repository.PurchaseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    val isPremium: StateFlow<Boolean> = billingRepository.isPremium()

    init {
        viewModelScope.launch {
            billingRepository.purchaseEvent.collect { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        _purchaseState.value = PurchaseState.Success
                    }
                    is PurchaseResult.Cancelled -> {
                        _purchaseState.value = PurchaseState.Idle
                    }
                    is PurchaseResult.Error -> {
                        _purchaseState.value = PurchaseState.Error(result.message)
                    }
                    null -> {}
                }
            }
        }
    }

    fun getProductPrice(productId: String): StateFlow<String?> {
        return billingRepository.getProductPrice(productId)
    }

    fun buyPremium(activity: android.app.Activity, productId: String) {
        billingRepository.buyPremium(activity, productId) { success ->
            if (!success) {
                _purchaseState.value = PurchaseState.Error("Failed to launch billing flow")
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

    // fun resetState() {
    //     _purchaseState.value = PurchaseState.Idle
    // }
}

sealed class PurchaseState {
    data object Idle : PurchaseState()
    data object Loading : PurchaseState()
    data object Success : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
