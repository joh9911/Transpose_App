package com.example.youtube_transpose

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youtube_transpose.databinding.FragmentPlayerBinding
import com.example.youtube_transpose.databinding.MainBinding
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
import java.lang.Math.abs

class PlayerFragment(videoDataList: ArrayList<VideoData>, position: Int, mode: String): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    lateinit var playlistItemsRecyclerViewAdapter: PlaylistItemsRecyclerViewAdapter
    var fbinding: FragmentPlayerBinding? = null
    val binding get() = fbinding!!
    val API_KEY = com.example.youtube_transpose.BuildConfig.API_KEY

    val videoDataList = videoDataList
    val position = position
    val mode = mode

    val playlists = arrayListOf<String>()

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
        initListener(videoDataList[position].videoId)
        return view
    }
    fun initView(){
        binding.bottomPlayerCloseButton.setOnClickListener {
            getActivity()?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        binding.bottomTitleTextView.text = videoDataList[position].title
        binding.fragmentVideoTitle.text = videoDataList[position].title
        binding.fragmentVideoDetail.text = videoDataList[position].date
        binding.channelTextView.text = videoDataList[position].channel
        binding.fragmentTitleLinearLayout.setOnClickListener {
            Log.d("난 타이틀을","클릭ㄱ했다")
        }
    }
    fun settingVideoData(videoData: VideoData){
        binding.bottomTitleTextView.text = videoData.title
        binding.fragmentVideoTitle.text = videoData.title
        binding.fragmentVideoDetail.text = videoData.date
        binding.channelTextView.text = videoData.channel
    }

    override fun onStart() {
        initRecyclerView()
        super.onStart()
    }
    fun initRecyclerView(){
        if (mode == "playlist"){
            Log.d("초기화함수","$position")
            binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
            playlistItemsRecyclerViewAdapter = PlaylistItemsRecyclerViewAdapter(videoDataList, position)
            playlistItemsRecyclerViewAdapter.setItemClickListener(object: PlaylistItemsRecyclerViewAdapter.OnItemClickListener{
                var mLastClickTime = 0L
                override fun onClick(v: View, position: Int) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime > 1000){
                        settingVideoData(videoDataList[position])
                        startStream(videoDataList[position].videoId)
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                }
            })
            binding.fragmentRecyclerView.adapter = playlistItemsRecyclerViewAdapter
        }

        if (mode == "video"){
            binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
            searchResultAdapter = SearchResultFragmentRecyclerViewAdapter(videoDataList)
            binding.fragmentRecyclerView.adapter = searchResultAdapter
        }
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
    private fun initListener(videoId: String){
        playerView = binding.playerView
        player = SimpleExoPlayer.Builder(activity)
            .build()
        playerView.player = player
        startStream(videoId)
    }
    private fun startStream(videoId: String){
        Log.d("vdid","${videoId}")
        if (player?.isPlaying!!)
            player?.removeMediaItem(0)
        val youtubeUrl = "https://www.youtube.com/watch?v=${videoId}"
        val url = youtubeUrl.trim()
        if (TextUtils.isEmpty(url)){
            Toast.makeText(activity, "url오류", Toast.LENGTH_SHORT).show()
        }

        val disposable: Disposable = Observable.fromCallable {
            val request = YoutubeDLRequest(url)
            // best stream containing video+audio
            request.addOption("-f b", "")

            YoutubeDL.getInstance().getInfo(request)
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ streamInfo ->
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
                Toast.makeText(
                    activity,
                    "streaming failed. failed to get stream info",
                    Toast.LENGTH_LONG
                ).show()
            }
        CompositeDisposable().add(disposable)
    }

    private fun setupVideoView(videoUrl: String){
        Log.d("셋업","비디오")
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