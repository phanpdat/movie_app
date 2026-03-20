package com.example.movie_app.ui.splash

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movie_app.BuildConfig
import com.example.movie_app.R
import com.example.movie_app.data.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getPopularMovies(BuildConfig.TMDB_API_KEY)
                Log.d("API_TEST", "=================================")
                Log.d("API_TEST", "API Test Successful!")
                Log.d("API_TEST", "Total Pages: ${response.total_pages}")
                Log.d("API_TEST", "First Movie Title: ${response.results.firstOrNull()?.title}")
                Log.d("API_TEST", "=================================")
            } catch (e: Exception) {
                Log.e("API_TEST", "API Test Failed: ${e.message}")
                requireActivity().runOnUiThread {
                    android.widget.Toast.makeText(context, "Network Error: Please check your internet connection", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            delay(5000)           
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        }
    }
}
