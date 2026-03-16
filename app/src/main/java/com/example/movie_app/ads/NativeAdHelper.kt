// package com.example.movie_app.ads

// import android.view.View
// import android.widget.Button
// import android.widget.ImageView
// import android.widget.TextView
// import com.example.movie_app.R
// import com.google.android.gms.ads.nativead.NativeAd
// import com.google.android.gms.ads.nativead.NativeAdView

// object NativeAdHelper {

//     fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView, onClose: (() -> Unit)? = null) {
//         val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
//         val bodyView = adView.findViewById<TextView>(R.id.ad_body)
//         val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
//         val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
//         val mediaView = adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(R.id.ad_media)
//         val closeView = adView.findViewById<ImageView>(R.id.iv_close)
//         val closeCardView = adView.findViewById<ImageView>(R.id.iv_close_card)
//         val detailsContainer = adView.findViewById<View>(R.id.included_details)

//         adView.headlineView = headlineView
//         adView.bodyView = bodyView
//         adView.callToActionView = callToActionView
//         adView.iconView = iconView
        
//         if (mediaView != null) {
//             adView.mediaView = mediaView
//         }
//         headlineView?.text = nativeAd.headline

//         if (nativeAd.callToAction == null) {
//             callToActionView?.visibility = View.INVISIBLE
//         } else {
//             callToActionView?.visibility = View.VISIBLE
//             callToActionView?.text = nativeAd.callToAction
//         }

//         if (nativeAd.icon == null) {
//             iconView?.visibility = View.GONE
//         } else {
//             iconView?.visibility = View.VISIBLE
//             iconView?.setImageDrawable(nativeAd.icon?.drawable)
//         }

//         if (nativeAd.body == null) {
//             bodyView?.visibility = View.INVISIBLE
//         } else {
//             bodyView?.visibility = View.VISIBLE
//             bodyView?.text = nativeAd.body
//         }

//         closeView?.setOnClickListener {
//             onClose?.invoke()
//         }

//         closeCardView?.setOnClickListener {
//             if (detailsContainer != null) {
//                 detailsContainer.visibility = View.GONE
//             } else {
//                 onClose?.invoke()
//             }
//         }

//         adView.setNativeAd(nativeAd)
//     }
// }
