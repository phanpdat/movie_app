package com.example.movie_app.ui.subscription

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movie_app.R
import com.example.movie_app.data.repository.FakeBillingRepository
import com.example.movie_app.databinding.FragmentSubscriptionBinding
import com.example.movie_app.util.UserPreferences
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SubscriptionFragment : Fragment(R.layout.fragment_subscription) {

    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SubscriptionViewModel
    private var selectedProductId: String = com.example.movie_app.util.Constants.PRODUCT_MONTHLY

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSubscriptionBinding.bind(view)

        val userPrefs = UserPreferences.getInstance(requireContext())
        val repository = FakeBillingRepository(userPrefs)
        val factory = SubscriptionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SubscriptionViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnStartAction.setOnClickListener {
            viewModel.buyPremium(selectedProductId)
        }

        binding.tvRestore.setOnClickListener {
            viewModel.restorePurchase()
        }

        setupPlanSelection()
    }

    private fun setupPlanSelection() {
        val plans = listOf(binding.planBasic, binding.planPremium, binding.planUltimate)
        
        plans.forEach { plan ->
            plan.setOnClickListener {
                selectedProductId = when(plan.id) {
                    R.id.planBasic -> com.example.movie_app.util.Constants.PRODUCT_WEEKLY
                    R.id.planPremium -> com.example.movie_app.util.Constants.PRODUCT_MONTHLY
                    R.id.planUltimate -> com.example.movie_app.util.Constants.PRODUCT_YEARLY
                    else -> com.example.movie_app.util.Constants.PRODUCT_MONTHLY
                }
                updateUI(plan.id)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.purchaseState.collect { state ->
                when (state) {
                    is PurchaseState.Loading -> {
                        binding.btnStartAction.isEnabled = false
                        binding.btnStartAction.text = "Processing..."
                    }
                    is PurchaseState.Success -> {
                        Snackbar.make(binding.root, "Premium Activated!", Snackbar.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    is PurchaseState.Error -> {
                        binding.btnStartAction.isEnabled = true
                        binding.btnStartAction.text = "Get Started Now"
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    is PurchaseState.Idle -> {
                        binding.btnStartAction.isEnabled = true
                        binding.btnStartAction.text = "Get Started Now"
                    }
                }
            }
        }
    }

    private fun updateUI(selectedId: Int) {
        // Basic
        binding.planBasic.setBackgroundResource(
            if (selectedId == R.id.planBasic) R.drawable.bg_subscription_card_selected 
            else R.drawable.bg_subscription_card
        )
        // Premium
        binding.planPremium.setBackgroundResource(
            if (selectedId == R.id.planPremium) R.drawable.bg_subscription_card_selected 
            else R.drawable.bg_subscription_card
        )
        // Ultimate
        binding.planUltimate.setBackgroundResource(
            if (selectedId == R.id.planUltimate) R.drawable.bg_subscription_card_selected 
            else R.drawable.bg_subscription_card
        )
        
        // Update radio icons
        binding.radioBasic.setImageResource(
            if (selectedId == R.id.planBasic) R.drawable.ic_sv_radio_selected 
            else R.drawable.ic_sv_radio_unselected
        )
        binding.radioPremium.setImageResource(
            if (selectedId == R.id.planPremium) R.drawable.ic_sv_radio_selected 
            else R.drawable.ic_sv_radio_unselected
        )
        binding.radioUltimate.setImageResource(
            if (selectedId == R.id.planUltimate) R.drawable.ic_sv_radio_selected 
            else R.drawable.ic_sv_radio_unselected
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
