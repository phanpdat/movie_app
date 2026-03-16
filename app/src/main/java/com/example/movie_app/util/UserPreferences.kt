package com.example.movie_app.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserPreferences private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("movie_app_prefs", Context.MODE_PRIVATE)

    private val _isPremium = MutableStateFlow(isPremiumUser())
    val isPremium: StateFlow<Boolean> = _isPremium

    fun setPremiumStatus(isPremium: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
        _isPremium.value = isPremium
    }

    private fun isPremiumUser(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PREMIUM, false)
    }

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
