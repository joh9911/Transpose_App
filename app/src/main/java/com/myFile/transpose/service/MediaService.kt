package com.myFile.transpose.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.*
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.google.common.util.concurrent.ListenableFuture
import com.myFile.transpose.BuildConfig
import com.myFile.transpose.R
import com.myFile.transpose.others.constants.Actions
import com.myFile.transpose.others.constants.Actions.TAG
import com.myFile.transpose.view.Activity.Activity
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


@OptIn(UnstableApi::class) class MediaService: MediaSessionService(){
    private lateinit var exoPlayer: ExoPlayer
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var environmentalReverb: EnvironmentalReverb? = null
    private var dynamicsProcessing: DynamicsProcessing? = null
    private var mediaSession: MediaSession? = null
    private lateinit var compositeDisposable: CompositeDisposable

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
    Log.d("dl 업데이트 에러","$throwable ")
    }


    override fun onCreate() {
        super.onCreate()
        initYoutubeDL()
        updateYoutubeDL()
        initRxJavaExceptionHandler()
        createNotificationChannel()
        initPlayer()
        initMediaSession()
        initAudioEffect()

    }
    private fun initPlayer(){
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(), true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        addListener()


    }

    @OptIn(UnstableApi::class) private fun addListener(){

        val listener = object: Player.Listener{

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                Log.d("오디오 세션","바뀜")
//                setEqualizer(null)
                super.onAudioSessionIdChanged(audioSessionId)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem ?: return
                exoPlayer.playWhenReady = true
                val title = mediaItem.mediaMetadata.title
                // 이건 두번 호출됨, 왜냐면 맨처음 아이템을 장착할 때 한번, 두번 째 세팅할 때 한번
                if (title == "converting.."){
                    startStream(mediaItem.mediaId)
                }
                super.onMediaItemTransition(mediaItem, reason)
            }
        }
        exoPlayer.addListener(listener)
    }

    @OptIn(UnstableApi::class) private fun initMediaSession(){
        val customCallback = CustomMediaSessionCallback()
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(customCallback)
            .build()

        mediaSession?.setCustomLayout(createCommandButton())
        val notificationIntent = Intent(this, Activity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
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

    private fun initAudioEffect(){
        val audioSessionId = exoPlayer.audioSessionId
        try{
            equalizer = Equalizer(0, exoPlayer.audioSessionId)
            equalizer?.enabled = true

            bassBoost = BassBoost(0, audioSessionId)
            bassBoost?.enabled = true

            loudnessEnhancer = LoudnessEnhancer(audioSessionId)
            loudnessEnhancer?.enabled = true

            virtualizer = Virtualizer(0, audioSessionId)
            virtualizer?.enabled = true
            virtualizer?.forceVirtualizationMode(Virtualizer.VIRTUALIZATION_MODE_BINAURAL)

            presetReverb = PresetReverb(1, 0)
            presetReverb?.enabled = true
        }
        catch (e: Exception){
            Log.d(TAG,"initAudioEffect 초기화 오류")
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
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession?{

        return mediaSession
    }




    private fun updateYoutubeDL(){
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch{
            YoutubeDL.getInstance().updateYoutubeDL(this@MediaService, YoutubeDL.UpdateChannel._NIGHTLY)
            Log.d("업데이트 코드가","실행됨")
        }
    }


    @SuppressLint("PrivateResource")
    private fun createCommandButton(): List<CommandButton> {
        val pitchMinusCommand = SessionCommand(Actions.MINUS, Bundle())
        val pitchPlusCommand = SessionCommand(Actions.PLUS,Bundle())

        val minusButton = CommandButton.Builder()
            .setSessionCommand(pitchMinusCommand)
            .setIconResId(R.drawable.ic_baseline_exposure_neg_1_24)
            .setDisplayName("Minus")
            .build()


        val plusButton = CommandButton.Builder()
            .setSessionCommand(pitchPlusCommand)
            .setIconResId(R.drawable.ic_baseline_exposure_plus_1_24)
            .setDisplayName("Plus")
            .build()


        return listOf(minusButton, plusButton )
    }

    // true로 바꿔야함 https://github.com/androidx/media/issues/167

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        try {
            Log.d("노티피케이션","업데이트호출")
            super.onUpdateNotification(session, true)
        }catch (e: Exception){
            Log.d("업데이트노티","$e")
        }
    }


    private fun setEqualizer(value: Int?){
        value ?: return
        if (value == -1){
            disableEqualizer()
            return
        }
        if (equalizer == null){
            try{
                equalizer = Equalizer(0, exoPlayer.audioSessionId)
                equalizer?.enabled = true
            }
            catch (e: Exception){
                Log.d(TAG,"equalizer 재 초기화 오류")
            }



        }

        equalizer?.usePreset(value.toShort())

        val i = equalizer?.numberOfBands!!
        val intent = Intent(Actions.GET_EQUALIZER_INFO)
        for (index in 0 until i){
            Log.d("이퀼라이저","${equalizer?.getBandLevel(index.toShort())}")

            intent.putExtra("$index","${equalizer?.getBandLevel(index.toShort())}")

        }
        sendBroadcast(intent)
        val sonic = DefaultRenderersFactory(this)
    }

    private fun disableEqualizer(){
        setEqualizer(3)
        equalizer?.release()
        equalizer = null
    }

    private fun useBassBoost(value: Int){
        bassBoost ?: return
        val audioSessionId = exoPlayer.audioSessionId

        if (audioSessionId != AudioEffect.ERROR_BAD_VALUE) {
            if (bassBoost?.strengthSupported!!) {
                Log.d("bass","조건문2")
                bassBoost?.setStrength(value.toShort()) // Set bass strength, values are between 0 and 1000
                exoPlayer.setAuxEffectInfo(AuxEffectInfo(bassBoost?.id!!, 1f))
            }
        }
    }

    private fun useLoudnessEnhancer(value: Int){
        loudnessEnhancer ?: return
        val audioSessionId = exoPlayer.audioSessionId
        if (audioSessionId != AudioEffect.ERROR_BAD_VALUE){
            Log.d("loud","조건문 1")

            loudnessEnhancer?.setTargetGain(value)
        }
    }

    @OptIn(UnstableApi::class) private fun useVirtualizer(value: Int) {
        virtualizer ?: return
        Log.d("버튜얼","$value")
        virtualizer?.setStrength(value.toShort())
        virtualizer?.enabled = true
        virtualizer?.forceVirtualizationMode(Virtualizer.VIRTUALIZATION_MODE_BINAURAL)


        exoPlayer.setAuxEffectInfo(AuxEffectInfo(virtualizer!!.id, 0.5f))
    }

    private fun usePresetReverb(value: Int, sendLevel: Int) {

        if (value == -1 && sendLevel == -1){
            disablePresetReverb()
            return
        }
        if (presetReverb == null){
            try{
                presetReverb = PresetReverb(1, 0)
                presetReverb?.enabled = true
            }catch (e: Exception){
                Log.d(TAG,"usePresetReverb 초기화 오류")
            }

        }

        Log.d("리버브","활성화")
        // reverb 효과는 오디오 세션 0에서 작동
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val normalizedVolume = currentVolume.toFloat() / maxVolume


            presetReverb?.preset = value.toShort()

            if (sendLevel == -1)
                exoPlayer.setAuxEffectInfo(AuxEffectInfo(presetReverb!!.id, normalizedVolume))
            else
                exoPlayer.setAuxEffectInfo(AuxEffectInfo(presetReverb!!.id, sendLevel.toFloat() / 100f))
        }catch (e: Exception){
            Log.d("예외","$e")
        }

    }

    private fun disablePresetReverb(){
        usePresetReverb(0, -1)
        presetReverb?.release()
        presetReverb = null
    }

    private fun useEnvironmentReverb(){
        Log.d("환경","사용 ${exoPlayer.audioSessionId == C.AUDIO_SESSION_ID_UNSET}")
        if (environmentalReverb == null){
            environmentalReverb = EnvironmentalReverb(0, 0)
            environmentalReverb!!.reflectionsLevel = -8500
            environmentalReverb!!.roomLevel = -8500
            environmentalReverb!!.enabled = true
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(environmentalReverb!!.id, 1.0f))
        } else{
            Log.d("환경","해제")
            environmentalReverb?.release()
            environmentalReverb = null
        }

    }

    private fun clearAudioEffect(){

    }

    private fun createMyCustomCommands(): List<SessionCommand> {

        val myCommands = arrayListOf<SessionCommand>()
        val downloadUrl = SessionCommand(CONVERT_URL, Bundle())

        val stopConverting = SessionCommand(STOP_CONVERTING, Bundle())

        val bassBoostCommand = SessionCommand(Actions.SET_BASS_BOOST, Bundle())

        val loudnessEnhancementCommand = SessionCommand(Actions.SET_LOUDNESS_ENHANCER, Bundle())

        val equalizerCommand = SessionCommand(Actions.SET_EQUALIZER, Bundle())

        val reverbCommand = SessionCommand(Actions.SET_REVERB, Bundle())

        val virtualizerCommand = SessionCommand(Actions.SET_VIRTUALIZER, Bundle())

        val environmentalReverbCommand = SessionCommand(Actions.SET_ENVIRONMENT_REVERB, Bundle())

        myCommands.add(downloadUrl)
        myCommands.add(stopConverting)
        myCommands.add(bassBoostCommand)
        myCommands.add(loudnessEnhancementCommand)
        myCommands.add(equalizerCommand)
        myCommands.add(reverbCommand)
        myCommands.add(virtualizerCommand)
        myCommands.add(environmentalReverbCommand)
        return myCommands
    }

    private fun releaseAudioEffects(){
        equalizer?.release()
        equalizer = null

        bassBoost?.release()
        bassBoost = null

        loudnessEnhancer?.release()
        loudnessEnhancer = null

        virtualizer?.release()
        virtualizer = null
    }



    private inner class CustomMediaSessionCallback: MediaSession.Callback {
        // Configure commands available to the controller in onConnect()

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            super.onPostConnect(session, controller)
            session.setCustomLayout(controller, createCommandButton())
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            exoPlayer.setMediaItem(
                MediaItem.Builder()
                    .setUri("asset:///15-seconds-of-silence.mp3")
                    .also {
                        val metadata = MediaMetadata.Builder()
                            .setTitle("ExceptionCircumvent..")
                            .setAlbumTitle("converting..")
                            .setAlbumArtist("converting..")
                            .setArtist("converting..")
                            .build()
                        it.setMediaMetadata(metadata)
                    }
                    .build()
            )
            exoPlayer.play()
            return super.onPlaybackResumption(mediaSession, controller)
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {

            val connectionResult = super.onConnect(session, controller)


            val commandButtons = createCommandButton()
            val myCustomCommands = createMyCustomCommands()
            val sessionCommands = connectionResult.availableSessionCommands.buildUpon()

            commandButtons.forEach { commandButton ->
                commandButton.sessionCommand?.let {
                    sessionCommands.add(it)
                }
            }
            // Add custom commands
            myCustomCommands.forEach { customCommand ->
                sessionCommands.add(customCommand)
            }


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
                Actions.SET_BASS_BOOST -> {

                    val value=
                        customCommand.customExtras.getInt("value")
                    Log.d("액션을 받음","제발 $value")
                    useBassBoost(value)
                }
                Actions.SET_LOUDNESS_ENHANCER -> {
                    Log.d("라우드","받음")
                    val value=
                        customCommand.customExtras.getInt("value")
                    useLoudnessEnhancer(value)
                }
                Actions.SET_EQUALIZER -> {
                    Log.d("이퀼라이저","받음")
                    val value = customCommand.customExtras.getInt("value")
                    setEqualizer(value)

                }
                Actions.SET_VIRTUALIZER -> {
                    Log.d("버튜얼","받음")
                    val value=
                        customCommand.customExtras.getInt("value")
                    useVirtualizer(value)
                }
                Actions.SET_REVERB -> {
                    Log.d("리버브","받음")
                    val value=
                        customCommand.customExtras.getInt("value")
                    val sendLevel =
                        customCommand.customExtras.getInt("sendLevel")
                    usePresetReverb(value, sendLevel)

                }
                Actions.SET_ENVIRONMENT_REVERB -> {
                    useEnvironmentReverb()
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
    }

    fun startStream(videoId: String){

        streamingCancel()

        val defaultResolutionIndex = this.getSharedPreferences("defaultResolution", 0)
        val resolutionList = arrayOf("1080p", "720p", "480p", "360p", "240p", "144p")

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
                    sendFailBroadCast("empty url")
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
                sendFailBroadCast(e.toString())
                Log.d("원인","$e")
            }
        compositeDisposable.add(disposable)
    }

    private fun sendFailBroadCast(string: String){
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

        // single mode 일 때와 playlist mode 일 때 구분
        if (exoPlayer.mediaItemCount == 1){
            exoPlayer.addMediaItem(exoPlayer.currentMediaItemIndex, mediaItem)
            exoPlayer.seekTo(0,0)
            exoPlayer.removeMediaItem(exoPlayer.nextMediaItemIndex)
        }else{
            val mediaItemCount = exoPlayer.mediaItemCount
            val currentPosition = exoPlayer.currentMediaItemIndex
            if (currentPosition == mediaItemCount - 1){
                exoPlayer.addMediaItem(exoPlayer.currentMediaItemIndex, mediaItem)
                exoPlayer.seekToPreviousMediaItem()
                exoPlayer.removeMediaItem(exoPlayer.nextMediaItemIndex)
            }else{
                exoPlayer.addMediaItem(exoPlayer.nextMediaItemIndex, mediaItem)
                exoPlayer.seekToNextMediaItem()
                exoPlayer.removeMediaItem(exoPlayer.previousMediaItemIndex)
            }
        }

        exoPlayer.prepare()

        exoPlayer.playWhenReady = true
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("서비스의","언바인드")
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("서비스의","바인드")
        return super.onBind(intent)
    }

    override fun onDestroy() {
        Log.d("종료","서비스")
        streamingCancel()
        mediaSession?.run {
            Log.d("종료시 이 코드는","실행이 될까?")
            player.release()
            clearListener()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }


}

