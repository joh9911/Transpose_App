package com.myFile.transpose.utils
import android.content.Context
import android.content.SharedPreferences
import com.myFile.transpose.BuildConfig

class AppUsageSharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_usage_preferences", Context.MODE_PRIVATE)

    fun saveAppUsageStartTime() {
        val firstTime = sharedPreferences.getLong("app_usage_start_time", 0)

        if (firstTime == 0L) {
            sharedPreferences.edit().putLong("app_usage_start_time", System.currentTimeMillis()).apply()
        }
    }

    fun saveCurrentAppVersion(){
        val versionCode = BuildConfig.VERSION_CODE
        sharedPreferences.edit().putInt("app_version_code", versionCode).apply()
    }

    fun saveDoNotShowAgain(boolean: Boolean){
        sharedPreferences.edit().putBoolean("do_not_show_again", boolean).apply()
    }

    fun getDoNotShowAgain(): Boolean {
        return sharedPreferences.getBoolean("do_not_show_again", false)
    }

    private fun getSavedAppVersionCode(): Int {
        return sharedPreferences.getInt("app_version_code", 0)
    }


    fun isNewVersionForUsers(): Boolean{
        val currentVersionCode = BuildConfig.VERSION_CODE
        val versionCode = getSavedAppVersionCode()
        return currentVersionCode > versionCode
    }
    fun initializeAppUsageStartTime(){
        sharedPreferences.edit().putLong("app_usage_start_time",System.currentTimeMillis()).apply()
    }

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences
    }
}