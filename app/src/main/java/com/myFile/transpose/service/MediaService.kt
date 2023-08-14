package com.myFile.transpose.service

import android.annotation.SuppressLint
import android.app.*
import android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.google.common.util.concurrent.ListenableFuture
import com.myFile.transpose.BuildConfig
import com.myFile.transpose.R
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.others.constants.Actions
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.IOException

class MediaService: MediaSessionService(), Player.Listener {
    private lateinit var exoPlayer: Player
    private var mediaSession: MediaSession? = null
    private lateinit var compositeDisposable: CompositeDisposable

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
    Log.d("dl 업데이트 에러","$throwable ")

    }

    override fun onCreate() {
        super.onCreate()
        Log.d("서비스가","실행됨")
        initYoutubeDL()
        updateYoutubeDL()

        initRxJavaExceptionHandler()
        createNotificationChannel()
        val customCallback = CustomMediaSessionCallback()
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(), true)
            .setHandleAudioBecomingNoisy(true)
            .build()


        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(customCallback)
            .build()

        mediaSession?.setCustomLayout(createCommandButton())
        val notificationIntent = Intent(this, Activity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        mediaSession?.setSessionActivity(pendingIntent)


    }

    private fun initRxJavaExceptionHandler() {
    compositeDisposable = CompositeDisposable()
    RxJavaPlugins.setErrorHandler { e ->
        if (e is UndeliverableException) {
            if (e.cause is InterruptedException)
                return@setErrorHandler
            }
        }
    }


    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("채널 ","생성")
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Player Channel", // 채널표시명
                NotificationManager.IMPORTANCE_HIGH
            )
                serviceChannel.setSound(null,null)
            val notificationManager: NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun initYoutubeDL(){
    try {
        YoutubeDL.getInstance().init(this)

    } catch (e: YoutubeDLException) {
        Log.e(ContentValues.TAG, "failed to initialize youtubedl-android", e)
    }
}
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession?
            = mediaSession

    override fun onDestroy() {
        Log.d("종료","서비스")
        streamingCancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }


    private fun updateYoutubeDL(){
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch{
            YoutubeDL.getInstance().updateYoutubeDL(this@MediaService)
        }
    }


    @SuppressLint("PrivateResource")
    private fun createCommandButton(): List<CommandButton> {
        val pitchMinusCommand = SessionCommand(Actions.MINUS, Bundle())
        val previousCommand = SessionCommand("Previous", Bundle())
        val playPauseCommand = SessionCommand("PlayPause",Bundle())
        val pitchPlusCommand = SessionCommand(Actions.PLUS,Bundle())
        val thumbsUpCommand = SessionCommand("ACTION_THUMBS_UP", Bundle())
        val repeatCommand = SessionCommand("ACTION_REPEAT", Bundle())
        val minusButton = CommandButton.Builder()
            .setSessionCommand(pitchMinusCommand)
            .setIconResId(R.drawable.ic_baseline_exposure_neg_1_24)
            .setDisplayName("Minus")
            .build()

        val previousButton = CommandButton.Builder()
            .setSessionCommand(previousCommand)
            .setIconResId(androidx.media3.ui.R.drawable.exo_notification_previous)
            .setDisplayName("Previous")
            .build()

        val playPauseButton = CommandButton.Builder()
            .setSessionCommand(playPauseCommand)
            .setIconResId(androidx.media3.ui.R.drawable.exo_notification_play)
            .setDisplayName("PlayPause")
            .build()

        val plusButton = CommandButton.Builder()
            .setSessionCommand(pitchPlusCommand)
            .setIconResId(R.drawable.ic_baseline_exposure_plus_1_24)
            .setDisplayName("Plus")
            .build()


        return listOf(plusButton, minusButton)
    }

    private fun createProgressNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        val notificationIntent = Intent(this, Activity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle("Converting...")
            setContentText("convert in progress")
            setSmallIcon(R.mipmap.app_icon)
            setContentIntent(pendingIntent)
            foregroundServiceBehavior = FOREGROUND_SERVICE_IMMEDIATE
            priority = NotificationCompat.PRIORITY_MAX
        }
        val PROGRESS_MAX = 100
        var PROGRESS_CURRENT = 0
        Log.d("크레이트","노티피케이션")
        builder.setOngoing(true)
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)

