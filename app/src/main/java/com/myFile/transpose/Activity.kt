package com.myFile.transpose

import android.app.ActivityManager
import android.content.*
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.PlaybackParameters
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.myFile.transpose.constants.Actions
import com.myFile.transpose.constants.TimeTarget.REVIEW_TARGET_DURATION
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.dialog.DialogForNotification
import com.myFile.transpose.fragment.HomeFragment
import com.myFile.transpose.fragment.MyPlaylistsFragment
import com.myFile.transpose.fragment.VideoPlayerFragment
import com.myFile.transpose.viewModel.SharedViewModel
import com.myFile.transpose.viewModel.SharedViewModelModelFactory
import java.util.jar.Manifest


class Activity: AppCompatActivity() {
    var mBinding: MainBinding? = null
    val binding get() = mBinding!!

    lateinit var transposePage: LinearLayout

    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var pitchSeekBar: SeekBar
    lateinit var tempoSeekBar: SeekBar
    lateinit var homeFragment: HomeFragment
    lateinit var myPlaylistFragment: MyPlaylistsFragment

    private lateinit var connection: NetworkConnection

    private lateinit var appUsageTimeChecker: AppUsageTimeChecker
    private lateinit var appUpdateManager: AppUpdateManager

    var toastMessage: Toast? = null

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var receiver: BroadcastReceiver

    lateinit var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

//    private val bindConnection = object: ServiceConnection{
//        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
//            Log.d("서버 커네그","$p1")
//            initViewModel()
//            val b = p1 as VideoService.VideoServiceBinder
//            videoService = b.getService()
//            videoService?.setServiceListenerToActivity(this@Activity)
//            isServiceBound.value = true
//
//        }
//
//        override fun onServiceDisconnected(p0: ComponentName?) {
//            videoService = null
//            isServiceBound.value = false
//        }
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()

//        showNoticeDialog()
        volumeControlStream = AudioManager.STREAM_MUSIC
        initController()
        checkUpdateInfo()
        if (Build.VERSION.SDK_INT >= 33) {
            Log.d("퍼미션 ","요청")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
        initBroadcastReceiver()
        initBottomNavigationView()
        initView()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        checkNetworkConnection()
//        bindService(Intent(this, VideoService::class.java), bindConnection, BIND_AUTO_CREATE)
        appUsageTimeSave()
        initFragment()
    }

    private fun initViewModel(){
        val viewModelFactory = SharedViewModelModelFactory()
        sharedViewModel = ViewModelProvider(this, viewModelFactory)[SharedViewModel::class.java]
    }


