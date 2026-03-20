package com.example.movie_app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
    private const val TAG = "InterstitialAdManager"
    private var interstitialAd: InterstitialAd? = null
    var isLoading = false

    fun load(context: Context, onReady: (() -> Unit)? = null) {
        if (interstitialAd != null || isLoading) {
            onReady?.invoke()
            return
        }

        isLoading = true
        Log.d(TAG, "Starting Interstitial Ad load...")

        val adRequest = AdRequest.Builder().build()
        val adUnitId = AdUtils.getAdUnitId(context, "interstitial_id")

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Interstitial Ad failed to load: ${adError.message}")
                    interstitialAd = null
                    isLoading = false
                    onReady?.invoke()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad loaded successfully!")
                    interstitialAd = ad
                    isLoading = false
                    onReady?.invoke()
                }
            })
    }

    fun show(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    load(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "Interstitial Ad failed to show")
                    interstitialAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showing")
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad not ready yet, skipping")
            onAdDismissed()
            load(activity)
        }
    }

    fun destroy() {
        interstitialAd = null
    }
}
