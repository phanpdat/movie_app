package com.example.movie_app.ads
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.movie_app.util.UserPreferences
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


object AdManager {

    private const val TAG = "AdManager"
    private fun getAdUnitId(context: Context, key: String): String {
        return try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val bundle = ai.metaData
            bundle?.getString(key) ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error reading $key from Manifest", e)
            ""
        }
    }

    // Preloaded ad references
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    var preloadedNativeAdSmall: NativeAd? = null
    var preloadedNativeAdFull: NativeAd? = null

    // LOADING STATES
    var isInterstitialLoading = false
    var isRewardedLoading = false
    var isNativeSmallLoading = false
    var isNativeFullLoading = false

    // PENDING CALLBACK QUEUES
    private val nativeSmallAdCallbacks = mutableListOf<(NativeAd?) -> Unit>()
    private val nativeFullAdCallbacks = mutableListOf<(NativeAd?) -> Unit>()

    // UNLOCK LOGIC (1 minute duration)
    private var lastUnlockTime: Long = 0
    private const val UNLOCK_DURATION = 60 * 1000 
    private var userPreferences: UserPreferences? = null

    fun init(context: Context) {
        userPreferences = UserPreferences.getInstance(context)
    }

    fun isUnlocked(): Boolean {
        if (userPreferences?.isPremium?.value == true) return true
        return System.currentTimeMillis() - lastUnlockTime < UNLOCK_DURATION
    }

    private fun setUnlocked() {
        lastUnlockTime = System.currentTimeMillis()
    }

    fun loadInterstitialAd(context: Context, onReady: (() -> Unit)? = null) {
        if (interstitialAd != null || isInterstitialLoading) {
            onReady?.invoke()
            return
        }

        isInterstitialLoading = true
        Log.d(TAG, "Starting Interstitial Ad load...")

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            getAdUnitId(context, "interstitial_id"),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Interstitial Ad failed to load: ${adError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                    onReady?.invoke()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad loaded successfully!")
                    interstitialAd = ad
                    isInterstitialLoading = false
                    onReady?.invoke()
                }
            })
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    loadInterstitialAd(activity)
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
            loadInterstitialAd(activity)
        }
    }

    fun loadRewardedAd(context: Context, onReady: (() -> Unit)? = null) {
        if (rewardedAd != null || isRewardedLoading) {
            onReady?.invoke()
            return
        }

        isRewardedLoading = true
        Log.d(TAG, "Starting Rewarded Ad load...")

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            getAdUnitId(context, "rewarded_ad_id"),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Rewarded Ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isRewardedLoading = false
                    onReady?.invoke()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad loaded successfully!")
                    rewardedAd = ad
                    isRewardedLoading = false
                    onReady?.invoke()
                }
            })
    }

    fun showRewardedAd(activity: Activity, onUserEarnedReward: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed")
                    rewardedAd = null
                    loadRewardedAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "Rewarded Ad failed to show")
                    rewardedAd = null
                    loadRewardedAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad showing")
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                setUnlocked()
                onUserEarnedReward()
            }
        } else {
            Log.d(TAG, "Rewarded Ad not ready yet")
            loadRewardedAd(activity)
        }
    }

    fun loadNativeSmallAd(context: Context, onLoaded: ((NativeAd?) -> Unit)? = null) {
        if (preloadedNativeAdSmall != null) {
            onLoaded?.invoke(preloadedNativeAdSmall)
            return
        }
        
        if (onLoaded != null) {
            nativeSmallAdCallbacks.add(onLoaded)
        }

        if (isNativeSmallLoading) {
            return
        }

        isNativeSmallLoading = true
        val builder = AdLoader.Builder(context, getAdUnitId(context, "native_ad_id"))

        builder.forNativeAd { nativeAd ->
            Log.d(TAG, "Native Small Ad loaded successfully!")
            preloadedNativeAdSmall = nativeAd
            isNativeSmallLoading = false
            nativeSmallAdCallbacks.forEach { it.invoke(nativeAd) }
            nativeSmallAdCallbacks.clear()
        }

        val adLoader = builder
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "Native Small Ad failed to load: ${loadAdError.message}")
                    isNativeSmallLoading = false
                    nativeSmallAdCallbacks.forEach { it.invoke(null) }
                    nativeSmallAdCallbacks.clear()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                .setReturnUrlsForImageAssets(false)
                .build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadNativeFullAd(context: Context, onLoaded: ((NativeAd?) -> Unit)? = null) {
        if (preloadedNativeAdFull != null) {
            onLoaded?.invoke(preloadedNativeAdFull)
            return
        }
        
        if (onLoaded != null) {
            nativeFullAdCallbacks.add(onLoaded)
        }

        if (isNativeFullLoading) {
            return
        }

        isNativeFullLoading = true
        val builder = AdLoader.Builder(context, getAdUnitId(context, "native_ad_id"))

        builder.forNativeAd { nativeAd ->
            Log.d(TAG, "Native Full Ad loaded successfully!")
            preloadedNativeAdFull = nativeAd
            isNativeFullLoading = false
            nativeFullAdCallbacks.forEach { it.invoke(nativeAd) }
            nativeFullAdCallbacks.clear()
        }

        val adLoader = builder
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "Native Full Ad failed to load: ${loadAdError.message}")
                    isNativeFullLoading = false
                    nativeFullAdCallbacks.forEach { it.invoke(null) }
                    nativeFullAdCallbacks.clear()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun destroyAds() {
        preloadedNativeAdSmall?.destroy()
        preloadedNativeAdSmall = null
        preloadedNativeAdFull?.destroy()
        preloadedNativeAdFull = null
        interstitialAd = null
        rewardedAd = null
    }
}
