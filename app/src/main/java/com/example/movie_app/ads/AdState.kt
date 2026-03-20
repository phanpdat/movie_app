package com.example.movie_app.ads

import android.content.Context
import com.example.movie_app.util.UserPreferences

object AdState {
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

    fun setUnlocked() {
        lastUnlockTime = System.currentTimeMillis()
    }
}