//        NotificationManagerCompat.from(this).apply {
//            // Issue the initial notification with zero progress
//            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
//            notify(NOTIFICATION_ID, builder.build())
//
//            // Do the job here that tracks the progress.
//            // Usually, this should be in a
//            // worker thread
//            // To show progress, update PROGRESS_CURRENT and update the notification with:
//            // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
//            // notificationManager.notify(notificationId, builder.build());
//
//
//            // When done, update the notification one more time to remove the progress bar
//        }
        startForeground(200, builder.build())
    }



    private inner class CustomMediaSessionCallback: MediaSession.Callback {
        // Configure commands available to the controller in onConnect()

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            super.onPostConnect(session, controller)
            session.setCustomLayout(controller, createCommandButton())
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {

            val connectionResult = super.onConnect(session, controller)

            val downloadUrl = SessionCommand(CONVERT_URL, Bundle())

            val stopConverting = SessionCommand(STOP_CONVERTING, Bundle())

            val commandButtons = createCommandButton()
            val sessionCommands = connectionResult.availableSessionCommands.buildUpon()

            commandButtons.forEach { commandButton ->
                commandButton.sessionCommand?.let {
                    sessionCommands.add(it)
                }
            }
            // Add custom commands
            sessionCommands.add(downloadUrl)
            sessionCommands.add(stopConverting)

            Log.d("온커넥트", "ㅁㄴㅇㄹ")
            return MediaSession.ConnectionResult.accept(
                sessionCommands.build(), connectionResult.availablePlayerCommands
            )
        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            super.onDisconnected(session, controller)
            Log.d("온디스커넥트", "ㅁㄴㅇㄹㄴㅁㄹ")

        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CONVERT_URL -> {
                    val videoId =
                        customCommand.customExtras.getString("videoId")
                    if (videoId != null) {
                        startStream(videoId)
                    }
                }
                STOP_CONVERTING -> {
                    streamingCancel()
                }
                Actions.MINUS -> {
                    val intent = Intent("YOUR_CUSTOM_ACTION")
                    intent.putExtra("status", "minus")
                    sendBroadcast(intent)
                }
                Actions.PLUS -> {
                    val intent = Intent("YOUR_CUSTOM_ACTION")
                    intent.putExtra("status", "plus")
                    sendBroadcast(intent)
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

//    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
//        Log.d("업데이트","노티 호출")
//
//        super.onUpdateNotification(session, startInForegroundRequired)
//    }

    companion object{
        const val NOTIFICATION_ID = 200
        private const val NOTIFICATION_CHANNEL_NAME = "notification channel 1"
        private const val NOTIFICATION_CHANNEL_ID = "notification channel id 1"
        const val START_FOREGROUND = "startForeground"
        const val CONVERT_URL = "convertUrl"
        const val STOP_CONVERTING = "stopConverting"
    }


    private fun streamingCancel(){
        compositeDisposable.clear()
        stopForeground(true)
    }

    fun startStream(videoId: String){
        if (exoPlayer.currentMediaItem != null)
            exoPlayer.removeMediaItem(0)

        exoPlayer.stop()
        streamingCancel()

        Handler(Looper.getMainLooper()).postDelayed({
            createProgressNotification()
        }, 100) // 1000ms = 1초

        Log.d("스타트","스트림${videoId}")
        val disposable: Disposable = Observable.fromCallable {

            val request = YoutubeDLRequest(videoId)
            request.addOption("-f", "best")
            YoutubeDL.getInstance().getInfo(request)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ streamInfo ->

                val videoUrl: String? = streamInfo.url
                Log.d("유알엘","$videoUrl")
                if (TextUtils.isEmpty(videoUrl)) {
                    sendFailBroadCast()
                    Log.d("유알엘 빌때","$videoUrl")
                } else {
                    setUpVideo(videoUrl!!, streamInfo)
                }
            }) { e ->
                if (e is UndeliverableException) {
                    val cause = e.cause
                    if (cause is IOException) {
                        // expected exception
                        return@subscribe
                    }
                    if (cause is InterruptedException) {
                        // expected exception
                        return@subscribe
                    }
                    // unexpected exception
                    Log.e("gkgk", "Undeliverable exception caught: ${e.cause}")
                    throw RuntimeException("Undeliverable exception caught!", e.cause)
                }
                if (BuildConfig.DEBUG) Log.d(ContentValues.TAG, "failed to get stream info", e)
                sendFailBroadCast()
                Log.d("원인","$e")
            }
        compositeDisposable.add(disposable)
    }

    private fun sendFailBroadCast(){
        val intent = Intent("YOUR_CUSTOM_ACTION")
        intent.putExtra("status", "failure")
        sendBroadcast(intent)
    }

    private fun setUpVideo(convertedUrl: String, videoData: VideoInfo){
        val mediaItem = MediaItem.Builder()
            .setUri(convertedUrl)
            .also {
                val metadata = MediaMetadata.Builder()
                    .setTitle(videoData.title)
                    .setAlbumTitle(videoData.title)
                    .setAlbumArtist(videoData.uploader)
                    .setArtist(videoData.uploader)
                    .setArtworkUri(videoData.thumbnail?.toUri())
                    .build()
                it.setMediaMetadata(metadata)
            }
            .build()


        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        exoPlayer.playWhenReady = true
    }


}

