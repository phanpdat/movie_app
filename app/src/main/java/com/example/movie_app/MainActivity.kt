package com.example.movie_app

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.movie_app.data.api.RetrofitClient
import com.example.movie_app.ads.AdManager
import com.example.movie_app.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
       // Initialize Ads
        MobileAds.initialize(this) {}
        AdManager.init(this)
        AdManager.loadInterstitialAd(this)
        AdManager.loadRewardedAd(this)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNav.setupWithNavController(navController)

        // Hide bottom nav in splash screen or detail screens (if any later)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = when (destination.id) {
                R.id.homeFragment, R.id.searchFragment, R.id.favoriteFragment -> View.VISIBLE
                else -> View.GONE
            }
        }
    }
}
