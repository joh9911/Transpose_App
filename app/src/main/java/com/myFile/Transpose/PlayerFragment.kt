package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.Transpose.databinding.FragmentPlayerBinding
import com.myFile.Transpose.databinding.MainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.*
import java.lang.Math.abs
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


class PlayerFragment(videoDataList: List<VideoData>,val channelDataList: ArrayList<ChannelData>, position: Int, mode: String): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    lateinit var playlistItemsRecyclerViewAdapter: PlaylistItemsRecyclerViewAdapter
    var fbinding: FragmentPlayerBinding? = null
    val binding get() = fbinding!!
    var playerModel = PlayerModel(playMusicList = videoDataList,
    currentPosition = position)

    private lateinit var coroutineExceptionHandler: CoroutineExceptionHandler

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
        activity.videoService!!.initPlayerFragment(this)
        val view = binding.root
        initExceptionHandler()
        getData()
        initView()
        initMotionLayout()
        initRecyclerView()
        initListener()
        return view
    }
    private fun initExceptionHandler(){
        coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
            Log.d("코루틴 에러","$throwable")
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(activity,R.string.network_error_message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getData() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            async { getVideoDetail() }
            async { getChannelData() }
        }
    }

    private suspend fun getVideoDetail(){
        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java)
            .getVideoDetail(BuildConfig.API_KEY, "statistics",playerModel.currentMusicModel()?.videoId!!)
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    binding.fragmentVideoDetail.text = viewCountCalculator(response.body()!!.items[0].statistics?.viewCount!!)
                }
            }
        }
    }
    private fun viewCountCalculator(viewCountString: String): String {
        val country = Locale.getDefault().language
        Log.d("viewCount","${country}")
        val viewCount = viewCountString.toInt()
        val df = DecimalFormat("#.#")
        var string = ""
        if (country == "ko"){
            if (viewCount < 1000)
                string = String.format(activity.resources.getString(R.string.view_count_under_thousand), viewCount.toString())
            else if (viewCount in 1000..9999){
                val convertedViewCount = df.format(viewCount/1000.0)
                string = String.format(activity.resources.getString(R.string.view_count_over_thousand), convertedViewCount)
            }
            else if (viewCount in 10000 .. 99999){
                val convertedViewCount = df.format(viewCount/10000.0)
                string = String.format(activity.resources.getString(R.string.view_count_over_hundred_thousand), convertedViewCount)
            }
            else if (viewCount in 100000.. 99999999){
                val convertedViewCount = (viewCount / 10000).toString()
                string = String.format(activity.resources.getString(R.string.view_count_over_hundred_thousand), convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(viewCount/100000000.0)
                string = String.format(activity.resources.getString(R.string.view_count_over_hundred_million), convertedViewCount)
            }
        }
        else{
            if (viewCount < 1000)
                string = String.format(activity.resources.getString(R.string.view_count_under_thousand), viewCount)
            else if (viewCount in 1000..9999){
                val convertedViewCount = df.format(viewCount/1000.0)
                string = String.format(activity.resources.getString(R.string.view_count_over_thousand), convertedViewCount)
            }
            else if (viewCount in 10000..999999){
                val convertedViewCount = (viewCount/1000).toString()
                string = String.format(activity.resources.getString(R.string.view_count_over_thousand), convertedViewCount)
            }
            else if (viewCount in 1000000..9999999){
                val convertedViewCount = df.format(viewCount/1000000.0)
                string = String.format(activity.resources.getString(R.string.view_count_over_million), convertedViewCount)
            }
            else if (viewCount in 10000000..999999999){
                val convertedViewCount = (viewCount/1000000).toString()
                string = String.format(activity.resources.getString(R.string.view_count_over_million), convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(viewCount/1000000000.0)
                string = String.format(activity.resources.getString(R.string.view_count_over_billion), convertedViewCount)
            }
        }
        return string
    }

    private suspend fun getChannelData(){

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
        binding.channelTextView.text = currentPlayingVideoData.channelTitle

        Glide.with(binding.playerThumbnailView)
            .load(currentPlayingVideoData.thumbnail)
            .placeholder(R.drawable.black_background)
            .into(binding.playerThumbnailView)


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

    fun playerViewInvisibleEvent(){
        binding.playerView.visibility = View.INVISIBLE
        binding.bufferingProgressBar.visibility = View.VISIBLE
        binding.playerThumbnailView.visibility = View.VISIBLE
    }

    fun playerViewVisibleEvent(){
        binding.playerView.visibility = View.VISIBLE
        binding.bufferingProgressBar.visibility = View.GONE
        binding.playerThumbnailView.visibility = View.GONE
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
                    binding.playerMotionLayout.transitionToState(R.id.start)
//                    activity.supportFragmentManager.beginTransaction()
//                        .replace(activity.binding.anyFrameLayout.id,ChannelFragment(channelDataList[position]))
//                        .addToBackStack(null)
//                        .commit()
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
        binding.channelTextView.text = currentVideoData.channelTitle

        Glide.with(binding.playerThumbnailView)
            .load(currentVideoData.thumbnail)
            .centerCrop()
            .placeholder(R.drawable.black_background)
            .into(binding.playerThumbnailView)

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

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {


                (activity).also { main ->
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
    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        binding.fragmentRecyclerView.scrollToPosition(playerModel.getCurrentPosition())
        Log.d("리줌","왜안되지")
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        Log.d("프레그먼트","온스탑")
    }

    override fun onStart() {
        super.onStart()
        Log.d("프레그먼트","온스타트")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onDetach() {
        super.onDetach()
    }


}