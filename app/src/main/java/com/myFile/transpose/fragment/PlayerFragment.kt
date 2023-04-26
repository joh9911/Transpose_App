package com.myFile.transpose.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.transpose.databinding.FragmentPlayerBinding
import com.myFile.transpose.databinding.MainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.myFile.transpose.*
import com.myFile.transpose.retrofit.*
import com.myFile.transpose.adapter.MyPlaylistItemRecyclerViewAdapter
import com.myFile.transpose.adapter.RelatedVideoRecyclerViewAdapter
import com.myFile.transpose.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.dto.ChannelSearchData
import com.myFile.transpose.dto.CommentThreadData
import com.myFile.transpose.dto.RelatedVideoData
import com.myFile.transpose.model.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import java.lang.Math.abs
import kotlin.collections.ArrayList


class PlayerFragment(private val videoData: VideoData, val playlistModel: PlaylistModel?): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var relatedVideoRecyclerViewAdapter: RelatedVideoRecyclerViewAdapter
    lateinit var myPlaylistItemRecyclerViewAdapter: MyPlaylistItemRecyclerViewAdapter
    var fbinding: FragmentPlayerBinding? = null
    val binding get() = fbinding!!

    private lateinit var nowPlaylistModel: NowPlaylistModel

    private lateinit var retrofit: Retrofit

    private val relatedVideoList = ArrayList<VideoData>()
    private val commentList = ArrayList<CommentData>()
    private lateinit var currentVideoDetailData: VideoDetailData
    private lateinit var currentChannelSearchData: ChannelSearchData

    var player: ExoPlayer? = null
    lateinit var playerView: PlayerView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var callbackPlaylistVersion: OnBackPressedCallback
    private lateinit var callback: OnBackPressedCallback

    private lateinit var playModeSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentPlayerBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        activity.videoService!!.initPlayerFragment(this)
        retrofit = RetrofitData.initRetrofit()

        val view = binding.root
        initRecyclerView()
        getDetailData(videoData.videoId)
        initView()
        setMotionLayoutListenerForInitialize()
        initListener()
        initPlaylistView()
        return view
    }


    private fun initPlaylistView(){
        if (playlistModel != null){
            nowPlaylistModel = NowPlaylistModel(playlistModel.playlistItems, playlistModel.firstPosition, playlistModel.playlistName)
            initBottomSheet()
            initPlaylistRecyclerView()
        }
    }

    private fun initPlaylistRecyclerView(){
        binding.playlistTitleInLinearLayout.text = String.format(resources.getString(R.string.playlist_text),"${nowPlaylistModel.getPlaylistTitle()}")
        binding.playlistRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPlaylistItemRecyclerViewAdapter = MyPlaylistItemRecyclerViewAdapter()
        myPlaylistItemRecyclerViewAdapter.setItemClickListener(object: MyPlaylistItemRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                replacePlaylistVideo(position)
            }
            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showNoticeDialog(nowPlaylistModel.getPlayMusicList()[position])
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
        myPlaylistItemRecyclerViewAdapter.submitList(nowPlaylistModel.getPlayMusicList().toMutableList())
        binding.playlistRecyclerView.adapter = myPlaylistItemRecyclerViewAdapter
    }

    private fun initBottomSheet(){
        playModeSharedPreferences = activity.getSharedPreferences("play_mode_preferences", Context.MODE_PRIVATE)

        binding.playlistTitleBottomSheet.text = nowPlaylistModel.getPlaylistTitle()
        bottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet)
        binding.coordinator.visibility = View.INVISIBLE
        bottomSheetBehavior.peekHeight = 20
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_EXPANDED -> Log.d("바텀시트","Expanded")
                    BottomSheetBehavior.STATE_DRAGGING -> Log.d("바텀시트","dragging")
                    BottomSheetBehavior.STATE_COLLAPSED -> Log.d("바텀시트","collapsed")
                    BottomSheetBehavior.STATE_HIDDEN -> Log.d("바텀시트","hidden")
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.coordinator.alpha = slideOffset
            }

        })
        val playlistLinearLayout = binding.playlistLinearLayout

        playlistLinearLayout.visibility = View.VISIBLE
        playlistLinearLayout.setOnClickListener {
            binding.coordinator.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.playlistRecyclerView.scrollToPosition(nowPlaylistModel.getCurrentPosition())
        }
        val bottomSheetCloseButton = binding.bottomSheetCloseButton
        bottomSheetCloseButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val playModeIconButton = binding.playModeIcon
        val playMode = playModeSharedPreferences.getInt("play_mode",0)
        if (playMode == 0)
            playModeIconButton.setImageResource(R.drawable.loop_4)
        else
            playModeIconButton.setImageResource(R.drawable.loop_1)
        playModeIconButton.setOnClickListener {
            if (playModeSharedPreferences.getInt("play_mode",0) == 0){
                playModeSharedPreferences.edit().putInt("play_mode",1).apply()
                playModeIconButton.setImageResource(R.drawable.loop_1)
            }
            else{
                playModeSharedPreferences.edit().putInt("play_mode",0).apply()
                playModeIconButton.setImageResource(R.drawable.loop_4)
            }

        }
    }


    private fun getDetailData(videoId: String) {
        relatedVideoList.clear()
        CoroutineScope(Dispatchers.IO + CoroutineExceptionObject.coroutineExceptionHandler).launch {
            async {getVideoDetail(videoId)}
//            async {getRelatedVideo(videoId)}
            async { getCommentThread(videoId) }
        }
    }

    private suspend fun getVideoDetail(videoId: String) {
        val response = retrofit.create(RetrofitService::class.java)
            .getVideoDetail(BuildConfig.API_KEY3, "snippet, statistics",videoId)
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                getChannelData(response.body()!!)
            }
        }
    }

    private suspend fun getChannelData(videoDetailResponseData: VideoDetailData) {
        val response = retrofit.create(RetrofitService::class.java)
            .getChannelData(
                BuildConfig.API_KEY4, "snippet, contentDetails, statistics, brandingSettings"
                ,videoDetailResponseData.items[0].snippet?.channelId)
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    detailMapping(videoDetailResponseData, response.body()!!)
                }
            }
        }
    }

    private fun detailMapping(videoDetailResponseData: VideoDetailData, channelDetailResponseData: ChannelSearchData){
        currentChannelSearchData = channelDetailResponseData
        currentVideoDetailData = videoDetailResponseData
        val youtubeDigitConverter = YoutubeDigitConverter(activity)
        relatedVideoRecyclerViewAdapter.setHeaderViewData(HeaderViewData(videoDetailResponseData.items[0].snippet?.title!!,
            youtubeDigitConverter.viewCountCalculator(videoDetailResponseData.items[0].statistics?.viewCount!!),
            youtubeDigitConverter.intervalBetweenDateText(videoDetailResponseData.items[0].snippet?.publishedAt!!),
            channelDetailResponseData.items[0].snippet?.title!!,
            channelDetailResponseData.items[0].snippet?.thumbnails?.high?.url!!,
            youtubeDigitConverter.subscriberCountConverter(channelDetailResponseData.items[0].statistics?.subscriberCount!!)
        ),activity)
    }
    private suspend fun getCommentThread(videoId: String){
        val response = retrofit.create(RetrofitService::class.java)
            .getCommentThreads(BuildConfig.RAISE_DEVELOP,"snippet",videoId,"100","relevance",null, "plainText")
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    commentMapping(response.body()!!)
                }
            }
            else{
                withContext(Dispatchers.Main){
                    commentList.clear()
                    relatedVideoRecyclerViewAdapter.notifyDataSetChanged()
                    binding.relatedVideoProgressBar.visibility = View.INVISIBLE
                    binding.errorTextView.visibility = View.VISIBLE
                }

            }
        }
        else{
            withContext(Dispatchers.Main){
                commentList.clear()
                relatedVideoRecyclerViewAdapter.notifyDataSetChanged()
                binding.relatedVideoProgressBar.visibility = View.INVISIBLE
                binding.errorTextView.visibility = View.VISIBLE
            }
        }
    }
    private fun commentMapping(body: CommentThreadData) {
        commentList.clear()
        val items = body.items
        val youtubeDigitConverter = YoutubeDigitConverter(activity)
        for (index in items.indices){
            val authorName = items[index].snippet?.topLevelComment?.snippet?.authorDisplayName!!
            val authorImage = items[index].snippet?.topLevelComment?.snippet?.authorProfileImageUrl!!
            val date = items[index].snippet?.topLevelComment?.snippet?.publishedAt!!
            val commentTime = youtubeDigitConverter.intervalBetweenDateText(date)
            val commentText = items[index].snippet?.topLevelComment?.snippet?.textDisplay!!
            commentList.add(CommentData(authorName,authorImage,commentTime,commentText))
        }
        binding.errorTextView.visibility = View.GONE
        binding.relatedVideoProgressBar.visibility = View.GONE
        relatedVideoRecyclerViewAdapter.notifyDataSetChanged()
    }

    private suspend fun getRelatedVideo(videoId: String) {
        val response = retrofit.create(RetrofitService::class.java)
            .getRelatedVideo(BuildConfig.API_KEY9,"id, snippet",videoId,"video","50")
        if (response.isSuccessful) {
            if (response.body()?.RelatedVideoDataitems?.size != 0) {
                withContext(Dispatchers.Main){
                    relatedVideoMapping(response.body()!!)
                }
            }
        }
        else{
            Log.d("안됐음","ㄴㅇㄹㄴㅁㄹ")
        }
    }
    private fun relatedVideoMapping(relatedVideoData: RelatedVideoData){
        relatedVideoList.clear()
        val youtubeDigitConverter = YoutubeDigitConverter(activity)
        for (index in relatedVideoData.RelatedVideoDataitems.indices){
            val thumbnail = relatedVideoData.RelatedVideoDataitems[index].snippet?.thumbnails?.high?.url!!
            val rawDate = relatedVideoData.RelatedVideoDataitems[index].snippet?.publishedAt!!
            val date = youtubeDigitConverter.intervalBetweenDateText(rawDate)
            val title = stringToHtmlSign(relatedVideoData.RelatedVideoDataitems[index].snippet?.title!!)
            val videoId = relatedVideoData.RelatedVideoDataitems[index].id?.videoId!!
            val channelTitle = relatedVideoData.RelatedVideoDataitems[index].snippet?.channelTitle!!
            val channelId = relatedVideoData.RelatedVideoDataitems[index].snippet?.channelId!!
            relatedVideoList.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date, false))
        }
        binding.relatedVideoProgressBar.visibility = View.GONE
        relatedVideoRecyclerViewAdapter.notifyDataSetChanged()
    }

    /**
     * 현재 관련 영상 대신 댓글로 대체를 했음
     */
    private fun initRecyclerView(){
        binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
        relatedVideoRecyclerViewAdapter = RelatedVideoRecyclerViewAdapter()
        relatedVideoRecyclerViewAdapter.setItemClickListener(object: RelatedVideoRecyclerViewAdapter.OnItemClickListener{
            override fun channelClick(v: View, position: Int) {
                setMotionLayoutListenerForChannelClick()
                binding.playerMotionLayout.transitionToState(R.id.start)
            }
            override fun videoClick(v: View, position: Int) {
                replaceVideo(relatedVideoList[position])
            }
            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showNoticeDialog(relatedVideoList[position])
                        }
                    }
                    true
                })
                popUp.show()
            }

            override fun minusButtonClick(v: View) {
                activity.pitchSeekBar.progress -= 1
                activity.videoService!!.setPitch(activity.pitchSeekBar.progress)
                Toast.makeText(activity,String.format(activity.getString(R.string.pitch_minus_text),activity.pitchSeekBar.progress),Toast.LENGTH_SHORT).show()
            }

            override fun initButtonClick(v: View) {
                activity.pitchSeekBar.progress = 0
                activity.videoService!!.setPitch(0)
                Toast.makeText(activity,String.format(activity.getString(R.string.pitch_initialize_text),activity.pitchSeekBar.progress),Toast.LENGTH_SHORT).show()

            }

            override fun plusButtonClick(v: View) {
                activity.pitchSeekBar.progress += 1
                activity.videoService!!.setPitch(activity.pitchSeekBar.progress)
                Toast.makeText(activity,String.format(activity.getString(R.string.pitch_plust_text),activity.pitchSeekBar.progress),Toast.LENGTH_SHORT).show()

            }
        })
        relatedVideoRecyclerViewAdapter.submitList(commentList) // 원래는 relatedvideolist였음
        binding.fragmentRecyclerView.adapter = relatedVideoRecyclerViewAdapter
    }

    private fun initView(){
        val currentPlayingVideoData = videoData
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

        Glide.with(binding.playerThumbnailView)
            .load(currentPlayingVideoData.thumbnail)
            .placeholder(R.color.before_getting_data_color)
            .into(binding.playerThumbnailView)
    }

    fun showNoticeDialog(videoData: VideoData) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

    fun channelDataMapper(): ChannelData {
        val channelThumbnail = currentChannelSearchData.items[0].snippet?.thumbnails?.default?.url!!
        val videoCount = currentChannelSearchData.items[0].statistics?.videoCount!!
        val subscriberCount = currentChannelSearchData.items[0].statistics?.subscriberCount!!
        val viewCount = currentChannelSearchData.items[0].statistics?.viewCount!!
        val channelBanner = currentChannelSearchData.items[0].brandingSettings?.image?.bannerExternalUrl
        val channelTitle = currentChannelSearchData.items[0].snippet?.title!!
        val channelDescription = currentChannelSearchData.items[0].snippet?.description!!
        val channelPlaylistId = currentChannelSearchData.items[0].contentDetails?.relatedPlaylists?.uploads!!
        return ChannelData(channelTitle, channelDescription, channelBanner, channelThumbnail, videoCount, viewCount, subscriberCount, channelPlaylistId)
    }

    fun replacePlaylistVideo(position: Int){ // 플레이리스트에서 아이템을 클릭할 경우
        nowPlaylistModel.updateCurrentPosition(position)
        nowPlaylistModel.refreshPlaylist()
        myPlaylistItemRecyclerViewAdapter.submitList(nowPlaylistModel.getPlayMusicList().toMutableList())
        updateVideoDetailAndGetRelatedVideoData(nowPlaylistModel.currentMusicModel())
        activity.videoService!!.playVideo(nowPlaylistModel.currentMusicModel())
    }

    fun replaceVideo(videoData: VideoData){ // 관련 영상에서 아이템을 클릭할 경우
        binding.fragmentRecyclerView.scrollToPosition(0)
        updateVideoDetailAndGetRelatedVideoData(videoData)
        activity.videoService!!.playVideo(videoData)
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
    private fun updateVideoDetailAndGetRelatedVideoData(videoData: VideoData){
        relatedVideoRecyclerViewAdapter.setHeaderViewTitle(videoData.title)
        relatedVideoRecyclerViewAdapter.setHeaderViewColorBeforeGettingData(activity)
        binding.bottomTitleTextView.text = videoData.title
        Glide.with(binding.playerThumbnailView)
            .load(videoData.thumbnail)
            .placeholder(R.color.before_getting_data_color)
            .into(binding.playerThumbnailView)
        binding.relatedVideoProgressBar.visibility = View.VISIBLE
        getDetailData(videoData.videoId)
    }

    fun playPrevPlaylistVideo(){
        activity.videoService!!.playVideo(nowPlaylistModel.prevMusic()!!)
        nowPlaylistModel.refreshPlaylist()
        myPlaylistItemRecyclerViewAdapter.submitList(nowPlaylistModel.getPlayMusicList().toMutableList())
        updateVideoDetailAndGetRelatedVideoData(nowPlaylistModel.currentMusicModel())
    }

    fun playNextPlaylistVideo(){
        activity.videoService!!.playVideo(nowPlaylistModel.nextMusic()!!)
        nowPlaylistModel.refreshPlaylist()
        myPlaylistItemRecyclerViewAdapter.submitList(nowPlaylistModel.getPlayMusicList().toMutableList())
        updateVideoDetailAndGetRelatedVideoData(nowPlaylistModel.currentMusicModel())
    }

    /**
     * 서비스의 exoPlayer와 연결해주는 함수
     */
    private fun initListener(){
        playerView = binding.playerView
        player = activity.exoPlayer
        playerView.player = player
        activity.videoService!!.playVideo(videoData)
    }

    private fun setMotionLayoutListenerForInitialize() {
        binding.playerMotionLayout.setTransitionListener(null)
        binding.playerMotionLayout.setTransitionListener(TransitionListenerForInitialize())
    }
    private fun setMotionLayoutListenerForChannelClick(){
        binding.playerMotionLayout.setTransitionListener(null)
        binding.playerMotionLayout.setTransitionListener(TransitionListenerForChannelClick())
    }

    inner class TransitionListenerForChannelClick: MotionLayout.TransitionListener{
        override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
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
        override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
            for (fragment: Fragment in activity.supportFragmentManager.fragments){
                if (fragment is HomeFragment && fragment.isVisible){
                    fragment.childFragmentManager.beginTransaction()
                        .add(fragment.binding.searchResultFrameLayout.id,
                            ChannelFragment(channelDataMapper())
                        )
                        .addToBackStack(null)
                        .commit()
                }
                if (fragment is MyPlaylistFragment && fragment.isVisible){
                    fragment.childFragmentManager.beginTransaction()
                        .add(fragment.binding.resultFrameLayout.id,
                            ChannelFragment(channelDataMapper())
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }
            if (binding.playerMotionLayout.currentState == R.id.start){
                if (playlistModel != null)
                    callbackPlaylistVersion.remove()
                else
                    callback.remove()

                settingBottomPlayButton()
                binding.playerView.useController = false
            }
            else{
                binding.playerView.useController = true
                if (playlistModel != null)
                    activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callbackPlaylistVersion)
                else
                    activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
            }
            setMotionLayoutListenerForInitialize()
        }

        override fun onTransitionTrigger(
            motionLayout: MotionLayout?,
            triggerId: Int,
            positive: Boolean,
            progress: Float
        ) {
        }
    }
    inner class TransitionListenerForInitialize:
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
            if (binding.playerMotionLayout.currentState == R.id.start){
                if (playlistModel != null)
                    callbackPlaylistVersion.remove()
                else
                    callback.remove()

                settingBottomPlayButton()
                binding.playerView.useController = false
            }
            else{
                binding.playerView.useController = true
                if (playlistModel != null)
                    activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callbackPlaylistVersion)
                else
                    activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
            }
        }
        override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("프레그먼트의","onDestroy")
        activity.videoService!!.stopForegroundService()
        fbinding = null
        callback.remove()
    }
    override fun onPause() {
        super.onPause()
        Log.d("프레그먼트플레이어","온퍼즈")
    }

    override fun onResume() {
        Log.d("프레그먼트플레이어","온리줌")
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        Log.d("프레그먼트플레이어","온스탑")
    }

    override fun onStart() {
        super.onStart()
        Log.d("프레그먼트플레이어","온스타트")
    }

    private fun initCallback(){
        callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Log.d("동영상 플레이어의","백프레스")
                binding.playerMotionLayout.transitionToState(R.id.start)
            }
        }
        callbackPlaylistVersion = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Log.d("동영상 플레이어의","백프레스")
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                else
                    binding.playerMotionLayout.transitionToState(R.id.start)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        initCallback()
        if (playlistModel != null)
            activity.onBackPressedDispatcher.addCallback(this,callbackPlaylistVersion)
        else
            activity.onBackPressedDispatcher.addCallback(this,callback)
    }

    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }

}