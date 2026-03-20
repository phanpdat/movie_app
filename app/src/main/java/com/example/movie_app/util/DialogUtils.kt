package com.example.movie_app.util

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.movie_app.R
import com.example.movie_app.ads.AdManager

fun Fragment.showPremiumDialog(onUnlocked: () -> Unit) {
    val dialog = Dialog(requireContext())
    dialog.setContentView(R.layout.dialog_unlock_premium)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    val btnWatchAds = dialog.findViewById<View>(R.id.btnWatchAds)
    val btnUpgrade = dialog.findViewById<View>(R.id.btnUpgrade)
    val ivClose = dialog.findViewById<View>(R.id.ivClose)

    ivClose.setOnClickListener { dialog.dismiss() }

    btnUpgrade.setOnClickListener {
        dialog.dismiss()
        try {
            findNavController().navigate(R.id.subscriptionFragment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    btnWatchAds.setOnClickListener {
        dialog.dismiss()
        AdManager.showRewardedAd(requireActivity()) {
            onUnlocked()
        }
    }

    dialog.show()
}
