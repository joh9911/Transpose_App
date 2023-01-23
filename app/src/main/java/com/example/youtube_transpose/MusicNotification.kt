package com.example.youtube_transpose

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

object MusicNotification {

    const val CHANNEL_ID = "foreground_service_channel" // 임의의 채널 ID
    lateinit var notification: Notification
    fun createNotification(
        context: Context,
        videoService: VideoService
    ): Notification {
        // 알림 클릭시 MainActivity로 이동됨
        val notificationIntent = Intent(context, Activity::class.java)
        notificationIntent.action = Actions.MAIN
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP


        val pendingIntent = PendingIntent
            .getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        // 각 버튼들에 관한 Intent
        val prevIntent = Intent(context, VideoService::class.java)
        prevIntent.action = Actions.PREV
        val prevPendingIntent = PendingIntent
            .getService(context, 0, prevIntent, FLAG_IMMUTABLE)

        val playIntent = Intent(context, VideoService::class.java)
        playIntent.action = Actions.PLAY
        val playPendingIntent = PendingIntent
            .getService(context, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(context, VideoService::class.java)
        nextIntent.action = Actions.NEXT
        val nextPendingIntent = PendingIntent
            .getService(context, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
        if (videoService.exoPlayer.isPlaying){
            notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(videoService.videoDetailData.title)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
                .addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous,
                    "Prev", prevPendingIntent))
                .addAction(NotificationCompat.Action(android.R.drawable.ic_media_play,
                    "Play", playPendingIntent))
                .addAction(NotificationCompat.Action(android.R.drawable.ic_media_next,
                    "Next", nextPendingIntent))
                .setContentIntent(pendingIntent)
                .build()
        }
        else{

            notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(videoService.videoDetailData.title)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous,
                    "Prev", prevPendingIntent))
                .addAction(NotificationCompat.Action(android.R.drawable.ic_media_play,
                    "Play", playPendingIntent))
                .addAction(NotificationCompat.Action(android.R.drawable.ic_media_next,
                    "Next", nextPendingIntent))
                .setContentIntent(pendingIntent)
                .build()
        }
        // 알림



        // Oreo 부터는 Notification Channel을 만들어야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Channel", // 채널표시명
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }

        return notification
    }
}