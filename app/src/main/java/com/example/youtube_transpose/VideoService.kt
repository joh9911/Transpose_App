package com.example.youtube_transpose

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
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
    lateinit var videoDetailData: VideoData
    lateinit var activity: Activity

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
        Log.d("비디오서비스","온디스트로이")
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
                        startForegroundService()
                        if (!exoPlayer.isPlaying)
                            stopForegroundService()
                    }
                    Player.STATE_ENDED -> {
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
                Actions.PREV -> Log.e(TAG, "Clicked = 이전")
                Actions.PLAY -> {
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                }
                Actions.NEXT -> Log.e(TAG, "Clicked = 다음")
            }
            return START_STICKY

    }

    private fun startForegroundService() {
        val notification = MusicNotification.createNotification(this, this)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForegroundService() {

        stopForeground(false)
        stopSelf()
    }

    fun saveVideoData(videoData: VideoData){
        videoDetailData = videoData
    }

    fun initActivity(param: Activity) {
        activity = param
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

}