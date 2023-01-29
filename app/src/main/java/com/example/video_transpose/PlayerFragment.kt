package com.example.video_transpose

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.video_transpose.databinding.FragmentPlayerBinding
import com.example.video_transpose.databinding.MainBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import java.lang.Math.abs


class PlayerFragment(videoDataList: ArrayList<VideoData>, position: Int, mode: String): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    lateinit var playlistItemsRecyclerViewAdapter: PlaylistItemsRecyclerViewAdapter
    var fbinding: FragmentPlayerBinding? = null
    val binding get() = fbinding!!
    var playerModel = PlayerModel(playMusicList = videoDataList,
    currentPosition = position)


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
        Log.d("현재 포지션","${playerModel.getCurrentPosition()}")
        Log.d("현재 곡","${playerModel.currentMusicModel()}")
        initView()
        initMotionLayout()
        initRecyclerView()
        initListener()
        return view
    }

    override fun onPause() {
        super.onPause()
        Log.d("프레그먼트","온퍼즈")
    }

    override fun onResume() {
        super.onResume()
        Log.d("프레그먼트","온리줌")
    }

    override fun onStop() {
        super.onStop()
        Log.d("프레그먼트","온스탑")
    }

    override fun onStart() {
        super.onStart()
        Log.d("프레그먼트","온스타트")
    }

    private fun initView(){
        val currentPlayingVideoData = playerModel.currentMusicModel()!!
        binding.bottomPlayerCloseButton.setOnClickListener {
            getActivity()?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        binding.bottomTitleTextView.text = currentPlayingVideoData.title
        binding.fragmentVideoTitle.text = currentPlayingVideoData.title
        binding.fragmentVideoDetail.text = currentPlayingVideoData.date
        binding.channelTextView.text = currentPlayingVideoData.channel
        Glide.with(binding.channelImageView)
            .load(currentPlayingVideoData.channelThumbnail)
            .into(binding.channelImageView)
        binding.fragmentTitleLinearLayout.setOnClickListener {
        }
        binding.channelLinearLayout.setOnClickListener {
        }
    }

    private fun initRecyclerView(){
        val videoDataList = playerModel.getPlayMusicList()
        if (mode == "playlist"){
            binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
            playlistItemsRecyclerViewAdapter = PlaylistItemsRecyclerViewAdapter()
            playlistItemsRecyclerViewAdapter.setItemClickListener(object: PlaylistItemsRecyclerViewAdapter.OnItemClickListener{
                override fun onClick(v: View, position: Int) {
                        replaceVideo(position)
                        playCurrentVideo()
                }
            })
            playlistItemsRecyclerViewAdapter.submitList(videoDataList)
            binding.fragmentRecyclerView.adapter = playlistItemsRecyclerViewAdapter
        }

        if (mode == "video"){
            binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
            searchResultAdapter = SearchResultFragmentRecyclerViewAdapter()
            searchResultAdapter.setItemClickListener(object: SearchResultFragmentRecyclerViewAdapter.OnItemClickListener{

                override fun channelClick(v: View, position: Int) {

                }
                override fun videoClick(v: View, position: Int) {
                    Log.d("누른 포지션","$position")
                    replaceVideo(position)
                    playCurrentVideo()

                }

                override fun optionButtonClick(v: View, position: Int) {

                }
            })
            searchResultAdapter.submitList(videoDataList)
            binding.fragmentRecyclerView.adapter = searchResultAdapter
        }
    }

    fun playPrevVideo(){
        activity.videoService!!.playVideo(playerModel.prevMusic()!!)
        playerModel.refreshPlaylist()
        if (mode == "playlist")
            playlistItemsRecyclerViewAdapter.submitList(playerModel.getPlayMusicList())
        if (mode == "video")
            searchResultAdapter.submitList(playerModel.getPlayMusicList())
        updatePlayerView()
    }

    fun playNextVideo(){
        activity.videoService!!.playVideo(playerModel.nextMusic()!!)
        playerModel.refreshPlaylist()
        if (mode == "playlist")
            playlistItemsRecyclerViewAdapter.submitList(playerModel.getPlayMusicList())
        if (mode == "video")
            searchResultAdapter.submitList(playerModel.getPlayMusicList())
        updatePlayerView()
    }

    fun playCurrentVideo(){
        activity.videoService!!.playVideo(playerModel.currentMusicModel()!!)
    }

    fun replaceVideo(position: Int){
        binding.fragmentRecyclerView.scrollToPosition(position)
        playerModel.updateCurrentPosition(position)
        playerModel.refreshPlaylist()

        if (mode == "playlist"){
            playlistItemsRecyclerViewAdapter.submitList(playerModel.getPlayMusicList())

        }
        if (mode == "video"){
            searchResultAdapter.submitList(playerModel.getPlayMusicList())
        }
        updatePlayerView()
    }

    private fun updatePlayerView(){
        Log.d("updatePlayerVIew","${playerModel.getCurrentPosition()}")
        val currentVideoData = playerModel.currentMusicModel()!!
        Log.d("updatePlayerVIew","${playerModel.getCurrentPosition()}")
        binding.bottomTitleTextView.text = currentVideoData.title
        binding.fragmentVideoTitle.text = currentVideoData.title
        binding.fragmentVideoDetail.text = currentVideoData.date
        binding.channelTextView.text = currentVideoData.channel
        Glide.with(binding.channelImageView)
            .load(currentVideoData.channelThumbnail)
            .into(binding.channelImageView)
    }

    private fun initListener(){
        playerView = binding.playerView
        player = activity.exoPlayer
        playerView.player = player
        activity.videoService!!.playVideo(playerModel.currentMusicModel()!!)
        binding.fragmentRecyclerView.scrollToPosition(playerModel.getCurrentPosition())
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
        Log.d("프레그먼트의","onDestroy")
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