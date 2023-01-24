package com.example.youtube_transpose

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadata
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.MediaController
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

class VideoService: Service() {
    lateinit var exoPlayer: SimpleExoPlayer
    lateinit var notification: Notification
    lateinit var videoDetailData: VideoData
    lateinit var mediaSession: MediaSessionCompat
    lateinit var activity: Activity
    val CHANNEL_ID = "foreground_service_channel" // 임의의 채널 ID

    companion object {
        const val TAG = "[MusicPlayerService]"
        const val VIDEO_FILE_ID = "VideoFileID"
        const val PLAY_PAUSE_ACTION = "playPauseAction"
        const val NOTIFICATION_ID = 20
    }

    override fun onBind(p0: Intent?): IBinder? {
        exoPlayer.playWhenReady = true
        return VideoServiceBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForegroundService()
        exoPlayer.release()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("서비스","언바인드")
        return super.onUnbind(intent)

    }

    override fun onCreate() {
        super.onCreate()

        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
        exoPlayer.addListener(object: Player.Listener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState){
                    Player.STATE_READY -> {
                        Log.d("재생이 ","돠엇따?")
                        startForegroundService()
                        if (!exoPlayer.isPlaying){
//                            stopForegroundService()
                        }
                    }
                    Player.STATE_ENDED -> {
                        Log.d("끝낫음","ㅁㄴㅇ")
                    }
                    Player.STATE_BUFFERING ->{
                        //your logic
                    }
                    Player.STATE_IDLE -> {
                    }

                }
            }
        })
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
                Actions.MINUS -> {
                    setPitch(activity.pitchSeekBar.progress-1)
                    activity.pitchSeekBar.progress = activity.pitchSeekBar.progress - 1
                }
                Actions.PREV -> {

                }
                Actions.PLAY -> {
                    startForegroundService()
                    Log.d("액션","플레이")
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                }
                Actions.NEXT -> {

                }
                Actions.PLUS -> {
                    setPitch(activity.pitchSeekBar.progress+1)
                    activity.pitchSeekBar.progress = activity.pitchSeekBar.progress + 1
                }
                Actions.INIT -> {
                    activity.pitchSeekBar.progress = 0
                    setPitch(0)
                }
            }
            return START_STICKY
    }

    private fun startForegroundService() {
        Log.d("스타트","포그라운드")
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun stopForegroundService() {
        Log.d("스탑","포그라운드")
        stopForeground(false)
    }

    fun saveVideoData(videoData: VideoData){
        Log.d("들어온 데이타","${videoData.title}")
        videoDetailData = videoData
    }

    fun initActivity(param: Activity) {
        activity = param
    }

    fun setupVideoView(videoUrl: String){
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
        setTempo(activity.tempoSeekBar.progress)
        setPitch(activity.pitchSeekBar.progress)
        exoPlayer?.play()
    }

    fun setPitch(value: Int){
        val pitchValue = value*0.05.toFloat()
        val tempoValue = activity.tempoSeekBar.progress*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        exoPlayer?.playbackParameters = param
    }

    fun setTempo(value: Int){
        val tempoValue = value*0.05.toFloat()
        val pitchValue = activity.pitchSeekBar.progress*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        exoPlayer?.playbackParameters = param
    }

    fun createNotification(
    ): Notification {
        // 알림 클릭시 MainActivity로 이동됨
        val notificationIntent = Intent(this, Activity::class.java)
        notificationIntent.action = Actions.MAIN
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP


        val pendingIntent = PendingIntent
            .getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val minusIntent = Intent(this, VideoService::class.java)
        minusIntent.action = Actions.MINUS
        val minusPendingIntent = PendingIntent
            .getService(this, 0, minusIntent, PendingIntent.FLAG_IMMUTABLE)

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

        val plusIntent = Intent(this, VideoService::class.java)
        plusIntent.action = Actions.PLUS
        val plusPendingIntent = PendingIntent
            .getService(this, 0, plusIntent, PendingIntent.FLAG_IMMUTABLE)


        val initIntent = Intent(this, VideoService::class.java)
        nextIntent.action = Actions.INIT
        val initPendingIntent = PendingIntent
            .getService(this, 0, initIntent, PendingIntent.FLAG_IMMUTABLE)

        mediaSession = MediaSessionCompat(this, "PlayerService")
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0,2,4)
            .setMediaSession(mediaSession.sessionToken)

        
        val metadataBuilder = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadata.METADATA_KEY_TITLE, videoDetailData.title)
            putString(MediaMetadata.METADATA_KEY_ARTIST, videoDetailData.channel)
            putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, videoDetailData.thumbnail)
            putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, videoDetailData.thumbnail)
            putLong(MediaMetadata.METADATA_KEY_DURATION,exoPlayer.duration)
        }
        mediaSession.setMetadata(metadataBuilder.build())
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    exoPlayer.currentPosition,
                    1f,
                    SystemClock.elapsedRealtime()
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
        )
        mediaSession.setCallback(MediaSessionCallback())

        if (exoPlayer.isPlaying){
            notification = NotificationCompat.Builder(this,
                CHANNEL_ID
            )
                .setContentTitle("Music Player")
                .setContentText(videoDetailData.title)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setStyle(mediaStyle)
                .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
                .addAction(NotificationCompat.Action(R.drawable.ic_baseline_exposure_neg_1_24,
                    "Minus", minusPendingIntent))
                .addAction(NotificationCompat.Action(
                    com.google.android.exoplayer2.ui.R.drawable.exo_controls_previous,
                    "Prev", prevPendingIntent))
                .addAction(NotificationCompat.Action(
                    com.google.android.exoplayer2.ui.R.drawable.exo_notification_pause,
                    "Play", playPendingIntent))
                .addAction(NotificationCompat.Action(
                    com.google.android.exoplayer2.ui.R.drawable.exo_notification_next,
                    "Next", nextPendingIntent))
                .addAction(NotificationCompat.Action(R.drawable.ic_baseline_exposure_plus_1_24,
                    "Plus", plusPendingIntent))
//                .addAction(NotificationCompat.Action(R.drawable.ic_baseline_replay_24,
//                    "initialize", initPendingIntent))
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build()
        }
        else{
            Log.d("엘스","제발")
            notification = NotificationCompat.Builder(this,
                CHANNEL_ID
            )
                .setContentTitle("Music Player")
                .setContentText(videoDetailData.title)
                .setStyle(mediaStyle)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(NotificationCompat.Action(R.drawable.ic_baseline_exposure_neg_1_24,
                    "Minus", minusPendingIntent))
                .addAction(NotificationCompat.Action(
                    com.google.android.exoplayer2.ui.R.drawable.exo_controls_previous,
                    "Prev", prevPendingIntent))
                .addAction(NotificationCompat.Action(
                    com.google.android.exoplayer2.ui.R.drawable.exo_notification_play,
                    "Play", playPendingIntent))
                .addAction(NotificationCompat.Action(
                    com.google.android.exoplayer2.ui.R.drawable.exo_notification_next,
                    "Next", nextPendingIntent))
                .addAction(NotificationCompat.Action(R.drawable.ic_baseline_exposure_plus_1_24,
                    "Plus", plusPendingIntent))
//                .addAction(NotificationCompat.Action(R.drawable.ic_baseline_replay_24,
//                    "initialize", initPendingIntent))
                .setContentIntent(pendingIntent)
                .setSilent(true)
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
            val manager = this.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }

        return notification
    }

    inner class MediaSessionCallback(): MediaSessionCompat.Callback(){
        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
            Log.d("온 버튼","이벤트")
        }
        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d("타임라인","변화")
            exoPlayer.seekTo(pos)
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING
                        else PlaybackStateCompat.STATE_PAUSED,
                        exoPlayer.currentPosition,
                        1f,
                        SystemClock.elapsedRealtime()
                    )
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
            )

        }

        override fun onPause() {
            super.onPause()
            Log.d("음아기","멈췃을때")
        }

        override fun onStop() {
            super.onStop()
            Log.d("음아기","멈췃을때")
        }
    }

}