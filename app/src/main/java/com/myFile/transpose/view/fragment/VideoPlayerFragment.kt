package com.myFile.transpose.view.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.SessionCommand
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.transpose.databinding.FragmentPlayerBinding
import com.myFile.transpose.databinding.MainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.myFile.transpose.*
import com.myFile.transpose.R
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.MyPlaylistItemRecyclerViewAdapter
import com.myFile.transpose.view.adapter.PlayerFragmentMainRecyclerViewAdapter
import com.myFile.transpose.view.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.model.model.ChannelDataModel
import com.myFile.transpose.model.model.CommentDataModel
import com.myFile.transpose.model.model.VideoDataModel
import com.myFile.transpose.model.model.VideoDetailDataModel
import com.myFile.transpose.viewModel.SharedViewModel
import com.myFile.transpose.viewModel.VideoPlayerViewModel
import com.myFile.transpose.viewModel.VideoPlayerViewModelFactory
import java.lang.Math.abs



class VideoPlayerFragment: Fragment() {

    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    private lateinit var mainRecyclerViewAdapter: PlayerFragmentMainRecyclerViewAdapter
    private lateinit var myPlaylistItemRecyclerViewAdapter: MyPlaylistItemRecyclerViewAdapter
    private var fbinding: FragmentPlayerBinding? = null
    val binding get() = fbinding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var callbackPlaylistVersion: OnBackPressedCallback
    private lateinit var callback: OnBackPressedCallback

    private lateinit var playModeSharedPreferences: SharedPreferences

