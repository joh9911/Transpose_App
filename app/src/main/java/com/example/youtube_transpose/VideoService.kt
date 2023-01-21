package com.example.youtube_transpose

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

class VideoService: Service() {
    lateinit var exoPlayer: SimpleExoPlayer
    lateinit var videoDetailData: VideoData

    companion object {
        const val TAG = "[MusicPlayerService]"
        const val VIDEO_FILE_ID = "VideoFileID"
        const val PLAY_PAUSE_ACTION = "playPauseAction"
        const val NOTIFICATION_ID = 20
    }

    override fun onBind(p0: Intent?): IBinder? {
        exoPlayer.playWhenReady = true
//        displayNotification()
        return VideoServiceBinder()
    }

    override fun onCreate() {
        super.onCreate()
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
    }
    fun init(){
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
        exoPlayer.playWhenReady = true
    }

    inner class VideoServiceBinder: Binder(){
        fun getService(): VideoService {
            return this@VideoService
        }
        fun getExoPlayerInstance() = exoPlayer
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            when (intent?.action) {
                Actions.START_FOREGROUND -> {
                    Log.e(TAG, "Start Foreground 인텐트를 받음")
                    startForegroundService()
                }
                Actions.STOP_FOREGROUND -> {
                    Log.e(TAG, "Stop Foreground 인텐트를 받음")
                    stopForegroundService()
                }
                Actions.PREV -> Log.e(TAG, "Clicked = 이전")
                Actions.PLAY -> {
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                }
                Actions.NEXT -> Log.e(TAG, "Clicked = 다음")
            }
            return START_STICKY

    }
    fun createNotification(): Notification {
        // 알림 클릭시 MainActivity로 이동됨
//        val notificationIntent = Intent(this, Activity::class.java)
//        notificationIntent.action = Actions.MAIN
//        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//
//
//        val pendingIntent = PendingIntent
//            .getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        // 각 버튼들에 관한 Intent
        val prevIntent = Intent(this, VideoService::class.java)
        prevIntent.action = Actions.PREV
        val prevPendingIntent = PendingIntent
            .getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(this, VideoService::class.java)
        playIntent.action = Actions.PLAY
        val playPendingIntent = PendingIntent
            .getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, VideoService::class.java)
        nextIntent.action = Actions.NEXT
        val nextPendingIntent = PendingIntent
            .getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        // 알림
        val notification = NotificationCompat.Builder(this, MusicNotification.CHANNEL_ID)
            .setContentTitle("Youtube_Transpose")
            .setContentText(videoDetailData.title)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous,
                "Prev", prevPendingIntent))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_play,
                "Play", playPendingIntent))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_next,
                "Next", nextPendingIntent))
//            .setContentIntent(pendingIntent)
            .build()


        // Oreo 부터는 Notification Channel을 만들어야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                MusicNotification.CHANNEL_ID,
                "Music Player Channel", // 채널표시명
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = this.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
        return notification
    }
    private fun startForegroundService() {

        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    fun getVideoData(videoData: VideoData){
        videoDetailData = videoData
    }

    fun setupVideoView(videoUrl: String){
        Log.d("셋업","비디오")
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .build()

        exoPlayer?.setMediaItem(mediaItem)
//        val audioSource = ProgressiveMediaSource
//            .Factory(DefaultHttpDataSource.Factory())
//            .createMediaSource(MediaItem.fromUri(videoUrl))
        val videoSource = ProgressiveMediaSource
            .Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(videoUrl))
        exoPlayer?.setMediaSource(videoSource)
        exoPlayer?.prepare()
        startForegroundService()
//        videoView.setVideoURI(Uri.parse(videoUrl))
    }
    private fun displayNotification() {
        //Lets create our remote view.
        val remoteView = RemoteViews(packageName, R.layout.video_notification)

        //Next create a pending intent and make it stop our video playback.
        val intent = PendingIntent.getService(this, 0, Intent(this, VideoService::class.java).apply {
            putExtra(PLAY_PAUSE_ACTION, 0)
        }, PendingIntent.FLAG_IMMUTABLE)
        remoteView.setOnClickPendingIntent(R.id.stop_player_btn, intent)

        //Now for showing through the notification manager.
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, "Default")

        notificationBuilder.setContent(remoteView)
        notificationBuilder.setSmallIcon(android.R.drawable.sym_def_app_icon)

        //Check for version and create a channel if needed.
        if (Build.VERSION.SDK_INT > 26) {
            manager.createNotificationChannel(NotificationChannel("ID", "Main", NotificationManager.IMPORTANCE_DEFAULT))
            notificationBuilder.setChannelId("ID")
        }
        val notification = notificationBuilder.build()
        startForeground(0, notification)
        manager.notify(NOTIFICATION_ID, notification)
    }
}