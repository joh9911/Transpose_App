package com.myFile.transpose

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.startUpdateFlowForResult
import com.google.android.play.core.review.ReviewManagerFactory
import com.myFile.transpose.constants.TimeTarget
import com.myFile.transpose.constants.TimeTarget.REVIEW_TARGET_DURATION
import com.myFile.transpose.database.AppDatabase
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.dialog.DialogForNotification
import com.myFile.transpose.fragment.HomeFragment
import com.myFile.transpose.fragment.MyPlaylistFragment
import com.myFile.transpose.fragment.PlayerFragment
import kotlinx.coroutines.*


class Activity: AppCompatActivity(), ServiceListenerToActivity {
    var mBinding: MainBinding? = null
    val binding get() = mBinding!!

    lateinit var exoPlayer: ExoPlayer
    lateinit var transposePage: LinearLayout

    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var pitchSeekBar: SeekBar
    lateinit var tempoSeekBar: SeekBar
    lateinit var homeFragment: HomeFragment
    lateinit var myPlaylistFragment: MyPlaylistFragment
    var videoService: VideoService? = null

    private lateinit var connection: NetworkConnection

    private lateinit var appUsageTimeChecker: AppUsageTimeChecker
    private lateinit var appUpdateManager: AppUpdateManager

    var toastMessage: Toast? = null

