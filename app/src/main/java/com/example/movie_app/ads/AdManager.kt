package com.example.movie_app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.nativead.NativeAd

object AdManager {

    fun init(context: Context) {
        AdState.init(context)
    }

    fun isUnlocked(): Boolean = AdState.isUnlocked()

    fun loadInterstitialAd(context: Context, onReady: (() -> Unit)? = null) {
        InterstitialAdManager.load(context, onReady)
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        InterstitialAdManager.show(activity, onAdDismissed)
    }

    fun loadRewardedAd(context: Context, onReady: (() -> Unit)? = null) {
        RewardedAdManager.load(context, onReady)
    }

    fun showRewardedAd(activity: Activity, onUserEarnedReward: () -> Unit) {
        RewardedAdManager.show(activity, onUserEarnedReward)
    }

    fun loadNativeSmallAd(context: Context, onLoaded: ((NativeAd?) -> Unit)? = null) {
        NativeAdManager.loadNativeSmallAd(context, onLoaded)
    }

    fun loadNativeFullAd(context: Context, onLoaded: ((NativeAd?) -> Unit)? = null) {
        NativeAdManager.loadNativeFullAd(context, onLoaded)
    }

    val preloadedNativeAdSmall: NativeAd? get() = NativeAdManager.preloadedNativeAdSmall
    val preloadedNativeAdFull: NativeAd? get() = NativeAdManager.preloadedNativeAdFull

    fun destroyAds() {
        InterstitialAdManager.destroy()
        RewardedAdManager.destroy()
        NativeAdManager.destroy()
    }
}
