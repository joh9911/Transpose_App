package com.myFile.transpose.utils
import android.content.Context
import android.content.SharedPreferences

class AppUsageSharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_usage_preferences", Context.MODE_PRIVATE)

    fun saveAppUsageStartTime() {
        val firstTime = sharedPreferences.getLong("app_usage_start_time", 0)

        if (firstTime == 0L) {
            sharedPreferences.edit().putLong("app_usage_start_time", System.currentTimeMillis()).apply()
        }
    }
    fun initializeAppUsageStartTime(){
        sharedPreferences.edit().putLong("app_usage_start_time",System.currentTimeMillis()).apply()
    }

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences
    }
}