    val isServiceBound: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    private val bindConnection = object: ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d("서버 커네그","$p1")
            val b = p1 as VideoService.VideoServiceBinder
            videoService = b.getService()
            videoService?.setServiceListenerToActivity(this@Activity)
            exoPlayer = b.getExoPlayerInstance()
            isServiceBound.value = true

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            videoService = null
            isServiceBound.value = false
        }

    }
    private lateinit var coroutineExceptionHandler: CoroutineExceptionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        showNoticeDialog()
        checkUpdateInfo()
        initView()
        initExceptionHandler()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        checkNetworkConnection()
        bindService(Intent(this, VideoService::class.java), bindConnection, BIND_AUTO_CREATE)
        appUsageTimeSave()
        clearCashedDataByTime()
        initFragment()
    }

    private fun clearCashedDataByTime(){
        val youtubeCashedDao = AppDatabase.getDatabase(this).youtubeCashedDataDao()
        CoroutineScope(Dispatchers.IO).launch {
            youtubeCashedDao.deleteOldCashedData(System.currentTimeMillis() - TimeTarget.DATA_DELETE_TARGET_DURATION)
        }
    }

    fun showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogForNotification()

        dialog.show(this.supportFragmentManager, "NoticeDialogFragment")
    }

    fun showToastMessage(message: String){
        toastMessage?.cancel()
        toastMessage = Toast.makeText(this,message,Toast.LENGTH_SHORT)
        toastMessage?.show()
    }

    /**
     * 처음 앱을 접속했을 때 시작 일수 저장
     */
    private fun appUsageTimeSave(){
        val appUsageSharedPreferences = AppUsageSharedPreferences(this)
        appUsageSharedPreferences.saveAppUsageStartTime()
        appUsageTimeChecker = AppUsageTimeChecker(this)
    }

    private fun checkNetworkConnection(){
        connection = NetworkConnection(this)
        connection.observe(this) { isConnected ->
            if (isConnected) {
            } else {
                Log.d("네트워크 연결 안됨", ".")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        /**
         * 앱 사용일이 7일이 되었을 때, 리뷰요청
         */
        val usageDuration = appUsageTimeChecker.getAppUsageDuration()
        Log.d("내 사용 시간", "$usageDuration $REVIEW_TARGET_DURATION")
        if (usageDuration >= REVIEW_TARGET_DURATION) {
            val manager = ReviewManagerFactory.create(this)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                    AppUsageSharedPreferences(this).initializeAppUsageStartTime()
                } else {
                    // There was some problem, log or handle the error code.
                    showToastMessage("오류가 발생")
                }
            }
        }
        /**
         * 업데이트 중 다시 앱을 접속했을 때 처리
         */
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        0
                    )
                }
            }
    }
    private fun checkUpdateInfo(){
        appUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                // instead, pass in AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    0)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (resultCode != RESULT_OK) {
                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
                checkUpdateInfo()
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun initFragment(){
        homeFragment = HomeFragment()
        myPlaylistFragment = MyPlaylistFragment()
        supportFragmentManager.beginTransaction()
            .add(binding.basicFrameLayout.id,homeFragment)
            .commit()
        supportFragmentManager.beginTransaction()
            .add(binding.basicFrameLayout.id,myPlaylistFragment)
            .commit()
        supportFragmentManager.beginTransaction().hide(myPlaylistFragment).commit()
        supportFragmentManager.beginTransaction().show(homeFragment).commit()
        binding.mainMotionLayout.setTransitionListener(object: MotionLayout.TransitionListener{
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
            }
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
            }
            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }

        })
    }

    private fun initExceptionHandler(){
        coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
            Log.d("코루틴 에러","$throwable")
            CoroutineScope(Dispatchers.Main).launch {
                showToastMessage(resources.getString(R.string.network_error_message))
            }
        }
    }

    private fun initView() {
        initTranspose()
        initBottomNavigationView()
    }
    private fun initTranspose(){
        transposePage = binding.transposePage
        pitchSeekBar = binding.pitchSeekBar
        pitchSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.pitchValue.text = (p1 - 10).toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                for(fragment in supportFragmentManager.fragments) {
                    if(fragment.isVisible && fragment is PlayerFragment) {
                        setPitch()
                    }
                }
            }
        })
        binding.pitchInitButton.setOnClickListener {
            pitchSeekBar.progress = 10
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is PlayerFragment) {
                    setPitch()
                }
            }
        }
        binding.pitchSeekBarMinusButton.setOnClickListener {
            pitchSeekBar.progress -= 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is PlayerFragment) {
                    setPitch()
                }
            }
        }
        binding.pitchSeekBarPlusButton.setOnClickListener {
            pitchSeekBar.progress += 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is PlayerFragment) {
                    setPitch()
                }
            }
        }
        tempoSeekBar = binding.tempoSeekBar
        tempoSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.tempoValue.text = (p1-10).toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                for(fragment in supportFragmentManager.fragments) {
                    if(fragment.isVisible && fragment is PlayerFragment) {
                        setTempo()
                    }
                }

            }
        })
        binding.tempoInitButton.setOnClickListener {
            tempoSeekBar.progress = 10
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is PlayerFragment) {
                    setTempo()
                }
            }
        }
        binding.tempoSeekBarMinusButton.setOnClickListener {
            tempoSeekBar.progress -= 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is PlayerFragment) {
                    setTempo()
                }
            }
        }
        binding.tempoSeekBarPlusButton.setOnClickListener {
            tempoSeekBar.progress += 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is PlayerFragment) {
                    setTempo()
                }
            }
        }
        binding.transposeBackButton.setOnClickListener {
            transposePageInvisibleEvent()
        }

        binding.transposePage.setOnClickListener {
            Log.d("asdf","ASdf")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("activity","onPause")
    }

    private fun initBottomNavigationView(){
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_icon -> {
                    if (homeFragment.isVisible){
                        if (transposePage.visibility == View.VISIBLE)
                            transposePage.visibility = View.INVISIBLE
                        else{
                            homeFragment.childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        }
                    }
                    else{
                        supportFragmentManager.beginTransaction().hide(myPlaylistFragment).commit()
                        supportFragmentManager.beginTransaction().show(homeFragment).commit()
                        transposePage.visibility = View.INVISIBLE

                    }
                }
                R.id.transpose_icon -> {
                    transposePage.visibility = View.VISIBLE
                }
                R.id.my_playlist_icon -> {
                    if (myPlaylistFragment.isVisible){
                        if (transposePage.visibility == View.VISIBLE)
                            transposePage.visibility = View.INVISIBLE
                        else
                            myPlaylistFragment.childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    }
                    else{
                        supportFragmentManager.beginTransaction().show(myPlaylistFragment).commit()
                        supportFragmentManager.beginTransaction().hide(homeFragment).commit()
                        transposePage.visibility = View.INVISIBLE

                    }
                }
            }
            true
        }
    }

    fun transposePageInvisibleEvent(){
            transposePage.visibility = View.INVISIBLE
        if (myPlaylistFragment.isVisible)
            binding.bottomNavigationView.menu.findItem(R.id.my_playlist_icon).isChecked = true
        if (homeFragment.isVisible)
            binding.bottomNavigationView.menu.findItem(R.id.home_icon).isChecked = true

    }

    override fun onBackPressed() {
        var tag = false
        for (fragment: Fragment in supportFragmentManager.fragments) {
            if (fragment is PlayerFragment && fragment.binding.playerMotionLayout.currentState == R.id.end)
                tag = true
        }
        if (tag)
            return super.onBackPressed()
        else {
            if (transposePage.visibility == View.VISIBLE)
                transposePageInvisibleEvent()
            else {
                return super.onBackPressed()
            }

//  if (transposePage.visibility != View.VISIBLE){
//                    Log.d("asdfsa","Asdfsaff")
//                    if (myPlaylistFragment.isVisible) {
//                        if (myPlaylistFragment.childFragmentManager.backStackEntryCount == 0) {
//                            supportFragmentManager.beginTransaction().show(homeFragment)
//                                .commit()
//                            supportFragmentManager.beginTransaction().hide(myPlaylistFragment)
//                                .commit()
//                        } else
//                            return super.onBackPressed()
//                    } else
//                        return super.onBackPressed()
//                }
//                else{
//                    transposePageInvisibleEvent()
//                    Log.d("이것도","하")
//                }


//        if (transposePage.visibility == View.VISIBLE)
//            transposePageInvisibleEvent()
//        else{
//            if (myPlaylistFragment.isVisible){
//                if (myPlaylistFragment.childFragmentManager.backStackEntryCount == 0){
//                    supportFragmentManager.beginTransaction().show(homeFragment).commit()
//                    supportFragmentManager.beginTransaction().hide(myPlaylistFragment).commit()
//                }
//                else
//                    return super.onBackPressed()
//            }
//            else
//                return super.onBackPressed()
//        }


//        if (supportFragmentManager.findFragmentById(R.id.player_fragment) == null){
//            if (transposePage.visibility == View.VISIBLE)
//                transposePageInvisibleEvent()
//            else
//                return super.onBackPressed()
//        }
//        else{
//            val playerFragment = supportFragmentManager.findFragmentById(binding.playerFragment.id) as PlayerFragment
//            if (playerFragment.binding.playerMotionLayout.currentState == R.id.end){
//                playerFragment.binding.playerMotionLayout.transitionToState(R.id.start)
//            }
//            else{
//                if (transposePage.visibility == View.VISIBLE)
//                    transposePageInvisibleEvent()
//                else
//                    return super.onBackPressed()
//            }
//        }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService(bindConnection)
        val intent = Intent(this, VideoService::class.java)
        stopService(intent)
    }

    override fun clickMinus() {
        val pitchValue = (binding.pitchSeekBar.progress - 11)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        binding.pitchSeekBar.progress -= 1
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        exoPlayer.playbackParameters = param
    }

    override fun clickPlus() {
        val pitchValue = (binding.pitchSeekBar.progress - 9)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        binding.pitchSeekBar.progress += 1
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        exoPlayer.playbackParameters = param
    }

    override fun clickInit() {
        binding.pitchSeekBar.progress = 0
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        exoPlayer.playbackParameters = param
    }

    override fun clickNext() {
        val playerFragment = supportFragmentManager.findFragmentById(R.id.player_fragment) as PlayerFragment
        if (playerFragment.playlistModel != null)
            playerFragment.playNextPlaylistVideo()
    }

    override fun clickPrev() {
        val playerFragment = supportFragmentManager.findFragmentById(R.id.player_fragment) as PlayerFragment
        if (playerFragment.playlistModel != null)
            playerFragment.playPrevPlaylistVideo()
    }

    override fun setPitch() {
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        exoPlayer.playbackParameters = param
    }

    override fun setTempo() {
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        exoPlayer.playbackParameters = param
    }

    override fun showStreamFailMessage() {
        showToastMessage("failed to get stream url")
    }

}