package com.example.youtube_transpose

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class MainActivity : AppCompatActivity(), View.OnClickListener {
    var player: SimpleExoPlayer? = null
    lateinit var btnStartStream: Button
    lateinit var etUrl: EditText
    lateinit var playerView: PlayerView
    lateinit var pbLoading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initListeners()
    }

    private fun initView(){
        btnStartStream = findViewById(R.id.btn_start_streaming)
        etUrl = findViewById(R.id.et_url)
        playerView = findViewById(R.id.video_view)
        pbLoading = findViewById(R.id.pb_status)
    }

    private fun initListeners(){
        btnStartStream.setOnClickListener(this)
        player = SimpleExoPlayer.Builder(this@MainActivity)
            .build()
        val url = "https://www.youtube.com/watch?v=hYcYjEt1Niw"
        playerView.player = player
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_start_streaming -> {
                startStream()
            }
        }
    }

    private fun startStream(){
        val url = etUrl.text.toString().trim()
        if (TextUtils.isEmpty(url)){
            Toast.makeText(this, "url오류", Toast.LENGTH_SHORT).show()
        }
        pbLoading.visibility = View.VISIBLE

        val disposable: Disposable = Observable.fromCallable {
            val request = YoutubeDLRequest(url)
            // best stream containing video+audio
            request.addOption("-f", "best")
            YoutubeDL.getInstance().getInfo(request)
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(Android)
            .subscribe({ streamInfo ->
                pbLoading.visibility = View.GONE
                val videoUrl: String = streamInfo.getUrl()
                if (TextUtils.isEmpty(videoUrl)) {
                    Toast.makeText(
                        this@StreamingExampleActivity,
                        "failed to get stream url",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    setupVideoView(videoUrl)
                }
            }) { e ->
                if (BuildConfig.DEBUG) Log.e(TAG, "failed to get stream info", e)
                pbLoading.visibility = View.GONE
                Toast.makeText(
                    this@StreamingExampleActivity,
                    "streaming failed. failed to get stream info",
                    Toast.LENGTH_LONG
                ).show()
            }
        compositeDisposable.add(disposable)
    }


}