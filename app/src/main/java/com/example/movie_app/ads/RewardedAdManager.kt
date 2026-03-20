package com.example.movie_app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {
    private const val TAG = "RewardedAdManager"
    private var rewardedAd: RewardedAd? = null
    var isLoading = false

    fun load(context: Context, onReady: (() -> Unit)? = null) {
        if (rewardedAd != null || isLoading) {
            onReady?.invoke()
            return
        }

        isLoading = true
        Log.d(TAG, "Starting Rewarded Ad load...")

        val adRequest = AdRequest.Builder().build()
        val adUnitId = AdUtils.getAdUnitId(context, "rewarded_ad_id")

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Rewarded Ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isLoading = false
                    onReady?.invoke()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad loaded successfully!")
                    rewardedAd = ad
                    isLoading = false
                    onReady?.invoke()
                }
            })
    }

    fun show(activity: Activity, onUserEarnedReward: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed")
                    rewardedAd = null
                    load(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "Rewarded Ad failed to show")
                    rewardedAd = null
                    load(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad showing")
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                AdState.setUnlocked()
                onUserEarnedReward()
            }
        } else {
            Log.d(TAG, "Rewarded Ad not ready yet")
            load(activity)
        }
    }

    fun destroy() {
        rewardedAd = null
    }
}
