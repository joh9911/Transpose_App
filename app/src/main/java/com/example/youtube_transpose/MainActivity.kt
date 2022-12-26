package com.example.youtube_transpose

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.BuildConfig
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class MainActivity : AppCompatActivity(){
    var player: SimpleExoPlayer? = null
    lateinit var btnStartStream: Button
    lateinit var etUrl: EditText
    lateinit var playerView: PlayerView
    lateinit var videoView: VideoView
    lateinit var pbLoading: ProgressBar
    lateinit var pitch: EditText
    lateinit var ptichBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "failed to initialize youtubedl-android", e)
        }
        initView()
        initListeners()
    }

    private fun initView(){
        btnStartStream = findViewById(R.id.btn_start_streaming)
        etUrl = findViewById(R.id.et_url)
        playerView = findViewById(R.id.video_view)
        pbLoading = findViewById(R.id.pb_status)
        pitch = findViewById(R.id.ptich)
        ptichBtn = findViewById(R.id.btn)
    }

    private fun initListeners(){
//        videoView.setOnPreparedListener { videoView.start() }
        player = SimpleExoPlayer.Builder(this@MainActivity)
            .build()
        val url = "https://www.youtube.com/watch?v=hYcYjEt1Niw"
        playerView.player = player
        btnStartStream.setOnClickListener {
            startStream()
        }
        ptichBtn.setOnClickListener {
            if (pitch.text != null){
                val value = pitch.text.toString().toFloat()
                val param = PlaybackParameters(1f, value)
                player?.playbackParameters = param
            }
        }
    }

//    override fun onClick(v: View?) {
//        when (v!!.id) {
//            R.id.btn_start_streaming -> {
//                startStream()
//            }
//            R.id.btn -> {
//                if (pitch.text != null){
//                    val value = pitch.text.toString().toFloat()
//                    val param = PlaybackParameters(1f, value)
//                    player?.playbackParameters = param
//                }
//            }
//        }
//    }

    private fun startStream(){
        val Url = "https://www.youtube.com/watch?v=VOmIplFAGeg"
        val url = etUrl.text.toString().trim()
        if (TextUtils.isEmpty(url)){
            Toast.makeText(this, "url오류", Toast.LENGTH_SHORT).show()
        }
        pbLoading.visibility = View.VISIBLE

        val disposable: Disposable = Observable.fromCallable {
            val request = YoutubeDLRequest(url)
            // best stream containing video+audio
            request.addOption("-f b", "")

            YoutubeDL.getInstance().getInfo(request)
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ streamInfo ->
                pbLoading.visibility = View.GONE
                Log.d("정보","$streamInfo")
                val videoUrl: String = streamInfo.url
                if (TextUtils.isEmpty(videoUrl)) {
                    Toast.makeText(
                        this,
                        "failed to get stream url",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    setupVideoView(videoUrl)
                }
            }) { e ->
                if (BuildConfig.DEBUG) Log.e(TAG, "failed to get stream info", e)
                Log.d("올퓨","$e")
                pbLoading.visibility = View.GONE
                Toast.makeText(
                    this,
                    "streaming failed. failed to get stream info",
                    Toast.LENGTH_LONG
                ).show()
            }
        CompositeDisposable().add(disposable)
    }
    private fun setupVideoView(videoUrl: String){
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .build()
        player?.setMediaItem(mediaItem)
//        val audioSource = ProgressiveMediaSource
//            .Factory(DefaultHttpDataSource.Factory())
//            .createMediaSource(MediaItem.fromUri(videoUrl))
        val videoSource = ProgressiveMediaSource
            .Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(videoUrl))
        player?.setMediaSource(videoSource)
        player?.prepare()
        player?.play()
//        videoView.setVideoURI(Uri.parse(videoUrl))
    }

}