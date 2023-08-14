package com.myFile.transpose.others

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.gson.Gson
import com.myFile.transpose.R
import com.myFile.transpose.model.model.VideoDataModel
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest

class ExpeditedWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {
        Log.d("두월크","실행")
        setForeground(createProgressNotification())
        val videoDataJson = inputData.getString("videoData")
        val videoData = Gson().fromJson(videoDataJson, VideoDataModel::class.java)

        val request = YoutubeDLRequest(videoData.videoId)
        request.addOption("-f b")
        val output = Data.Builder()
        try {
            val response = YoutubeDL.getInstance().getInfo(request)
            val intent = Intent("YOUR_CUSTOM_ACTION")
            intent.putExtra("url", "${response.url}")
            applicationContext.sendBroadcast(intent)
            Log.d("work 유알엘","${response.url}")
            output.putString("convertedUrl","${response.url}")
        }catch (e: Exception){
            Log.d("익셉션","$e")
        }
        Log.d("리턴","전")

        return Result.success(output.build())
    }


    private fun createProgressNotification(): ForegroundInfo {
        val PROGRESS_MAX = 100
        var PROGRESS_CURRENT = 0
        val notification  = NotificationCompat.Builder(applicationContext, "notification channel id 1")
            .setContentTitle("Converting...")
            .setContentText("convert in progress")
            .setSmallIcon(R.mipmap.app_icon)
            .setOngoing(true)
            .setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            .build()

        Log.d("크레이트","노티피케이션")

        return ForegroundInfo(200, notification)
    }

}