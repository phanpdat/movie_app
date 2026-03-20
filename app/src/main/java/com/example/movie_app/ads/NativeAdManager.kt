package com.example.movie_app.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

object NativeAdManager {
    private const val TAG = "NativeAdManager"
    
    var preloadedNativeAdSmall: NativeAd? = null
    var preloadedNativeAdFull: NativeAd? = null

    var isNativeSmallLoading = false
    var isNativeFullLoading = false

    private val nativeSmallAdCallbacks = mutableListOf<(NativeAd?) -> Unit>()
    private val nativeFullAdCallbacks = mutableListOf<(NativeAd?) -> Unit>()

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
        val adUnitId = AdUtils.getAdUnitId(context, "native_ad_id")
        val builder = AdLoader.Builder(context, adUnitId)

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
        val adUnitId = AdUtils.getAdUnitId(context, "native_ad_id")
        val builder = AdLoader.Builder(context, adUnitId)

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

    fun destroy() {
        preloadedNativeAdSmall?.destroy()
        preloadedNativeAdSmall = null
        preloadedNativeAdFull?.destroy()
        preloadedNativeAdFull = null
    }
}