    private fun initController(){
        val sessionToken = SessionToken(this, ComponentName(this, MediaService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
             startService(Intent(this,MediaService::class.java))
            // MediaController is available here with controllerFuture.get()
        }, MoreExecutors.directExecutor())
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

    /**
     * 현재 VideoPlayerFragment 가 실행중일 때, 그 후 재생할 mode 가 같으면
     * 그냥 state를 end로 transition
     * mode가 다를 경우 서비스 종료 하고 mode 바꾼 후 프레그먼트 replace
     * 실행중이 아닐 경우 mode 바꾼 후 그냥 프레그먼트 replace
     */
    fun executeVideoPlayerFragment(mode: SharedViewModel.PlaybackMode){

        val targetFragment = supportFragmentManager.fragments.firstOrNull { it is VideoPlayerFragment } as? VideoPlayerFragment
        if (targetFragment != null){
            if (sharedViewModel.playbackMode == mode) {
                targetFragment.binding.playerMotionLayout.transitionToEnd()
            } else {
                sharedViewModel.playbackMode = mode
                stopService(Intent(this, MediaService::class.java))
                supportFragmentManager.beginTransaction()
                    .replace(binding.playerFragment.id, VideoPlayerFragment())
                    .commit()
            }
        }
        else{
            sharedViewModel.playbackMode = mode
            supportFragmentManager.beginTransaction()
                .replace(binding.playerFragment.id, VideoPlayerFragment())
                .commit()
        }
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
        registerReceiver(receiver, IntentFilter("YOUR_CUSTOM_ACTION"))
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
        myPlaylistFragment = MyPlaylistsFragment()
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
                    if(fragment.isVisible && fragment is VideoPlayerFragment) {
                        setPitch()
                    }
                }
            }
        })
        binding.pitchInitButton.setOnClickListener {
            pitchSeekBar.progress = 10
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is VideoPlayerFragment) {
                    setPitch()
                }
            }
        }
        binding.pitchSeekBarMinusButton.setOnClickListener {
            pitchSeekBar.progress -= 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is VideoPlayerFragment) {
                    setPitch()
                }
            }
        }
        binding.pitchSeekBarPlusButton.setOnClickListener {
            pitchSeekBar.progress += 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is VideoPlayerFragment) {
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
                    if(fragment.isVisible && fragment is VideoPlayerFragment) {
                        setTempo()
                    }
                }

            }
        })
        binding.tempoInitButton.setOnClickListener {
            tempoSeekBar.progress = 10
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is VideoPlayerFragment) {
                    setTempo()
                }
            }
        }
        binding.tempoSeekBarMinusButton.setOnClickListener {
            tempoSeekBar.progress -= 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is VideoPlayerFragment) {
                    setTempo()
                }
            }
        }
        binding.tempoSeekBarPlusButton.setOnClickListener {
            tempoSeekBar.progress += 1
            for(fragment in supportFragmentManager.fragments) {
                if(fragment.isVisible && fragment is VideoPlayerFragment) {
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
        val targetFragment = supportFragmentManager.fragments.firstOrNull {
            it is VideoPlayerFragment && it.binding.playerMotionLayout.currentState == R.id.end
        } as? VideoPlayerFragment

        if (targetFragment != null) {
            return super.onBackPressed()
        } else {
            if (transposePage.visibility == View.VISIBLE) {
                transposePageInvisibleEvent()
            } else {
                return super.onBackPressed()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        MediaController.releaseFuture(this.controllerFuture)
        val intent = Intent(this, MediaService::class.java)
        stopService(intent)
    }

    fun pitchMinusButtonClick(){
        binding.pitchSeekBar.progress -= 1
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        showToastMessage(String.format(getString(R.string.pitch_minus_text),binding.pitchSeekBar.progress - 10))
    }

    fun pitchInitButtonClick() {
        binding.pitchSeekBar.progress = 10
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        showToastMessage(String.format(getString(R.string.pitch_initialize_text),binding.pitchSeekBar.progress - 10))
    }

    fun pitchPlusButtonClick() {
        binding.pitchSeekBar.progress += 1
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        showToastMessage(String.format(getString(R.string.pitch_plus_text),binding.pitchSeekBar.progress - 10))
    }

    fun tempoMinusButtonClick() {
        binding.tempoSeekBar.progress -= 1
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        showToastMessage(String.format(getString(R.string.tempo_minus_text),binding.tempoSeekBar.progress - 10))

    }

    fun tempoInitButtonClick() {
        binding.tempoSeekBar.progress = 10
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        showToastMessage(String.format(getString(R.string.tempo_init_text),binding.tempoSeekBar.progress - 10))
    }

    fun tempoPlusButtonClick() {
        binding.tempoSeekBar.progress += 1
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        showToastMessage(String.format(getString(R.string.tempo_plus_text),binding.tempoSeekBar.progress - 10))
    }


    fun setPitch() {
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
    }

    fun setTempo() {
        val tempoValue = (binding.tempoSeekBar.progress - 10)*0.05.toFloat()
        val pitchValue = (binding.pitchSeekBar.progress - 10)*0.05.toFloat()
        val param = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
        controller?.playbackParameters = PlaybackParameters(1f + tempoValue, 1f + pitchValue)
    }

    fun showStreamFailMessage() {
        showToastMessage("failed to get stream url")
    }
    private fun initBroadcastReceiver(){
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("이게 안돼나? 브로드","$intent")
                when (intent?.getStringExtra("status")) {
                    "failure" -> {
                        showStreamFailMessage()
                    }
                    "minus" -> {
                        pitchMinusButtonClick()
                    }
                    "plus" -> {
                        pitchPlusButtonClick()
                    }
                }
                if (intent?.action == "YOUR_CUSTOM_ACTION"){
                    Log.d("젭ㄹ되라","${intent.getStringExtra("url")}")
                }
            }
        }
    }




}