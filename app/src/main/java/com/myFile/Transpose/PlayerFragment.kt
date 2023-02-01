package com.myFile.Transpose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.Transpose.databinding.FragmentPlayerBinding
import com.myFile.Transpose.databinding.MainBinding
import com.google.android.exoplayer2.ExoPlayer
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

    private val mediaReceiver = MediaReceiver()


    val position = position
    val mode = mode

    var player: ExoPlayer? = null
    lateinit var playerView: PlayerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentPlayerBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initView()
        initMotionLayout()
        initRecyclerView()
        initListener()
        return view
    }

    inner class MediaReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                player?.playWhenReady = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activity.unregisterReceiver(mediaReceiver)
    }

    override fun onResume() {
        super.onResume()
        activity.registerReceiver(mediaReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
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
            activity.supportFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.bottomPlayerPauseButton.setOnClickListener {
            if (player?.isPlaying!!){
                player?.pause()
                binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            else{
                player?.play()
                binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_pause_24)
            }

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

    fun settingBottomPlayButton(){
        if (player?.isPlaying!!)
            binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_pause_24)
        else
            binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
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
        val currentVideoData = playerModel.currentMusicModel()!!
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
                // 화면이 축소된 상태에서는 엑소 플레이어의 컨트롤러 없애기
                if (binding.playerMotionLayout.currentState == R.id.start){
                    settingBottomPlayButton()
                    binding.playerView.useController = false
                }
                else
                    binding.playerView.useController = true
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