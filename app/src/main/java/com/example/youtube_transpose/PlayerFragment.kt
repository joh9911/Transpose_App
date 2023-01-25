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
import com.bumptech.glide.Glide
import com.example.youtube_transpose.databinding.FragmentPlayerBinding
import com.example.youtube_transpose.databinding.MainBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.BuildConfig
import com.google.android.exoplayer2.ui.PlayerNotificationManager
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

    val videoDataList = videoDataList
    val position = position
    val mode = mode

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
        initRecyclerView()
        initListener()
//        playlistUrl()
        Log.d("작업이 끝난후","실행되나?")
        return view
    }
    fun initView(){
        Log.d("이니셜라이즈 ","뷰")
        binding.bottomPlayerCloseButton.setOnClickListener {
            activity.exoPlayer.stop()
            getActivity()?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        binding.bottomTitleTextView.text = videoDataList[position].title
        binding.fragmentVideoTitle.text = videoDataList[position].title
        binding.fragmentVideoDetail.text = videoDataList[position].date
        binding.channelTextView.text = videoDataList[position].channel
        Glide.with(binding.channelImageView)
            .load(videoDataList[position].channelThumbnail)
            .into(binding.channelImageView)
        binding.fragmentTitleLinearLayout.setOnClickListener {
            Log.d("난 타이틀을","클릭ㄱ했다")
        }
        binding.channelLinearLayout.setOnClickListener {
            Log.d("채널을","클릭했다")
        }
    }
    fun settingVideoData(videoData: VideoData){
        binding.bottomTitleTextView.text = videoData.title
        binding.fragmentVideoTitle.text = videoData.title
        binding.fragmentVideoDetail.text = videoData.date
        binding.channelTextView.text = videoData.channel
        Glide.with(binding.channelImageView)
            .load(videoData.channelThumbnail)
            .into(binding.channelImageView)
    }


    fun initRecyclerView(){
        if (mode == "playlist"){
            binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
            playlistItemsRecyclerViewAdapter = PlaylistItemsRecyclerViewAdapter(videoDataList, position)
            playlistItemsRecyclerViewAdapter.setItemClickListener(object: PlaylistItemsRecyclerViewAdapter.OnItemClickListener{
                var mLastClickTime = 0L
                override fun onClick(v: View, position: Int) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime > 1000){
                        settingVideoData(videoDataList[position])
                        activity.videoService!!.playVideo(position)
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                }
            })
            binding.fragmentRecyclerView.adapter = playlistItemsRecyclerViewAdapter
        }

        if (mode == "video"){
            binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
            searchResultAdapter = SearchResultFragmentRecyclerViewAdapter(videoDataList)
            searchResultAdapter.setItemClickListener(object: SearchResultFragmentRecyclerViewAdapter.OnItemClickListener{

                override fun channelClick(v: View, position: Int) {

                }

                override fun videoClick(v: View, position: Int) {
                    settingVideoData(videoDataList[position])
                    activity.videoService!!.playVideo(position)
                }

                override fun optionButtonClick(v: View, position: Int) {

                }
            })
            binding.fragmentRecyclerView.adapter = searchResultAdapter
        }
    }

    private fun initListener(){
        playerView = binding.playerView
        player = activity.exoPlayer
        playerView.player = player
        activity.videoService!!.saveVideoData(videoDataList)
        activity.videoService!!.playVideo(position)
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
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        activity.videoService!!.stopForegroundService()
        fbinding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onDetach() {
        super.onDetach()
    }


}