package com.example.movie_app.ads

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object AdUtils {
    private const val TAG = "AdUtils"

    fun getAdUnitId(context: Context, key: String): String {
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
}
