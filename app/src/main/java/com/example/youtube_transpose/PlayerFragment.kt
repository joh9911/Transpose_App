package com.example.youtube_transpose

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youtube_transpose.databinding.ActivityMainBinding
import com.example.youtube_transpose.databinding.FragmentPlayerBinding
import com.example.youtube_transpose.databinding.MainBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.BuildConfig
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.FlacConstants
import com.google.android.exoplayer2.util.MimeTypes
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.abs

class PlayerFragment(videoData: VideoData): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    var fbinding: FragmentPlayerBinding? = null
    val binding get() = fbinding!!
    val API_KEY = "AIzaSyBZlnQ_kRZ7mvs0wL31ezbBeEPYAoIM3EM"

    val aray = ArrayList<VideoData>()
    val videoData = videoData

    var player: SimpleExoPlayer? = null
    lateinit var playerView: PlayerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentPlayerBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        Log.d("프레그먼트","onccreateview")
        initView()
        initMotionLayout()
        initYoutubeDL()
        initListener()
        getResultData()
        return view
    }
    fun initView(){
        binding.bottomPlayerCloseButton.setOnClickListener {
            getActivity()?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        binding.bottomTitleTextView.text = videoData.title
        binding.fragmentVideoTitle.text = videoData.title
        binding.fragmentVideoDetail.text = videoData.date
        binding.channelTextView.text = videoData.channel

        binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchResultAdapter = SearchResultFragmentRecyclerViewAdapter(aray)
        binding.fragmentRecyclerView.adapter = searchResultAdapter

    }
    private fun getResultData() {
        val retrofit = RetrofitVideo.initRetrofit()
        retrofit.create(RetrofitService::class.java).getVideoDetails(API_KEY,"snippet","cookie","50","video")
            .enqueue(object : Callback<VideoSearchData> {
                override fun onResponse(call: Call<VideoSearchData>, response: Response<VideoSearchData>) {
                    for (index in 0 until response.body()?.items?.size!!){
                        val thumbnail = response?.body()?.items?.get(index)?.snippet?.thumbnails?.high?.url!!
                        val date = response?.body()?.items?.get(index)?.snippet?.publishedAt!!.substring(0, 10)
                        val account = response?.body()?.items?.get(index)?.snippet?.channelTitle!!
                        val title = stringToHtmlSign(response?.body()?.items?.get(index)?.snippet?.title!!)
                        val videoId = response?.body()?.items?.get(index)?.id?.videoId!!
                        aray.add(VideoData(thumbnail, title, account, videoId, date))
                    }
                    Log.d("fragment 비디오 목록","$aray")
                    searchResultAdapter.notifyDataSetChanged()

                }
                override fun onFailure(call: Call<VideoSearchData>, t: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: ${t.message}")
                }
            })
    }
    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }

    fun setPitch(value: Int){
        val pitchValue = value*0.05.toFloat()
        val tempoValue = activity.tempoSeekBar.progress*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        player?.playbackParameters = param
    }

    fun setTempo(value: Int){
        val tempoValue = value*0.05.toFloat()
        val pitchValue = activity.pitchSeekBar.progress*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        player?.playbackParameters = param
    }
    private fun initListener(){
        playerView = binding.playerView
        player = SimpleExoPlayer.Builder(activity)
            .build()
        playerView.player = player
        startStream()
    }
    private fun startStream(){
        Log.d("vdid","${videoData.videoId}")
        val youtubeUrl = "https://www.youtube.com/watch?v=${videoData.videoId}"
        binding.bottomTitleTextView.text = videoData.title
        val url = youtubeUrl.trim()
        if (TextUtils.isEmpty(url)){
            Toast.makeText(activity, "url오류", Toast.LENGTH_SHORT).show()
        }
//        pbLoading.visibility = View.VISIBLE

        val disposable: Disposable = Observable.fromCallable {
            val request = YoutubeDLRequest(url)
            // best stream containing video+audio
            request.addOption("-f b", "")

            YoutubeDL.getInstance().getInfo(request)
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ streamInfo ->
//                pbLoading.visibility = View.GONE
                Log.d("정보","$streamInfo")
                val videoUrl: String = streamInfo.url
                if (TextUtils.isEmpty(videoUrl)) {
                    Toast.makeText(
                        activity,
                        "failed to get stream url",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d("유알엘","$videoUrl")
                    setupVideoView(videoUrl)
                }
            }) { e ->
                if (BuildConfig.DEBUG) Log.e(ContentValues.TAG, "failed to get stream info", e)
//                pbLoading.visibility = View.GONE
                Toast.makeText(
                    activity,
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
        setTempo(activity.tempoSeekBar.progress)
        setPitch(activity.pitchSeekBar.progress)
        player?.play()
//        videoView.setVideoURI(Uri.parse(videoUrl))
    }

    private fun initYoutubeDL(){
        try {
            YoutubeDL.getInstance().init(activity)
        } catch (e: YoutubeDLException) {
            Log.e(ContentValues.TAG, "failed to initialize youtubedl-android", e)
        }
    }


    private fun initMotionLayout() {
        binding.playerMotionLayout.setTransitionListener(object :
            MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange( // 재정의를 통해 메인 엑티비티(모션 레이아웃)과 연동한다.
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {

                /**
                 * 메인 엑티비티 모션 레이아웃에 값을 전달
                 */
                /**
                 * Fragment 는 자기 단독으로 존재할 수 없기 떄문에 activity 가 존재 할수밖에 없고
                 * activity 를 가져오면 해당 Fragment 가 attach 되어있는 액티비티를 가져온다.
                 */
                (activity as Activity).also { main ->
                    main.findViewById<MotionLayout>(mainBinding.mainMotionLayout.id).progress =
                        abs(progress)
                }
                if (binding.playerMotionLayout.currentState == R.id.end)
                    binding.bottomPlayerCloseButton.visibility = View.INVISIBLE
                else
                    binding.bottomPlayerCloseButton.visibility = View.VISIBLE
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()

        fbinding = null
        player?.release()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onDetach() {
        super.onDetach()
    }


}