    private lateinit var viewModel: VideoPlayerViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private var myListener: Player.Listener? = null


//    private lateinit var controllerFuture: ListenableFuture<MediaController>
//    private val controller: MediaController?
//        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentPlayerBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initViewModel()
        initListener()
        initView()
        initCallback()
        setMotionLayoutListenerForInitialize()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
//        startForeground(sharedViewModel.currentVideoData.value!!)
        setController()
    }

    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = VideoPlayerViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel = ViewModelProvider(this, viewModelFactory)[VideoPlayerViewModel::class.java]

    }

    private fun initObserver(){
        when(sharedViewModel.playbackMode){

            SharedViewModel.PlaybackMode.SINGLE_VIDEO -> {

                sharedViewModel.singleModeVideoId.observe(viewLifecycleOwner){
                    convertingUiInSingleVideoMode()
                    sendUrlConvertCommand(it)
                    fetchAllData(it)
                }

                viewModel.currentVideoDetailData.observe(viewLifecycleOwner){
                    addRecyclerViewHeaderTitleViewInSingleMode(it)
                    setMiniPlayerView(it)
                }

                viewModel.currentChannelDetailData.observe(viewLifecycleOwner){
                    val videoDetailDataModel = viewModel.currentVideoDetailData.value
                    val channelDataModel = viewModel.currentChannelDetailData.value
                    addRecyclerViewHeaderRestView(videoDetailDataModel, channelDataModel)
                }

                viewModel.currentCommentThreadData.observe(viewLifecycleOwner){
                    addRecyclerViewCommentDataView(it)
                }

                viewModel.currentVideoDataModel.observe(viewLifecycleOwner){

                }

            }

            SharedViewModel.PlaybackMode.PLAYLIST -> {

                sharedViewModel.currentVideoData.observe(viewLifecycleOwner){ currentVideoData ->
                    sendUrlConvertCommand(currentVideoData.videoId)
                    convertingUiInPlaylistMode()
                    fetchAllData(currentVideoData.videoId)
                    addRecyclerViewHeaderTitleViewInPlaylistMode(currentVideoData)
                    setMiniPlayerView(currentVideoData)
                    val nowPlaylistModel = sharedViewModel.nowPlaylistModel.value ?: return@observe
                    myPlaylistItemRecyclerViewAdapter.submitList(nowPlaylistModel.getPlayMusicList().toMutableList())
                }

                sharedViewModel.nowPlaylistModel.observe(viewLifecycleOwner){
                    initPlaylistRecyclerView()
                    initBottomSheet()
                    myPlaylistItemRecyclerViewAdapter.submitList(it.getPlayMusicList().toMutableList())
                }

                viewModel.currentChannelDetailData.observe(viewLifecycleOwner){
                    Log.d("1","tlf")
                    val videoDetailDataModel = viewModel.currentVideoDetailData.value
                    val channelDataModel = viewModel.currentChannelDetailData.value
                    addRecyclerViewHeaderRestView(videoDetailDataModel, channelDataModel)
                }

                viewModel.currentCommentThreadData.observe(viewLifecycleOwner){
                    Log.d("2","dl")
                    addRecyclerViewCommentDataView(it)
                }
            }
        }
    }

    private fun addRecyclerViewHeaderTitleViewInSingleMode(videoDetailDataModel: VideoDetailDataModel?){
        val newList = mutableListOf<PlayerFragmentMainRecyclerViewAdapter.PlayerFragmentMainItem>()
        videoDetailDataModel.let {
            val headerTitleData = PlayerFragmentMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderTitleData(it?.videoTitle ?: "")
            newList.add(headerTitleData)
        }
        mainRecyclerViewAdapter.submitList(newList)
    }

    private fun addRecyclerViewHeaderTitleViewInPlaylistMode(videoData: VideoDataModel?){
        val newList = mutableListOf<PlayerFragmentMainRecyclerViewAdapter.PlayerFragmentMainItem>()
        videoData.let {
            val headerTitleData = PlayerFragmentMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderTitleData(videoData?.title ?: "")
            newList.add(headerTitleData)
        }
        mainRecyclerViewAdapter.submitList(newList)
    }

    private fun addRecyclerViewHeaderRestView(videoDetailDataModel: VideoDetailDataModel?, channelDataModel: ChannelDataModel?){
        val headerRestData = PlayerFragmentMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderRestData(videoDetailDataModel, channelDataModel)
        val currentList = mainRecyclerViewAdapter.currentList.toMutableList()
        currentList.add(headerRestData)
        mainRecyclerViewAdapter.submitList(currentList)
    }

    private fun addRecyclerViewCommentDataView(commentDataModelList: List<CommentDataModel>?){
        commentDataModelList ?: return
        val commentDataList = commentDataModelList.map{
            PlayerFragmentMainRecyclerViewAdapter.PlayerFragmentMainItem.ContentData(it)
        }
        val currentList = mainRecyclerViewAdapter.currentList.toMutableList()
        currentList.addAll(commentDataList)
        mainRecyclerViewAdapter.submitList(currentList)
    }

    private fun initView(){
        initMiniPlayerView()
        initRecyclerView()
        initPlaylistRecyclerView()
        initBottomSheet()
    }

    /**
     * 현재 관련 영상 대신 댓글로 대체를 했음
     */
    private fun initRecyclerView(){
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(activity)
        mainRecyclerViewAdapter = PlayerFragmentMainRecyclerViewAdapter()
        mainRecyclerViewAdapter.setItemClickListener(object: PlayerFragmentMainRecyclerViewAdapter.OnItemClickListener{
            override fun channelClick(v: View, position: Int) {
                val channelDataModel = viewModel.currentChannelDetailData.value ?: return
                setMotionLayoutListenerForChannelClick(channelDataModel)
                binding.playerMotionLayout.transitionToState(R.id.start)
            }
            override fun videoClick(v: View, position: Int) {
            }
            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                        }
                    }
                    true
                }
                popUp.show()
            }

            override fun pitchMinusButtonClick(v: View) {
                activity.pitchMinusButtonClick()
            }

            override fun pitchInitButtonClick(v: View) {
                activity.pitchInitButtonClick()
            }

            override fun pitchPlusButtonClick(v: View) {
                activity.pitchPlusButtonClick()
            }

            override fun tempoMinusButtonClick(v: View) {
                activity.tempoMinusButtonClick()

            }

            override fun tempoInitButtonClick(v: View) {
                activity.tempoInitButtonClick()
            }

            override fun tempoPlusButtonClick(v: View) {
                activity.tempoPlusButtonClick()
            }

            override fun addButtonClick(v: View) {
                val currentVideoDataModel = viewModel.currentVideoDataModel.value ?: return
                showNoticeDialog(currentVideoDataModel)
            }

        })
        binding.mainRecyclerView.adapter = mainRecyclerViewAdapter
    }


    private fun showNoticeDialog(videoData: VideoDataModel) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

    private fun initPlaylistRecyclerView(){
        if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST){
            val nowPlaylistModel = sharedViewModel.nowPlaylistModel.value ?: return
            binding.playlistTitleInLinearLayout.text = String.format(resources.getString(R.string.playlist_text),"${nowPlaylistModel.getPlaylistTitle()}")
            binding.playlistRecyclerView.layoutManager = LinearLayoutManager(activity)
            myPlaylistItemRecyclerViewAdapter = MyPlaylistItemRecyclerViewAdapter()
            myPlaylistItemRecyclerViewAdapter.setItemClickListener(object: MyPlaylistItemRecyclerViewAdapter.OnItemClickListener{
                override fun onClick(v: View, position: Int) {
                    replaceVideoByPosition(position)
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
    }

    private fun initBottomSheet(){
        if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST){
            val nowPlaylistModel = sharedViewModel.nowPlaylistModel.value ?: return
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
    }

    private fun initMiniPlayerView() {
        binding.bottomPlayerCloseButton.setOnClickListener {
            sendStopConvertingCommand()
            activity.supportFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.bottomPlayerPauseButton.setOnClickListener {
            val controller = activity.controller ?: return@setOnClickListener
            if (controller.currentMediaItem != null){
                if (controller.isPlaying)
                    controller.pause()
                else
                    controller.play()
            }
        }
    }

    private fun setMiniPlayerView(currentVideoDetailDataModel: VideoDetailDataModel?){
        binding.bottomTitleTextView.text = currentVideoDetailDataModel?.videoTitle
    }


    private fun setMiniPlayerView(currentVideoDataModel: VideoDataModel?){

        binding.bottomTitleTextView.text = currentVideoDataModel?.title

        Glide.with(binding.playerThumbnailView)
            .load(currentVideoDataModel?.thumbnail)
            .placeholder(R.color.before_getting_data_color)
            .into(binding.playerThumbnailView)
    }

    private fun fetchAllData(videoId: String){
        viewModel.fetchAllData(videoId)
    }

    private fun convertingUiInSingleVideoMode(){
        binding.playerView.visibility = View.VISIBLE
        binding.bufferingProgressBar.visibility = View.VISIBLE
    }

    private fun playingUiInSingleVideoMode(){
        binding.playerView.visibility = View.VISIBLE
        binding.bufferingProgressBar.visibility = View.GONE
    }

    private fun convertingUiInPlaylistMode(){
        binding.playerView.visibility = View.INVISIBLE
        binding.bufferingProgressBar.visibility = View.VISIBLE
        binding.playerThumbnailView.visibility = View.VISIBLE
    }

    private fun playingUiInPlaylistMode(){
        binding.playerView.visibility = View.VISIBLE
        binding.bufferingProgressBar.visibility = View.GONE
        binding.playerThumbnailView.visibility = View.GONE
    }

    fun replaceVideoByPosition(position: Int){
        sharedViewModel.replaceVideoByPosition(position)
        val nowPlaylistModel = sharedViewModel.nowPlaylistModel.value ?: return

        sendUrlConvertCommand(nowPlaylistModel.currentMusicModel().videoId)
    }

    fun playPrevVideo(){
        sharedViewModel.playPrevVideo()
        val nowPlaylistModel = sharedViewModel.nowPlaylistModel.value ?: return

        sendUrlConvertCommand(nowPlaylistModel.currentMusicModel().videoId)
    }

    fun playNextVideo(){
        sharedViewModel.playNextVideo()
        val nowPlaylistModel = sharedViewModel.nowPlaylistModel.value ?: return

        sendUrlConvertCommand(nowPlaylistModel.currentMusicModel().videoId)
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("프레그먼트의","onDestroy")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        myListener?.let { activity.controller?.removeListener(it) }
        activity.controller?.stop()
        fbinding = null
        callback.remove()
    }

    private fun sendStopConvertingCommand(){
        val action = "stopConverting"
        val sessionCommand = SessionCommand(action, Bundle())
        activity.controller?.sendCustomCommand(sessionCommand, Bundle())
    }

    override fun onPause() {
        super.onPause()
        Log.d("프레그먼트플레이어","온퍼즈")
    }

    override fun onResume() {
        Log.d("프레그먼트플레이어","온리줌")
        binding.mainRecyclerView.scrollToPosition(0)
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        Log.d("프레그먼트플레이어","온스탑")
//        releaseController()
    }

    override fun onStart() {
        super.onStart()
        Log.d("프레그먼트플레이어","온스타트")
//        setController()
    }


    private fun sendUrlConvertCommand(currentVideoDataId: String) {
        val controller = activity.controller ?: return
        controller.pause()

        val action = "convertUrl"
        val bundle = Bundle().apply { putString("videoId", currentVideoDataId) }
        val sessionCommand = SessionCommand(action, bundle)
        controller.sendCustomCommand(sessionCommand, bundle)
    }

    private fun initListener(){
        val controller = activity.controller ?: return
        myListener = object: Player.Listener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying){
                    binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_pause_24)
                }
                else{
                    if (controller.currentPosition >= controller.contentDuration)
                        return
                    binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState){
                    Player.STATE_ENDED -> {
                        binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
//                        if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST){
//                            val playModePreferences = activity.getSharedPreferences("play_mode_preferences",Context.MODE_PRIVATE)
//                            if (playModePreferences.getInt("play_mode",0) == 0)
//                                playNextVideo()
//                            else{
//                                controller.seekTo(0)
//                            }
//                        }
                    }
                    Player.STATE_READY -> {
                        Log.d("레디가 됐는데","왜 안돼지?")
                        when(sharedViewModel.playbackMode){
                            SharedViewModel.PlaybackMode.SINGLE_VIDEO -> playingUiInSingleVideoMode()
                            SharedViewModel.PlaybackMode.PLAYLIST -> playingUiInPlaylistMode()
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                activity.showStreamFailMessage()
                super.onPlayerError(error)
            }
        }
    }

    private fun setController(){
        val controller = activity.controller ?: return
        binding.playerView.player = controller
        myListener?.let { controller.addListener(it) }

    }

    private fun releaseController() {
        binding.playerView.player = null
//        MediaController.releaseFuture(activity.controllerFuture)
    }

    private fun setMotionLayoutListenerForInitialize() {
        binding.playerMotionLayout.setTransitionListener(null)
        binding.playerMotionLayout.setTransitionListener(TransitionListenerForInitialize())
    }
    private fun setMotionLayoutListenerForChannelClick(channelDataModel: ChannelDataModel){
        binding.playerMotionLayout.setTransitionListener(null)
        binding.playerMotionLayout.setTransitionListener(TransitionListenerForChannelClick(channelDataModel))
    }

    inner class TransitionListenerForChannelClick(private val channelDataModel: ChannelDataModel?): MotionLayout.TransitionListener{
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
            activity.transposePageInvisibleEvent()
            sharedViewModel.setChannelData(channelDataModel)
            for (fragment: Fragment in activity.supportFragmentManager.fragments){
                if (fragment is HomeFragment && fragment.isVisible){

                    fragment.childFragmentManager.beginTransaction()
                        .add(fragment.binding.searchResultFrameLayout.id,
                            ChannelFragment()
                        )
                        .addToBackStack(null)
                        .commit()
                }
                if (fragment is MyPlaylistsFragment && fragment.isVisible){
                    fragment.childFragmentManager.beginTransaction()
                        .add(fragment.binding.resultFrameLayout.id,
                            ChannelFragment()
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }
            if (binding.playerMotionLayout.currentState == R.id.start){
                if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST)
                    callbackPlaylistVersion.remove()
                else
                    callback.remove()

                binding.playerView.useController = false
            }
            else{
                binding.playerView.useController = true
                if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST)
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
                if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST){
                    callbackPlaylistVersion.remove()
                }

                else{
                    callback.remove()
                }
                binding.playerView.useController = false
            }
            else{
                binding.playerView.useController = true
                if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST)
                    activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callbackPlaylistVersion)
                else
                    activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
            }
        }
        override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
        }
    }

    private fun initCallback(){
        callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                binding.playerMotionLayout.transitionToState(R.id.start)
            }
        }
        callbackPlaylistVersion = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                else
                    binding.playerMotionLayout.transitionToState(R.id.start)
            }
        }
        addCallback()
    }

    private fun addCallback(){
        if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST)
            activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callbackPlaylistVersion)
        else
            activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

    }
}