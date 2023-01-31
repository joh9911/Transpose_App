package com.example.video_transpose

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.media.MediaMetadata
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.BuildConfig
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*

class VideoService: Service() {
    lateinit var exoPlayer: SimpleExoPlayer
    lateinit var notification: Notification
    lateinit var mediaSession: MediaSessionCompat
    lateinit var activity: Activity
    lateinit var currentVideoData: VideoData
    var isConverting = false // url 변환이 진행중인지를 확인하기 위한 변수

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
        return super.onUnbind(intent)
        stopForegroundService()
    }

    override fun onCreate() {
        super.onCreate()
        initYoutubeDL()
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
                        startForegroundService()
                    }
                    Player.STATE_ENDED -> {
                        if (exoPlayer.mediaItemCount == 0) // play중이면 mediaItem을 제거하는데, 제거할 때 state_ended가 실행됨
                            return
                        val playerFragment = activity.supportFragmentManager.findFragmentById(R.id.player_fragment) as PlayerFragment
                        playerFragment.playNextVideo()
                        startForegroundService()
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
                    val playerFragment = activity.supportFragmentManager.findFragmentById(R.id.player_fragment) as PlayerFragment
                    playerFragment.playPrevVideo()
                    startForegroundService()
                }
                Actions.PLAY -> {
                    startForegroundService()
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                }
                Actions.NEXT -> {
                    val playerFragment = activity.supportFragmentManager.findFragmentById(R.id.player_fragment) as PlayerFragment
                    playerFragment.playNextVideo()
                    startForegroundService()
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

    fun startForegroundService() {
        startForeground(NOTIFICATION_ID, createNotification())
    }


    fun stopForegroundService() {
        stopForeground(true)
        exoPlayer.stop()
    }

    fun initActivity(param: Activity) {
        activity = param
    }

    private fun initYoutubeDL(){
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e(ContentValues.TAG, "failed to initialize youtubedl-android", e)
        }
    }

    fun playVideo(videoData: VideoData){
        if (exoPlayer.isPlaying)
            exoPlayer.removeMediaItem(0)
        currentVideoData = videoData
        val youtubeUrl = "https://www.youtube.com/watch?v=${videoData.videoId}".trim()
        startStream(youtubeUrl)
    }

    private fun startStream(url: String){
        if (isConverting){
            CompositeDisposable().dispose()
            isConverting = false
        }
        val disposable: Disposable = Observable.fromCallable {
            isConverting = true
            val request = YoutubeDLRequest(url)
            request.addOption("-f b", "")
            YoutubeDL.getInstance().getInfo(request)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ streamInfo ->
                isConverting = false
                val videoUrl: String = streamInfo.url
                if (TextUtils.isEmpty(videoUrl)) { Toast.makeText(activity, "failed to get stream url", Toast.LENGTH_LONG).show()
                } else {
                    setUpVideo(videoUrl)
                }
            }) { e ->
                if (BuildConfig.DEBUG) Log.e(ContentValues.TAG, "failed to get stream info", e)
                Toast.makeText(activity, "streaming failed. failed to get stream info", Toast.LENGTH_LONG).show()
            }
        CompositeDisposable().add(disposable)
    }

    private fun setUpVideo(convertedUrl: String){
        val videoSource = ProgressiveMediaSource
            .Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(convertedUrl))
        exoPlayer?.setMediaSource(videoSource)
        exoPlayer?.prepare()
        setTempo(activity.tempoSeekBar.progress)
        setPitch(activity.pitchSeekBar.progress)
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
                putString(MediaMetadata.METADATA_KEY_TITLE, currentVideoData.title)
                putString(MediaMetadata.METADATA_KEY_ARTIST, currentVideoData.channel)
                putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, currentVideoData.thumbnail)
                putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, currentVideoData.thumbnail)
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
                .setContentText(currentVideoData.title)
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
            notification = NotificationCompat.Builder(this,
                CHANNEL_ID
            )
                .setContentTitle("Music Player")
                .setContentText(currentVideoData.title)
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
        }
        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
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
        }

        override fun onStop() {
            super.onStop()
        }
    }

}