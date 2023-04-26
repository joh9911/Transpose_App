package com.myFile.transpose

import android.content.Context

class AppUsageTimeChecker(context: Context) {
    private val appUsageSharedPreferences = AppUsageSharedPreferences(context)

    fun getAppUsageDuration(): Long {
        val startTime = appUsageSharedPreferences.getSharedPreferences().getLong("app_usage_start_time", 0)
        return if (startTime != 0L) {
            (System.currentTimeMillis() - startTime) / 1000 // 초 단위로 반환
        } else {
            0L
        }
    }
}