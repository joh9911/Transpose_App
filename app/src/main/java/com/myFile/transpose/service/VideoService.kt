//package com.myFile.transpose
//
//import android.app.*
//import android.content.*
//import android.content.pm.ServiceInfo
//import android.graphics.Bitmap
//import android.graphics.drawable.Drawable
//import android.media.AudioManager
//import android.media.MediaMetadata
//import android.os.*
//import android.provider.MediaStore
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.text.TextUtils
//import android.util.Log
//import android.view.KeyEvent
//import android.widget.Toast
//import androidx.core.app.NotificationCompat
//import androidx.media.session.MediaButtonReceiver
//import androidx.media3.common.Player
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.target.CustomTarget
//import com.bumptech.glide.request.transition.Transition
//import com.myFile.transpose.others.constants.Actions
//import com.myFile.transpose.View.fragment.PlayerServiceListener
//import com.myFile.transpose.Network.retrofit.VideoData
//import com.yausername.youtubedl_android.YoutubeDL
//import com.yausername.youtubedl_android.YoutubeDLException
//import com.yausername.youtubedl_android.YoutubeDLRequest
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
//import io.reactivex.rxjava3.core.Observable
//import io.reactivex.rxjava3.disposables.CompositeDisposable
//import io.reactivex.rxjava3.disposables.Disposable
//import io.reactivex.rxjava3.exceptions.UndeliverableException
//import io.reactivex.rxjava3.plugins.RxJavaPlugins
//import io.reactivex.rxjava3.schedulers.Schedulers
//import kotlinx.coroutines.*
//import java.io.IOException
//
//class VideoService: Service() {
//    lateinit var exoPlayer: ExoPlayer
//    lateinit var notification: Notification
//    lateinit var mediaSession: MediaSessionCompat
//    lateinit var playbackStateBuilder: PlaybackStateCompat.Builder
//    lateinit var metaDataBuilder: MediaMetadataCompat.Builder
//
//    private var currentVideoData: VideoData? = null
//    private var currentVideoThumbnailBitmap: Bitmap? = null
//    private lateinit var audioManager: AudioManager
//    private lateinit var compositeDisposable: CompositeDisposable
//    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
//    private val mediaReceiver = MediaReceiver()
//
//    val CHANNEL_ID = "foreground_service_channel" // 임의의 채널 ID
//
//    companion object {
//        const val TAG = "[MusicPlayerService]"
//        const val VIDEO_FILE_ID = "VideoFileID"
//        const val PLAY_PAUSE_ACTION = "playPauseAction"
//        const val NOTIFICATION_ID = 20
//
//    }
//
//
//    private var playerServiceListener: PlayerServiceListener? = null
//
//    private var serviceListenerToActivity: ServiceListenerToActivity? = null
//
//    private var listener: VideoServiceListener? = null
//
//    fun setVideoServiceListener(videoServiceListener: VideoServiceListener){
//        listener = videoServiceListener
//    }
//
//    fun setServiceListenerToActivity(listener: ServiceListenerToActivity?){
//        serviceListenerToActivity = listener
//    }
//
//    fun setPlayerServiceListener(listener: PlayerServiceListener?) {
//        playerServiceListener = listener
//    }
//
//    fun getServiceListenerToActivity(): ServiceListenerToActivity? {
//        return serviceListenerToActivity
//    }
//
//    fun getPlayerServiceListener(): PlayerServiceListener? {
//        return playerServiceListener
//    }
//
//    fun disconnectServiceListenerToActivity(){
//        serviceListenerToActivity = null
//    }
//
//    fun disconnectPlayerServiceListener(){
//        playerServiceListener = null
//    }
//    val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
//        Log.d("dl 업데이트 에러","$throwable ")
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopForegroundService()
//        unregisterReceiver(mediaReceiver)
//        exoPlayer.release()
//    }
//
//    override fun onUnbind(intent: Intent?): Boolean {
//        Log.d("unbind","stopForegroundService")
//        stopForegroundService()
//        return super.onUnbind(intent)
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//
//        initRxJavaExceptionHandler()
//        updateYoutubeDL()
//        initYoutubeDL()
//
//        setAudioFocus()
//        registerReceiver(mediaReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
//        val trackSelector = DefaultTrackSelector(this).apply {
//            setParameters(buildUponParameters().setMaxVideoSizeSd())
//        }
//        exoPlayer = ExoPlayer.Builder(this)
//            .setTrackSelector(trackSelector)
//            .setSeekForwardIncrementMs(10000)
//            .setSeekBackIncrementMs(10000)
//            .build()
//        exoPlayer.addListener(object: Player.Listener{
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                if (isPlaying) {
//                    requestAudioFocus()
//                    playerServiceListener?.onIsPlaying(1)
//                    startNotification(1)
//                }
//                else {
//                    // 동영상 재생이 끝났을 때를 제외
//                    if (exoPlayer.currentPosition >= exoPlayer.contentDuration)
//                        return
//                    playerServiceListener?.onIsPlaying(2)
//                    if (getPlayerServiceListener() != null)
//                        startNotification(2)
//                }
//                super.onIsPlayingChanged(isPlaying)
//            }
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                when (playbackState){
//                    Player.STATE_READY -> {
//                        startNotification(1)
//                    }
//                    Player.STATE_ENDED -> {
//                        if (exoPlayer.mediaItemCount == 0) // play중이면 mediaItem을 제거하는데, 제거할 때 state_ended가 실행됨
//                            return
//                        startNotification(3)
//                        playerServiceListener?.onIsPlaying(3)
//                        playerServiceListener?.onStateEnded()
//
//                    }
//                    Player.STATE_BUFFERING ->{
//                        //your logic
//                    }
//                    Player.STATE_IDLE -> {
//                    }
//                }
//            }
//        })
//        mediaSessionBuilder()
//        createNotificationChannel()
//    }
//
//    private fun createNotificationChannel(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                "Music Player Channel", // 채널표시명
//                NotificationManager.IMPORTANCE_LOW
//            )
//            val manager = this.getSystemService(NotificationManager::class.java)
//            manager?.createNotificationChannel(serviceChannel)
//        }
//    }
//
//    private fun mediaSessionBuilder(){
//        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
//            playbackStateBuilder = PlaybackStateCompat.Builder()
//            setPlaybackState(playbackStateBuilder.build())
//            setCallback(MediaSessionCallback())
//            metaDataBuilder = MediaMetadataCompat.Builder()
//        }
//    }
//
//    inner class MediaSessionCallback: MediaSessionCompat.Callback(){
//        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
//            if (mediaButtonEvent != null) {
//                val action = mediaButtonEvent?.action
//                if (action == Intent.ACTION_MEDIA_BUTTON) {
//                    val event =
//                        mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
//                    if (event != null && event.action == KeyEvent.ACTION_UP) {
//                        when (event.keyCode) {
//                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
//                                serviceListenerToActivity?.clickNext()
//                            }
//                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
//                                exoPlayer.pause()
//                            }
//                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
//                                exoPlayer.play()
//                            }
//                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
//                                if (exoPlayer.currentPosition >= 4000) {
//                                    exoPlayer.seekToPrevious()
//                                } else {
//                                    serviceListenerToActivity?.clickPrev()
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            return super.onMediaButtonEvent(mediaButtonEvent)
//        }
//        override fun onSeekTo(pos: Long) {
//            super.onSeekTo(pos)
//            exoPlayer.seekTo(pos)
//        }
//    }
//
//    fun startNotification(type: Int) {
//        if (currentVideoData != null){
//            val serviceIntent = Intent(this, VideoService::class.java)
//
//            val notification = when (type) {
//                0 -> createPrepareNotification()
//                1 -> createPlayingNotification()
//                2 -> createPausedNotification()
//                else -> createFinishedNotification()
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                applicationContext.startForegroundService(serviceIntent)
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//                try {
//                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//                }catch (e: Exception){
//                    Log.d("notification Error","$e")
//                }
//            }
//            else{
//                try {
//                    startForeground(NOTIFICATION_ID, notification)
//                }catch (e: Exception){
//                    Log.d("notification Error","$e")
//                }
//            }
//
//
//        }
//    }
//
//    private fun getNotificationBuilder(
//        bitmapThumbnail: Bitmap?,
//        actions: List<NotificationCompat.Action>,
//        isOnGoing: Boolean,
//        type: Int
//    ): Notification {
//        val notificationIntent = Intent(this, Activity::class.java)
//        notificationIntent.action = Actions.MAIN
//        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//        val pendingIntent = PendingIntent
//            .getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        return NotificationCompat.Builder(this,CHANNEL_ID)
//            .setContentTitle(currentVideoData?.title)
//            .setContentText(currentVideoData?.channelTitle)
//            .setLargeIcon(bitmapThumbnail)
//            .setSmallIcon(R.mipmap.app_icon)
//            .setOngoing(isOnGoing)
//            .apply {
//                if (type == 0){
//                    setStyle(androidx.media.app.NotificationCompat.MediaStyle()
//                        .setMediaSession(mediaSession.sessionToken)
//                    )
//                }
//                else{
//                    setStyle(androidx.media.app.NotificationCompat.MediaStyle()
//                        .setMediaSession(mediaSession.sessionToken)
//                        .setShowActionsInCompactView(0, 2, 4)
//                    )
//                }
//            }
//            .apply {
//                actions.forEach{ action ->
//                    addAction(action)
//                }
//            }
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setContentIntent(pendingIntent)
//            .setSilent(true)
//            .build()
//    }
//
//    private fun createPrepareNotification(): Notification {
//
//        val prevIntent = Intent(this, VideoService::class.java)
//        prevIntent.action = Actions.PREV
//        val prevPendingIntent = PendingIntent
//            .getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)
//
//       val prevAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_controls_previous,
//            "Prev", prevPendingIntent)
//
//        val nextIntent = Intent(this, VideoService::class.java)
//        nextIntent.action = Actions.NEXT
//        val nextPendingIntent = PendingIntent
//            .getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        val nextAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_notification_next,
//            "Next", nextPendingIntent)
//
//        val actions = listOf(prevAction, nextAction)
//
//        mediaSession.setMetadata(null)
//        mediaSession.setPlaybackState(null)
//
//        notification = getNotificationBuilder(
//            currentVideoThumbnailBitmap,
//            actions,
//            true,
//            0
//        )
//        Log.d("크레이트 프리페어 노티피케이션","실행")
//        return notification
//    }
//
//    private fun createPlayingNotification(): Notification {
//        val minusIntent = Intent(this, VideoService::class.java)
//        minusIntent.action = Actions.MINUS
//        val minusPendingIntent = PendingIntent
//            .getService(this, 0, minusIntent, PendingIntent.FLAG_IMMUTABLE)
//        val minusAction = NotificationCompat.Action(R.drawable.ic_baseline_exposure_neg_1_24,
//            "Minus", minusPendingIntent)
//
//        val prevIntent = Intent(this, VideoService::class.java)
//        prevIntent.action = Actions.PREV
//        val prevPendingIntent = PendingIntent
//            .getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)
//        val prevAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_controls_previous,
//            "Prev", prevPendingIntent)
//
//        val playIntent = Intent(this, VideoService::class.java)
//        playIntent.action = Actions.PLAY
//        val playPendingIntent = PendingIntent
//            .getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
//        val playAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_notification_pause,
//            "Play", playPendingIntent)
//
//        val nextIntent = Intent(this, VideoService::class.java)
//        nextIntent.action = Actions.NEXT
//        val nextPendingIntent = PendingIntent
//            .getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
//        val nextAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_notification_next,
//            "Next", nextPendingIntent)
//
//        val plusIntent = Intent(this, VideoService::class.java)
//        plusIntent.action = Actions.PLUS
//        val plusPendingIntent = PendingIntent
//            .getService(this, 0, plusIntent, PendingIntent.FLAG_IMMUTABLE)
//        val plusAction = NotificationCompat.Action(R.drawable.ic_baseline_exposure_plus_1_24,
//            "Plus", plusPendingIntent)
//
//        val actions = listOf(minusAction, prevAction, playAction, nextAction, plusAction)
//
//        mediaSession.setMetadata(MediaMetadataCompat.Builder().apply {
//            putLong(MediaMetadata.METADATA_KEY_DURATION,exoPlayer.duration)
//        }.build())
//
//        mediaSession.setPlaybackState(
//            playbackStateBuilder
//                .setState(
//                    if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING
//                    else PlaybackStateCompat.STATE_PAUSED,
//                    exoPlayer.currentPosition,
//                    1f,
//                    SystemClock.elapsedRealtime()
//                )
//                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                .build()
//        )
//
//        notification = getNotificationBuilder(
//            currentVideoThumbnailBitmap,
//            actions,
//            true,
//            1
//        )
//        return notification
//    }
//
//    private fun createPausedNotification(): Notification {
//        val minusIntent = Intent(this, VideoService::class.java)
//        minusIntent.action = Actions.MINUS
//        val minusPendingIntent = PendingIntent
//            .getService(this, 0, minusIntent, PendingIntent.FLAG_IMMUTABLE)
//        val minusAction = NotificationCompat.Action(R.drawable.ic_baseline_exposure_neg_1_24,
//            "Minus", minusPendingIntent)
//
//        val prevIntent = Intent(this, VideoService::class.java)
//        prevIntent.action = Actions.PREV
//        val prevPendingIntent = PendingIntent
//            .getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)
//        val prevAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_controls_previous,
//            "Prev", prevPendingIntent)
//
//        val playIntent = Intent(this, VideoService::class.java)
//        playIntent.action = Actions.PLAY
//        val playPendingIntent = PendingIntent
//            .getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
//        val playAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_notification_play,
//            "Play", playPendingIntent)
//
//        val nextIntent = Intent(this, VideoService::class.java)
//        nextIntent.action = Actions.NEXT
//        val nextPendingIntent = PendingIntent
//            .getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
//        val nextAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_notification_next,
//            "Next", nextPendingIntent)
//
//        val plusIntent = Intent(this, VideoService::class.java)
//        plusIntent.action = Actions.PLUS
//        val plusPendingIntent = PendingIntent
//            .getService(this, 0, plusIntent, PendingIntent.FLAG_IMMUTABLE)
//        val plusAction = NotificationCompat.Action(R.drawable.ic_baseline_exposure_plus_1_24,
//            "Plus", plusPendingIntent)
//
//        val actions = listOf(minusAction, prevAction, playAction, nextAction, plusAction)
//
//        mediaSession.setMetadata(MediaMetadataCompat.Builder().apply {
//            putLong(MediaMetadata.METADATA_KEY_DURATION,exoPlayer.duration)
//        }.build())
//
//        mediaSession.setPlaybackState(
//            playbackStateBuilder
//                .setState(
//                    if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING
//                    else PlaybackStateCompat.STATE_PAUSED,
//                    exoPlayer.currentPosition,
//                    1f,
//                    SystemClock.elapsedRealtime()
//                )
//                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                .build()
//        )
//
//        notification = getNotificationBuilder(
//            currentVideoThumbnailBitmap,
//            actions,
//            false,
//            2
//        )
//
//        return notification
//    }
//
//    private fun createFinishedNotification(): Notification {
//        val minusIntent = Intent(this, VideoService::class.java)
//        minusIntent.action = Actions.MINUS
//        val minusPendingIntent = PendingIntent
//            .getService(this, 0, minusIntent, PendingIntent.FLAG_IMMUTABLE)
//        val minusAction = NotificationCompat.Action(R.drawable.ic_baseline_exposure_neg_1_24,
//            "Minus", minusPendingIntent)
//
//        val prevIntent = Intent(this, VideoService::class.java)
//        prevIntent.action = Actions.PREV
//        val prevPendingIntent = PendingIntent
//            .getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)
//        val prevAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_controls_previous,
//            "Prev", prevPendingIntent)
//
//        val replayIntent = Intent(this, VideoService::class.java)
//        replayIntent.action = Actions.REPLAY
//        val replayPendingIntent = PendingIntent
//            .getService(this, 0, replayIntent, PendingIntent.FLAG_IMMUTABLE)
//        val replayAction = NotificationCompat.Action(
//            R.drawable.ic_baseline_loop_24,
//            "Replay", replayPendingIntent)
//
//        val nextIntent = Intent(this, VideoService::class.java)
//        nextIntent.action = Actions.NEXT
//        val nextPendingIntent = PendingIntent
//            .getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
//        val nextAction = NotificationCompat.Action(
//            com.google.android.exoplayer2.ui.R.drawable.exo_notification_next,
//            "Next", nextPendingIntent)
//
//        val plusIntent = Intent(this, VideoService::class.java)
//        plusIntent.action = Actions.PLUS
//        val plusPendingIntent = PendingIntent
//            .getService(this, 0, plusIntent, PendingIntent.FLAG_IMMUTABLE)
//        val plusAction = NotificationCompat.Action(R.drawable.ic_baseline_exposure_plus_1_24,
//            "Plus", plusPendingIntent)
//
//        val actions = listOf(minusAction, prevAction, replayAction, nextAction, plusAction)
//
//        mediaSession.setMetadata(MediaMetadataCompat.Builder().apply {
//            putLong(MediaMetadata.METADATA_KEY_DURATION,exoPlayer.duration)
//        }.build())
//
//        mediaSession.setPlaybackState(
//            playbackStateBuilder
//                .setState(
//                    if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING
//                    else PlaybackStateCompat.STATE_PAUSED,
//                    exoPlayer.currentPosition,
//                    1f,
//                    SystemClock.elapsedRealtime()
//                )
//                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                .build()
//        )
//
//        notification = getNotificationBuilder(
//            currentVideoThumbnailBitmap,
//            actions,
//            false,
//            3
//        )
//
//        return notification
//    }
//
//
//    private fun requestAudioFocus(){
//        val result: Int = audioManager.requestAudioFocus(
//            afChangeListener,
//            // Use the music stream.
//            AudioManager.STREAM_MUSIC,
//            // Request permanent focus.
//            AudioManager.AUDIOFOCUS_GAIN
//        )
//
////        exoPlayer.playWhenReady = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
//    }
//    private fun setAudioFocus() {
//        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        afChangeListener =
//            AudioManager.OnAudioFocusChangeListener {
//                when (it) {
//                    AudioManager.AUDIOFOCUS_LOSS -> {
//                        exoPlayer.playWhenReady = false
//                    }
//                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                        exoPlayer.playWhenReady = false
//                    }
//                    AudioManager.AUDIOFOCUS_GAIN -> {
////                        exoPlayer.playWhenReady = true
//                    }
//                }
//            }
//    }
//    private fun initRxJavaExceptionHandler() {
//        compositeDisposable = CompositeDisposable()
//        RxJavaPlugins.setErrorHandler { e ->
//            if (e is UndeliverableException) {
//                if (e.cause is InterruptedException)
//                    return@setErrorHandler
//            }
//        }
//    }
//    inner class MediaReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
//                exoPlayer.playWhenReady = false
//            }
//        }
//    }
//    override fun onBind(p0: Intent?): IBinder {
//        Log.d("온바인드","가 적절히 호출됨")
//        exoPlayer.playWhenReady = true
//        return VideoServiceBinder()
//    }
//
//    inner class VideoServiceBinder: Binder(){
//        fun getService(): VideoService {
//            return this@VideoService
//        }
//        fun getExoPlayerInstance() = exoPlayer
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d("온스타트커맨드","실행")
//            when (intent?.action) {
//                Actions.MINUS -> {
//                    serviceListenerToActivity?.clickMinus()
//                }
//                Actions.PREV -> {
//                    serviceListenerToActivity?.clickPrev()
//                }
//                Actions.REPLAY -> {
//                    exoPlayer.seekTo(0)
//                }
//                Actions.PLAY -> {
//                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
//                }
//                Actions.NEXT -> {
//                    serviceListenerToActivity?.clickNext()
//                }
//                Actions.PLUS -> {
//                    serviceListenerToActivity?.clickPlus()
//                }
//                Actions.INIT -> {
//                    serviceListenerToActivity?.clickInit()
//                }
//
//            }
//            return START_STICKY
//    }
//
//
//    fun stopForegroundService() {
//        Log.d("stopForegroundService","실행")
//        stopForeground(true)
//        exoPlayer.stop()
//    }
//
//    private fun initYoutubeDL(){
//        try {
//            YoutubeDL.getInstance().init(this)
//
//        } catch (e: YoutubeDLException) {
//            Log.e(ContentValues.TAG, "failed to initialize youtubedl-android", e)
//        }
//    }
//    private fun updateYoutubeDL(){
//        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch{
//            YoutubeDL.getInstance().updateYoutubeDL(this@VideoService)
//        }
//    }
//
//
//    fun playVideo(videoData: VideoData?){
//        if (exoPlayer.currentMediaItem != null)
//            exoPlayer.removeMediaItem(0)
//
//        currentVideoData = videoData
//        currentVideoThumbnailBitmap = null
//
//        playerServiceListener?.playerViewInvisible()
//        val youtubeUrl = "https://www.youtube.com/watch?v=${videoData?.videoId}".trim()
//        startStream(youtubeUrl)
//    }
//
//    fun streamingCancel(){
//        compositeDisposable.clear()
//    }
//
//    private fun startStream(url: String){
//        playerServiceListener?.onIsPlaying(0)
//        streamingCancel()
//        startNotification(0)
//
//        Glide.with(this)
//            .asBitmap()
//            .load(currentVideoData?.thumbnail)
//            .into(object: CustomTarget<Bitmap>(){
//                override fun onResourceReady(
//                    resource: Bitmap,
//                    transition: Transition<in Bitmap>?
//                ) {
//                    currentVideoThumbnailBitmap = resource
//                }
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    currentVideoThumbnailBitmap = null
//                }
//            })
//
//        val disposable: Disposable = Observable.fromCallable {
//
//            val request = YoutubeDLRequest(url)
//            request.addOption("-f b", "")
//
//            YoutubeDL.getInstance().getInfo(request)
//        }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ streamInfo ->
//
//                val videoUrl: String? = streamInfo.url
//                Log.d("유알엘","$videoUrl")
//                if (TextUtils.isEmpty(videoUrl)) {
//                    Log.d("유알엘 빌때","$videoUrl")
//                    serviceListenerToActivity?.showStreamFailMessage()
//                } else {
//                    setUpVideo(videoUrl!!)
//                }
//            }) { e ->
//                if (e is UndeliverableException) {
//                    val cause = e.cause
//                    if (cause is IOException) {
//                        // expected exception
//                        return@subscribe
//                    }
//                    if (cause is InterruptedException) {
//                        // expected exception
//                        return@subscribe
//                    }
//                    // unexpected exception
//                    Log.e(TAG, "Undeliverable exception caught: ${e.cause}")
//                    throw RuntimeException("Undeliverable exception caught!", e.cause)
//                }
//                if (BuildConfig.DEBUG) Log.d(ContentValues.TAG, "failed to get stream info", e)
//                Log.d("원인","$e")
//                serviceListenerToActivity?.showStreamFailMessage()
//                playerServiceListener?.playerViewInvisible()
//            }
//        compositeDisposable.add(disposable)
//    }
//
//    private fun setUpVideo(convertedUrl: String){
//        playerServiceListener?.playerViewVisible()
//        val videoSource: MediaSource = if (convertedUrl.contains("m3u8")){
//            HlsMediaSource
//                .Factory(DefaultHttpDataSource.Factory())
//                .createMediaSource(MediaItem.fromUri(convertedUrl))
//        } else{
//            ProgressiveMediaSource
//                .Factory(DefaultHttpDataSource.Factory())
//                .createMediaSource(MediaItem.fromUri(convertedUrl))
//        }
//
//        exoPlayer.setMediaSource(videoSource)
//        exoPlayer.prepare()
//        requestAudioFocus()
//
//        exoPlayer.playWhenReady = true
//        serviceListenerToActivity?.setTempo()
//        serviceListenerToActivity?.setPitch()
//    }
//
//}