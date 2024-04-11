package com.myFile.transpose.view.Activity

import android.Manifest
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.Color
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.*
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.mikephil.charting.data.Entry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.myFile.transpose.MyApplication
import com.myFile.transpose.R
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.data.model.*
import com.myFile.transpose.others.constants.Actions
import com.myFile.transpose.others.constants.Actions.TAG
import com.myFile.transpose.others.constants.TimeTarget.REVIEW_TARGET_DURATION
import com.myFile.transpose.service.MediaService
import com.myFile.transpose.utils.AppUsageSharedPreferences
import com.myFile.transpose.utils.AppUsageTimeChecker
import com.myFile.transpose.utils.NetworkConnection
import com.myFile.transpose.view.adapter.PlayerPlaylistRecyclerViewAdapter
import com.myFile.transpose.view.adapter.PlayerMainRecyclerViewAdapter
import com.myFile.transpose.view.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.view.dialog.DialogPasteYoutubeLink
import com.myFile.transpose.view.fragment.ConvertFragment
import com.myFile.transpose.view.fragment.HomeFragment
import com.myFile.transpose.view.fragment.LibraryFragment
import com.myFile.transpose.viewModel.SharedViewModel
import com.myFile.transpose.viewModel.SharedViewModelModelFactory
import kotlin.math.pow


class Activity : AppCompatActivity() {
    var mBinding: MainBinding? = null
    val binding get() = mBinding!!

    lateinit var bottomNavigationView: BottomNavigationView

    lateinit var searchView: SearchView
    lateinit var myFileSearchView: SearchView
    lateinit var searchViewItem: MenuItem
    lateinit var pasteLinkItem: MenuItem
    lateinit var myFileSearchItem: MenuItem
//    lateinit var settingIcon: MenuItem

    private lateinit var connection: NetworkConnection

    private lateinit var appUsageTimeChecker: AppUsageTimeChecker
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUsageSharedPreferences: AppUsageSharedPreferences

    var toastMessage: Toast? = null

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var receiver: BroadcastReceiver

    private lateinit var rotationObserver: ContentObserver

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    lateinit var navController: NavController

    private lateinit var playModeSharedPreferences: SharedPreferences

    private lateinit var mainRecyclerViewAdapter: PlayerMainRecyclerViewAdapter
    private lateinit var playerPlaylistRecyclerViewAdapter: PlayerPlaylistRecyclerViewAdapter

    lateinit var playlistBottomSheetBehavior: BottomSheetBehavior<View>

    lateinit var playerBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        setAppSetting()
        initView()
        initController()
        initBroadcastReceiver()
        appUsageTimeSave()
        initObserver()
        showPatchNotes()
        Log.d("로그 확인","액티비티 온크레이트")

    }

    override fun onStart() {
        super.onStart()
        getIntentEvent()
    }
    private fun getIntentEvent(){
        Log.d("로그 확인","getIntentEvent ${intent.action}")
        // 초기화가 안되서 그런지, 1초 딜레이를 주니깐 실행이 됨,  왜 안되는지 나중에 원인 파악좀
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val type = intent.type
                when {
                    type?.startsWith("audio/") == true -> {
                        // 이미지 파일 처리
                        Log.d("로그 확인","getIntentEvent 이미지 파일 처리")
                        Handler(Looper.getMainLooper()).postDelayed({getMyAudioFileFromIntent()},1000)

                    }
                    type?.startsWith("video/") == true -> {
                        // 비디오 파일 처리
                        Log.d("로그 확인","getIntentEvent 비디오 파일 처리")
                        Handler(Looper.getMainLooper()).postDelayed({getMyVideoFileFromIntent()},1000)

                    }
                    else -> {
                        // 알 수 없는 타입 처리
                    }
                }

            }
            Intent.ACTION_SEND -> {
                getYoutubeSharedLinkFromIntent()
            }

        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        getIntentEvent()
    }

    private fun getMyAudioFileFromIntent(){
        intent.data?.let {
            val currentList = sharedViewModel.getAudioFileDataByIntent(this, it) ?: return
            Log.d("로그 확인","getMyAudioFileFromIntent")
            val myPlaylistTitle = "MyAudioFiles"
            val nowPlaylistModel = NowPlaylistModel(currentList, 0, myPlaylistTitle, null)
            activatePlayerInMyAudioFilesMode(nowPlaylistModel)
        }
    }

    private fun getMyVideoFileFromIntent(){
        intent.data?.let {
            val currentList = sharedViewModel.getVideoFileDataByIntent(this, it) ?: return
            Log.d("로그 확인","getMyVideoFileFromIntent")
            val myPlaylistTitle = "MyVideoFiles"
            val nowPlaylistModel = NowPlaylistModel(currentList, 0, myPlaylistTitle, null)
            activatePlayerInMyVideoFilesMode(nowPlaylistModel)
        }
    }

    private fun getYoutubeSharedLinkFromIntent() {
        Log.d("해당 매소드", "실행됨")
        if (Intent.ACTION_SEND == intent.action && "text/plain" == intent.type) {
            val sharedLink = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedLink != null) {
                sharedViewModel.searchResultMode = SharedViewModel.SearchResultMode.SharedLink
                sharedViewModel.sharedLink = sharedLink

                minimizePlayerBottomSheet()

                controlChildNavFragment(R.id.searchResultFragment)
            }
        }
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            intent.removeExtra(Intent.EXTRA_TEXT)
        }

    }

    private fun hasModifyAudioSettingPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestModifyAudioSettingPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.MODIFY_AUDIO_SETTINGS),
            0
        )
    }

    fun hasWriteExternalStoragePermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    fun requestWriteExternalStoragePermission() {
        Log.d(TAG, "WRITE 권한 요청")
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
    }

    private fun showDialogForWriteExternalStoragePermission() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.storage_permission_dialog_title))
            .setMessage(resources.getString(R.string.write_storage_permission_dialog_message))
            .setPositiveButton(resources.getString(R.string.storage_permission_dialog_go_settings_text)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(resources.getString(R.string.storage_permission_dialog_cancel_text), null)
            .show()
    }


    fun hasReadExternalStoragePermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasAudioPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            val hasVideoPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED

            hasAudioPermission && hasVideoPermission
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    fun requestReadExternalStoragePermission() {
        Log.d(TAG, "권한 요청")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO
                ),
                1
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
    }


    private fun showDialogForReadExternalStoragePermission() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.storage_permission_dialog_title))
            .setMessage(resources.getString(R.string.storage_permission_dialog_message))
            .setPositiveButton(resources.getString(R.string.storage_permission_dialog_go_settings_text)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(resources.getString(R.string.storage_permission_dialog_cancel_text), null)
            .show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    val audioPermissionGranted =
                        grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED

                    val videoPermissionGranted =
                        grantResults.size > 1 &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (audioPermissionGranted && videoPermissionGranted) {
                        // 둘 다 승인됨
                        Log.d(TAG, "오디오 및 비디오 권한 허용 상태")
                    }else {
                        // 하나라도 거부됨
                        Log.d(TAG, "권한 거부")
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO) ||
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO)) {
                            // '다시 묻지 않기'가 선택됐을 때
                            showDialogForReadExternalStoragePermission()
                        }
                        else {
                            Log.d(TAG, "권한 거부")
                            // 권한 거부됨
                        }
                    }
                }
                else{
                    // Android 13 미만에서의 권한 처리
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "외부 저장소 권한 허용 상태")
                    } else {
                        showDialogForReadExternalStoragePermission()
                        Log.d(TAG, "권한 거부")
                        // 권한 거부 처리
                    }
                }

            }
            2 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "WRITE 권한 허용 상태")
                    // 권한이 허용됨
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        // '다시 묻지 않기'가 선택됐을 때
                        Log.d(TAG, "WRITE 다시 묻지 않기")
                        showDialogForWriteExternalStoragePermission()
                    } else {
                        Log.d(TAG, "WRITE 권한 거부")
                        // 권한 거부됨
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }


    private fun setAppSetting() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        volumeControlStream = AudioManager.STREAM_MUSIC

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
            ), 0
        )

        checkUpdateInfo()
        checkNetworkConnection()

    }

    private fun initViewModel() {
        val viewModelFactory = SharedViewModelModelFactory(application as MyApplication)
        sharedViewModel = ViewModelProvider(this, viewModelFactory)[SharedViewModel::class.java]

    }


    private fun showToastMessage(message: String) {
        Handler(Looper.getMainLooper()).post {
            toastMessage?.cancel()
            toastMessage = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            toastMessage?.show()
        }
    }

    private fun showPatchNotes(){
        if (appUsageSharedPreferences.isNewVersionForUsers()){
            val intent = Intent(this, AppIntroForPatchNote::class.java)
            startActivity(intent)
        }
    }

    // 앱 세팅 부분
    private fun appUsageTimeSave() {
        /**
         * 처음 앱을 접속했을 때 시작 일수 저장
         */
        appUsageSharedPreferences = AppUsageSharedPreferences(this)
        appUsageSharedPreferences.saveAppUsageStartTime()
        appUsageTimeChecker = AppUsageTimeChecker(this)
    }

    private fun checkNetworkConnection() {
        connection = NetworkConnection(this)
        connection.observe(this) { isConnected ->
            if (isConnected) {
                Log.d("네트워크", "연결됨")
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
            Log.d("리뷰요청", "실행")
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, IntentFilter("YOUR_CUSTOM_ACTION"), Context.RECEIVER_NOT_EXPORTED)
            } else{
                registerReceiver(receiver, IntentFilter("YOUR_CUSTOM_ACTION"))
            }



    }

    private fun checkUpdateInfo() {
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
                    0
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            0 -> {
                if (resultCode != RESULT_OK) {
                    Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
//                checkUpdateInfo()
                }
            }
            1 -> {
                Log.d("이게 왜","안될까?")
                controlChildNavFragment(R.id.searchResultFragment)
            }
        }

    }


    // 뷰 초기화
    private fun initView() {
        initToolbar()
        initPlayerViewSetting()
        initPlayerBottomSheet()
        initBottomNavigationView()
        initMainRecyclerView()
        initPlaylistRecyclerView()
        initPlaylistBottomSheet()
        initScreenRotationSettingObserver()
    }

    // toolBar와 SearchView 관련 코드
    fun collapseSearchView() {
        searchViewItem.collapseActionView()
    }

    private fun controlChildNavFragment(destinationId: Int?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment?
        val currentDestinationId =
            when (val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {
                is HomeFragment -> currentFragment.navController.currentDestination?.id
                is ConvertFragment -> currentFragment.navController.currentDestination?.id
                is LibraryFragment -> currentFragment.navController.currentDestination?.id
                else -> null
            }
        if (destinationId == currentDestinationId) return
        try{
            when (destinationId) {
                null -> {
                    when (val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {
                        is HomeFragment -> currentFragment.navController.navigateUp()
                        is ConvertFragment -> currentFragment.navController.navigateUp()
                        is LibraryFragment -> currentFragment.navController.navigateUp()
                    }
                }
                else -> {
                    when (val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {
                        is HomeFragment -> currentFragment.navController.navigate(destinationId)
                        is ConvertFragment -> currentFragment.navController.navigate(destinationId)
                        is LibraryFragment -> currentFragment.navController.navigate(destinationId)
                    }
                }
            }
        } catch (e: Exception){
            Toast.makeText(this,"Error Occur",Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFromFragment() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment?
        when (val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {
            is HomeFragment -> sharedViewModel.fromFragmentIdInHomeNavFragment =
                currentFragment.navController.currentDestination?.id

            is ConvertFragment -> sharedViewModel.fromFragmentIdInConvertNavFragment =
                currentFragment.navController.currentDestination?.id

            is LibraryFragment -> sharedViewModel.fromFragmentIdInLibraryNavFragment =
                currentFragment.navController.currentDestination?.id
        }
    }


    private fun initToolbar() {
        setSupportActionBar(binding.mainToolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayUseLogoEnabled(false)

    }

    private fun searchViewActivateEvent() {
        animateViewsWhenSearchViewActivated()
        pasteLinkItem.isVisible = false
        searchViewItem.isVisible = false
        if (::myFileSearchItem.isInitialized)
            myFileSearchItem.isVisible = false
//        settingIcon.isVisible = false
    }

    private fun searchViewCollapseEvent() {
        Log.d(TAG, "붕괴 이벤트 ${searchViewItem.isActionViewExpanded}")
        animateViewShenSearchViewCollapsed()

//        sharedViewModel.clearCurrentVideoData()
        pasteLinkItem.isVisible = true
        searchViewItem.isVisible = true

        sharedViewModel.fromChildFragmentInNavFragment.value?.let{
            if (it == R.id.myVideoFileItemsFragment || it == R.id.myAudioFileItemsFragment){
                if (::myFileSearchItem.isInitialized)
                    myFileSearchItem.isVisible = true
            }
        }
//        settingIcon.isVisible = true
        searchView.clearFocus()
        myFileSearchView.clearFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tool_bar, menu)
        searchViewItem = menu?.findItem(R.id.search_icon)!!

        searchViewItem = menu.findItem(R.id.search_icon)
        pasteLinkItem = menu.findItem(R.id.paste_icon)
        myFileSearchItem = menu.findItem(R.id.my_file_search_icon)

//        settingIcon = menu.findItem(R.id.setting_icon)

        // 유튜브용 서치뷰 설정
        searchView = searchViewItem.actionView as SearchView
        val searchAutoComplete =
            searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val searchViewCloseButton =
            searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchAutoComplete.setTextColor(resources.getColor(R.color.white))
        searchAutoComplete.setHintTextColor(resources.getColor(R.color.white))
        searchAutoComplete.hint = resources.getString(R.string.searchView_hint)
        searchViewCloseButton.setColorFilter(resources.getColor(R.color.white))

        searchView.setOnQueryTextFocusChangeListener { p0, isClicked -> // 서치뷰 검색창을 클릭할 때 이벤트
            if (isClicked) {
//                Log.d(TAG,"searchView 검색창 클릭")
//                controlChildNavFragment(R.id.suggestionKeywordFragment)
            }
        }

        searchViewItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                sharedViewModel.isSearchViewExpanded = true
                saveFromFragment()
                controlChildNavFragment(R.id.suggestionKeywordFragment)
                searchViewActivateEvent()
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                if (playerBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    if (sharedViewModel.isFullScreenMode) {
                        activatePlayerViewOriginalScreenMode()
                    } else {
                        if (playlistBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                            hidePlaylistBottomSheet()
                        else
                            minimizePlayerBottomSheet()
                    }
                    return false
                } else {
                    sharedViewModel.isSearchViewExpanded = false
                    // expand상태에서 뒤로가기 누를 시 suggestionKeywordFragment 도 종료
                    // 검색 버튼 시에도 종료되어야 하므로 예외 처리
                    if (sharedViewModel.aboutToCollapsedBySearchButton) {
                        sharedViewModel.aboutToCollapsedBySearchButton = false
                    } else {
                        //null 일 때는 navigateUp
                        controlChildNavFragment(null)
                    }
                    Log.d("로그 확인","그냥 서치뷰")

                    searchViewCollapseEvent()
                    return true
                }

            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) {
                    return false  // 키보드의 기본 동작을 유지하려면 false 반환
                }

                searchView.clearFocus()
                sharedViewModel.searchResultMode = SharedViewModel.SearchResultMode.SearchKeyword
                sharedViewModel.searchKeyword = query

//                minimizePlayerBottomSheet()
                controlChildNavFragment(R.id.searchResultFragment)

                sharedViewModel.aboutToCollapsedBySearchButton = true

                collapseSearchView()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank())
                    sharedViewModel.getSuggestionKeyword(newText)
                else
                    sharedViewModel.clearSuggestionKeywords()
                return false

            }
        })

        // 내 파일용 서치뷰 설정
        myFileSearchView = myFileSearchItem.actionView as SearchView

        val myFileSearchAutoComplete =
            myFileSearchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val myFileSearchCloseButton =
            myFileSearchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        myFileSearchAutoComplete.setTextColor(resources.getColor(R.color.white))
        myFileSearchAutoComplete.setHintTextColor(resources.getColor(R.color.white))
        myFileSearchCloseButton.setColorFilter(resources.getColor(R.color.white))

        sharedViewModel.fromChildFragmentInNavFragment.value?.let{
            if (it == R.id.myVideoFileItemsFragment || it == R.id.myAudioFileItemsFragment){
                myFileSearchItem.isVisible = true
            }
        }

        myFileSearchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {


                sharedViewModel.isSearchViewExpanded = true
                saveFromFragment()
                sharedViewModel.fromChildFragmentInNavFragment.value?.let{
                    when(it){
                        R.id.myAudioFileItemsFragment -> {
                            myFileSearchAutoComplete.hint = getString(R.string.my_audio_file_searchView_hint)
                            controlChildNavFragment(R.id.myAudioFileSearchFragment)
                        }
                        R.id.myVideoFileItemsFragment -> {
                            myFileSearchAutoComplete.hint = getString(R.string.my_video_file_searchView_hint)
                            controlChildNavFragment(R.id.myVideoFileSearchFragment)
                        }

                    }
                }
                searchViewActivateEvent()
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                if (playerBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    if (sharedViewModel.isFullScreenMode) {
                        activatePlayerViewOriginalScreenMode()
                    } else {
                        if (playlistBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                            hidePlaylistBottomSheet()
                        else
                            minimizePlayerBottomSheet()
                    }
                    return false
                } else {
                    sharedViewModel.isSearchViewExpanded = false
                    // expand상태에서 뒤로가기 누를 시 suggestionKeywordFragment 도 종료
                    // 검색 버튼 시에도 종료되어야 하므로 예외 처리
                    if (sharedViewModel.aboutToCollapsedBySearchButton) {
                        sharedViewModel.aboutToCollapsedBySearchButton = false
                    } else {
                        //null 일 때는 navigateUp
                        controlChildNavFragment(null)
                    }

                    Log.d("로그 확인","내 파일 서치뷰")
                    searchViewCollapseEvent()
                    return true
                }
            }

        })

        myFileSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) {
                    return false  // 키보드의 기본 동작을 유지하려면 false 반환
                }

                myFileSearchView.clearFocus()
//                minimizePlayerBottomSheet()

//                sharedViewModel.aboutToCollapsedBySearchButton = true
//                collapseSearchView()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()){
                    sharedViewModel.fromChildFragmentInNavFragment.value?.let {
                        when(it){
                            R.id.myAudioFileItemsFragment -> sharedViewModel.searchMyAudioFilesByKeyword(newText)
                            R.id.myVideoFileItemsFragment -> sharedViewModel.searchVideoFilesByKeyword(newText)
                            else -> {}
                        }
                    }
                }

                else{
                    sharedViewModel.fromChildFragmentInNavFragment.value?.let {
                        when(it){
                            R.id.myAudioFileItemsFragment -> sharedViewModel.clearMySearchedAudioFiles()
                            R.id.myVideoFileItemsFragment -> sharedViewModel.clearMySearchedVideoFiles()
                            else -> {}
                        }
                    }
                }
                return false

            }
        })



        pasteLinkItem.setOnMenuItemClickListener {
            showPasteYoutubeLinkDialog()
            true
        }

//        settingIcon.setOnMenuItemClickListener {
//            settingIconClickEvent()
//            true
//        }
        return super.onCreateOptionsMenu(menu)
    }

//    private fun settingIconClickEvent(){
//        val intent = Intent(this, SettingActivity::class.java)
//        startActivity(intent)
//    }

    private fun showPasteYoutubeLinkDialog() {
        val dialog = DialogPasteYoutubeLink()

        dialog.setListener(object : DialogPasteYoutubeLink.NoticeDialogListener {
            override fun onDialogPositiveClick(dialog: DialogFragment, text: Editable?) {
                sharedViewModel.searchResultMode = SharedViewModel.SearchResultMode.SharedLink
                sharedViewModel.sharedLink = text.toString()

                controlChildNavFragment(R.id.searchResultFragment)
            }

            override fun onDialogNegativeClick(dialog: DialogFragment) {
                dialog.dismiss()
            }
        })
        dialog.show(supportFragmentManager, "NoticeDialogFragment")
    }

    // PlayerView의 화면 회전 관련 코드
    private fun isScreenRotationEnabled() = Settings.System.getInt(
        contentResolver,
        Settings.System.ACCELEROMETER_ROTATION, 0
    ) == 1

    private fun updateOrientation() {
        if (isScreenRotationEnabled()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            requestedOrientation = if (sharedViewModel.isFullScreenMode) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        }
    }

    private fun updatePlayerUIByFullScreenMode(fullScreenMode: Boolean) {
        if (fullScreenMode) {
            hideSystemUI()
            setPlayerViewSizeInFullScreen()
            playerBottomSheetBehavior.isDraggable = false
            findViewById<ImageButton>(R.id.exo_minus).visibility = View.VISIBLE
            findViewById<ImageButton>(R.id.exo_plus).visibility = View.VISIBLE
        } else {
            showSystemUI()
            setPlayerViewSizeInOriginalScreen()
            playerBottomSheetBehavior.isDraggable = true
            findViewById<ImageButton>(R.id.exo_minus).visibility = View.GONE
            findViewById<ImageButton>(R.id.exo_plus).visibility = View.GONE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation != Configuration.ORIENTATION_UNDEFINED && playerBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            when (newConfig.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> activatePlayerViewFullScreenMode()
                Configuration.ORIENTATION_PORTRAIT -> activatePlayerViewOriginalScreenMode()
            }
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun initScreenRotationSettingObserver() {
        rotationObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                updateOrientation()
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            false,
            rotationObserver
        )
    }

    private fun activatePlayerViewFullScreenMode() {
        sharedViewModel.isFullScreenMode = true
        updateOrientation()
        if (isScreenRotationEnabled()) {
            Handler(Looper.getMainLooper()).postDelayed({
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }, 4000)
        }
        updatePlayerUIByFullScreenMode(true)
    }


    fun activatePlayerViewOriginalScreenMode() {
        sharedViewModel.isFullScreenMode = false
        updateOrientation()
        if (isScreenRotationEnabled()) {
            Handler(Looper.getMainLooper()).postDelayed({
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }, 2000)
        }
        updatePlayerUIByFullScreenMode(false)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.mainCoordinatorLayout).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(
            window,
            binding.mainCoordinatorLayout
        ).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun setPlayerViewSizeInFullScreen() {
        binding.playerView.layoutParams = binding.playerView.layoutParams.apply {
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            width = ConstraintLayout.LayoutParams.MATCH_PARENT
        }
        binding.playerView.requestLayout()
    }

    private fun setPlayerViewSizeInOriginalScreen() {
        val scale = resources.displayMetrics.density
        val maxHeight = 250 * scale
        binding.playerView.layoutParams = binding.playerView.layoutParams.apply {
            height = maxHeight.toInt()
            width = ConstraintLayout.LayoutParams.MATCH_PARENT
        }
        binding.playerView.requestLayout()
    }

    private fun initPlayerViewSetting() {
        binding.playerView.setFullscreenButtonClickListener { fullScreen ->
            if (fullScreen) {
                sharedViewModel.isManualOrientationChange = true
                activatePlayerViewFullScreenMode()
            } else {
                sharedViewModel.isManualOrientationChange = false
                activatePlayerViewOriginalScreenMode()
            }
        }
        val exoMinusButton = findViewById<ImageButton>(R.id.exo_minus)
        exoMinusButton.setOnClickListener {
            pitchMinusButtonClick()
        }

        val exoPlusButton = findViewById<ImageButton>(R.id.exo_plus)
        exoPlusButton.setOnClickListener {
            pitchPlusButtonClick()
        }
    }

    // tempo, pitch 조절 코드

    private fun sendStopConvertingCommand() {
        val action = "stopConverting"
        val sessionCommand = SessionCommand(action, Bundle())
        controller?.sendCustomCommand(sessionCommand, Bundle())
    }

    private fun initPlaylistRecyclerView() {
        binding.playlistRecyclerView.layoutManager = LinearLayoutManager(this)
        playerPlaylistRecyclerViewAdapter = PlayerPlaylistRecyclerViewAdapter()
        playerPlaylistRecyclerViewAdapter.setItemClickListener(object :
            PlayerPlaylistRecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                // 변환 중에 이미 변환한 아이템을 클릭 시 재생되다가 변환한 영상이 재생됨
                // 따라서 stop 명령
                controller?.seekTo(position, 0)
                sendStopConvertingCommand()
            }

            override fun optionButtonClick(v: View, position: Int) {
                if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.MYAUDIOFILES) return
                val nowPlaylistModel = sharedViewModel.nowPlaylistModel ?: return
                val popUp = PopupMenu(this@Activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showAddPlaylistDialog(nowPlaylistModel.getPlayMusicList()[position])
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
        binding.playlistRecyclerView.adapter = playerPlaylistRecyclerViewAdapter
    }

    fun expandPlaylistBottomSheet() {
        playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hidePlaylistBottomSheet() {
        playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun initPlaylistBottomSheet() {
        playModeSharedPreferences =
            getSharedPreferences("play_mode_preferences", Context.MODE_PRIVATE)
        playlistBottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet)
        playlistBottomSheetBehavior.skipCollapsed = true
        playlistBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        sharedViewModel.playlistBottomSheetState =
                            BottomSheetBehavior.STATE_EXPANDED
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> Log.d("바텀시트", "dragging")
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.mainBackgroundView.alpha = 0f
                        sharedViewModel.playlistBottomSheetState =
                            BottomSheetBehavior.STATE_COLLAPSED
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.mainBackgroundView.alpha = 0f
                        sharedViewModel.playlistBottomSheetState = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset >= 0) {
                    binding.mainBackgroundView.alpha = slideOffset

                }
            }

        })
        val playlistLinearLayout = binding.playlistLinearLayout

        playlistLinearLayout.visibility = View.VISIBLE
        playlistLinearLayout.setOnClickListener {
            playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            val nowPlaylistModel = sharedViewModel.nowPlaylistModel ?: return@setOnClickListener
            binding.playlistRecyclerView.scrollToPosition(nowPlaylistModel.getCurrentPosition())
        }
        val bottomSheetCloseButton = binding.bottomSheetCloseButton
        bottomSheetCloseButton.setOnClickListener {
            playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val playModeIconButton = binding.playModeIcon
        val playMode = playModeSharedPreferences.getInt("play_mode", 0)
        if (playMode == 0)
            playModeIconButton.setImageResource(R.drawable.loop_4)
        else
            playModeIconButton.setImageResource(R.drawable.loop_1)
        playModeIconButton.setOnClickListener {
            if (playModeSharedPreferences.getInt("play_mode", 0) == 0) {
                playModeSharedPreferences.edit().putInt("play_mode", 1).apply()
                playModeIconButton.setImageResource(R.drawable.loop_1)
                controller?.repeatMode = Player.REPEAT_MODE_ONE

            } else {
                playModeSharedPreferences.edit().putInt("play_mode", 0).apply()
                playModeIconButton.setImageResource(R.drawable.loop_4)
                controller?.repeatMode = Player.REPEAT_MODE_ALL
            }
        }
        binding.playlistTitleBottomSheet.post {
            playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

    }

    private fun initBottomNavigationView() {
        bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.homeFragment -> bottomNavigationView.menu.findItem(R.id.homeFragment).isChecked =
                    true

                R.id.convertFragment -> bottomNavigationView.menu.findItem(R.id.convertFragment).isChecked =
                    true

                R.id.libraryFragment -> bottomNavigationView.menu.findItem(R.id.libraryFragment).isChecked =
                    true
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val currentDestinationId = navController.currentDestination?.id

            if (currentDestinationId == item.itemId) {
                when (item.itemId) {
                    R.id.homeFragment -> {
                        Log.d(TAG,"홈 탭을 누름")
                        controlChildNavFragment(R.id.playlistFragment)
                        return@setOnItemSelectedListener true
                    }
                    R.id.convertFragment -> {
                        controlChildNavFragment(R.id.audioEditFragment)
                        return@setOnItemSelectedListener true
                    }
                    R.id.libraryFragment -> {
                        controlChildNavFragment(R.id.myPlaylistsFragment)
                        return@setOnItemSelectedListener true
                    }
                    // 추가로 다른 탭에 대한 동일한 로직을 넣을 수 있습니다.
                }
            }
            item.onNavDestinationSelected(navController)
        }
    }

    private fun initMainRecyclerView() {
        /**
         * 현재 관련 영상 대신 댓글로 대체를 했음
         */
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerViewAdapter = PlayerMainRecyclerViewAdapter()
        mainRecyclerViewAdapter.setItemClickListener(object :
            PlayerMainRecyclerViewAdapter.OnItemClickListener {
            override fun channelClick(v: View, position: Int) {
                sharedViewModel.currentChannelDetailData.value ?: return

//                navController.navigate(R.id.channelFragment)
                playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            }

            override fun videoClick(v: View, position: Int) {
            }

            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(this@Activity, v)
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
                pitchMinusButtonClick()
            }

            override fun pitchInitButtonClick(v: View) {
                pitchInitButtonClick()
            }

            override fun pitchPlusButtonClick(v: View) {
                pitchPlusButtonClick()
            }

            override fun tempoMinusButtonClick(v: View) {
                tempoMinusButtonClick()

            }

            override fun tempoInitButtonClick(v: View) {
                tempoInitButtonClick()
            }

            override fun tempoPlusButtonClick(v: View) {
                tempoPlusButtonClick()
            }

            override fun addButtonClick(v: View) {
                if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.MYAUDIOFILES){
                    return
                }
                    
                val currentVideoDataModel = sharedViewModel.currentVideoDataModel.value ?: return
                showAddPlaylistDialog(currentVideoDataModel)
            }

        })
        binding.mainRecyclerView.adapter = mainRecyclerViewAdapter
    }

    fun expandPlayerBottomSheet() {
        playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun minimizePlayerBottomSheet() {
        playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hidePlayerBottomSheet() {
        playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val inverseRation = 1f - ratio
        val r = Color.red(from) * inverseRation + Color.red(to) * ratio
        val g = Color.green(from) * inverseRation + Color.green(to) * ratio
        val b = Color.blue(from) * inverseRation + Color.blue(to) * ratio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    private fun initPlayerBottomSheet() {
        playerBottomSheetBehavior = BottomSheetBehavior.from(binding.playerBottomSheet)

        playerBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.wholeBackgroundView.alpha = 0f
                        bottomSheet.setOnClickListener {
                            playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        binding.playerView.useController = false
                        sharedViewModel.setBottomSheetState(newState)
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.playerView.useController = true
                        sharedViewModel.setBottomSheetState(newState)
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.wholeBackgroundView.alpha = 0f
                        binding.playerBottomSheet.visibility = View.INVISIBLE
                        controller?.clearMediaItems()
                        clearEachRecyclerView()
                        sharedViewModel.clearCurrentVideoData()
                        sharedViewModel.setBottomSheetState(newState)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset >= 0) {
                    binding.wholeBackgroundView.alpha = 0.7f * slideOffset

                    val startColor =
                        ContextCompat.getColor(applicationContext, R.color.statusBar_background)
                    val endColor = ContextCompat.getColor(applicationContext, R.color.charcoal_gray)
                    val statusBarBlendedColor = blendColors(startColor, endColor, slideOffset)
                    window.statusBarColor = statusBarBlendedColor

                    val navigationBarStartColor =
                        ContextCompat.getColor(applicationContext, R.color.blue_background)
                    val navigationBarBlendedColor =
                        blendColors(navigationBarStartColor, endColor, slideOffset)
                    window.navigationBarColor = navigationBarBlendedColor

                    binding.playlistLinearLayout.setBackgroundColor(statusBarBlendedColor)
                    binding.bottomNavigationView.setBackgroundColor(navigationBarBlendedColor)
                }
            }
        })
        binding.playerBottomSheet.post {
            playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.bottomPlayerCloseButton.setOnClickListener {
            playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.bottomPlayerPauseButton.setOnClickListener {
            val myController = controller ?: return@setOnClickListener
            if (myController.isPlaying)
                myController.pause()
            else
                myController.play()
        }
    }


    fun animateViewsWhenSearchViewActivated() {
        // 바텀 네비게이션 뷰는 밑으로 내려가야함

        val translateYValue = binding.bottomNavigationView.height.toFloat()
        binding.bottomNavigationView.translationY = translateYValue

        // 바텀 시트는 바텀 네비게이션 크기 만큼 밑으로 내려가야함
        binding.playerBottomSheet.translationY = 0f

    }


    fun animateViewShenSearchViewCollapsed() {

        binding.bottomNavigationView.translationY = 0f

        val scale = resources.displayMetrics.density
        val transition = -56 * scale

        binding.playerBottomSheet.translationY = transition

    }


    fun pitchMinusButtonClick() {
        controller ?: return
        val currentPitchValue = sharedViewModel.pitchValue.value ?: 100
        sharedViewModel.setPitchValue(currentPitchValue - 10)

        val afterPitchValue = sharedViewModel.pitchValue.value ?: 100
        val semitonesFromCenter = (afterPitchValue - 100) * 0.1
        val adjustedPitch = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentTempoValue = controller?.playbackParameters?.speed!!
        controller?.playbackParameters = PlaybackParameters(currentTempoValue, adjustedPitch)

        showToastMessage(String.format(getString(R.string.pitch_minus_text), semitonesFromCenter))
    }

    fun pitchInitButtonClick() {
        controller ?: return
        sharedViewModel.setPitchValue(100)
        val currentTempoValue = controller?.playbackParameters?.speed!!
        controller?.playbackParameters = PlaybackParameters(currentTempoValue, 1f)
        showToastMessage(String.format(getString(R.string.pitch_initialize_text), 0.0))
    }

    fun pitchPlusButtonClick() {


        controller ?: return

        
        val currentPitchValue = sharedViewModel.pitchValue.value ?: 100
        sharedViewModel.setPitchValue(currentPitchValue + 10)

        val afterPitchValue = sharedViewModel.pitchValue.value ?: 100
        val semitonesFromCenter = (afterPitchValue - 100) * 0.1
        val adjustedPitch = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentTempoValue = controller?.playbackParameters?.speed!!

        controller?.playbackParameters = PlaybackParameters(currentTempoValue, adjustedPitch)

        showToastMessage(String.format(getString(R.string.pitch_plus_text), semitonesFromCenter))
    }

    fun tempoMinusButtonClick() {
        controller ?: return
        val currentTempoValue = sharedViewModel.tempoValue.value ?: 100
        sharedViewModel.setTempoValue(currentTempoValue - 10)

        val afterTempoValue = sharedViewModel.tempoValue.value ?: 100
        val semitonesFromCenter = (afterTempoValue - 100) * 0.1
        val adjustedTempo = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentPitchValue = controller?.playbackParameters?.pitch!!
        controller?.playbackParameters = PlaybackParameters(adjustedTempo, currentPitchValue)

        showToastMessage(String.format(getString(R.string.tempo_minus_text), semitonesFromCenter))

    }

    fun tempoInitButtonClick() {
        controller ?: return
        sharedViewModel.setTempoValue(100)
        val currentPitchValue = controller?.playbackParameters?.pitch!!
        controller?.playbackParameters = PlaybackParameters(1f, currentPitchValue)
        showToastMessage(String.format(getString(R.string.tempo_init_text), 0.0))
    }

    fun tempoPlusButtonClick() {
        controller ?: return
        val currentTempoValue = sharedViewModel.tempoValue.value ?: 100
        sharedViewModel.setTempoValue(currentTempoValue + 10)

        val afterTempoValue = sharedViewModel.tempoValue.value ?: 100

        val semitonesFromCenter = (afterTempoValue - 100) * 0.1
        val adjustedTempo = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentPitchValue = controller?.playbackParameters?.pitch!!
        controller?.playbackParameters = PlaybackParameters(adjustedTempo, currentPitchValue)

        showToastMessage(String.format(getString(R.string.tempo_plus_text), semitonesFromCenter))
    }


    private fun initBroadcastReceiver() {
        receiver = MyBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(Actions.GET_EQUALIZER_INFO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        }
        else{
            registerReceiver(receiver, intentFilter)
        }
    }

    inner class MyBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Actions.GET_EQUALIZER_INFO){
                Log.d("인텐트가 왔음","ㅎ")
                val valuesToUpdate = arrayListOf<Entry>()
                valuesToUpdate.add(Entry(0f,0f))
                for (index in 0 until 5) {
                    val value = intent.getStringExtra("$index")?.toFloatOrNull() ?: 0f
                    valuesToUpdate.add(Entry(index + 1.0f, value / 1000)) // +1.0f는 Entry에서의 X 값이므로 조절이 필요합니다.
                }
                valuesToUpdate.add(Entry(6f,0f))
                sharedViewModel.equalizerChartValueList = valuesToUpdate
            }
            if (intent?.action == "YOUR_CUSTOM_ACTION") {
                Log.d("인턴트가 왔잖아", "${intent.getStringExtra("status")}")
                when (intent.getStringExtra("status")) {
                    "failure" -> {
                        showToastMessage("failed to get stream url")
                    }

                    "minus" -> {
                        pitchMinusButtonClick()
                    }

                    "plus" -> {
                        pitchPlusButtonClick()
                    }
                }
            }
        }

    }


    private fun showAddPlaylistDialog(videoData: VideoDataModel) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(supportFragmentManager, "AddPlaylistDialog")
    }


    private fun setMiniPlayerView(videoDetailDataModel: VideoDetailDataModel?) {

        binding.bottomTitleTextView.text = videoDetailDataModel?.videoTitle

        Glide.with(binding.playerThumbnailView)
            .load(videoDetailDataModel?.thumbnail)
            .placeholder(R.color.before_getting_data_color)
            .into(binding.playerThumbnailView)
    }


    private fun setConvertingUi() {
        binding.playerView.visibility = View.GONE
        binding.bufferingProgressBar.visibility = View.VISIBLE
        binding.playerThumbnailView.visibility = View.VISIBLE
        binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)

    }

    private fun setPlayingUi() {
        binding.playerView.visibility = View.VISIBLE
        binding.bufferingProgressBar.visibility = View.GONE
        binding.playerThumbnailView.visibility = View.GONE

    }

    private fun activatePlayerBottomSheet() {
        binding.playerBottomSheet.visibility = View.VISIBLE
        playerBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun deActivatePlaylistView() {
        binding.playlistCoordinatorLayout.visibility = View.GONE
        binding.playlistLinearLayout.visibility = View.GONE
    }

    private fun activatePlaylistView(nowPlaylistModel: NowPlaylistModel) {
        binding.playlistCoordinatorLayout.visibility = View.VISIBLE
        binding.playlistLinearLayout.visibility = View.VISIBLE

        binding.playlistTitleBottomSheet.text = nowPlaylistModel.getPlaylistTitle()
        binding.playlistTitleInLinearLayout.text = String.format(
            resources.getString(R.string.playlist_text),
            "${nowPlaylistModel.getPlaylistTitle()}"
        )

    }


    fun activatePlayerInSingleMode(videoId: String) {
        // 얘는 intercept 에서 실행하기 때문에
        runOnUiThread {
            sharedViewModel.playbackMode = SharedViewModel.PlaybackMode.SINGLE_VIDEO
            clearEachRecyclerView()
            addMediaItemsInSingleMode(videoId)
            deActivatePlaylistView()
            setConvertingUi()
            sharedViewModel.fetchAllData(videoId)
            activatePlayerBottomSheet()
        }
    }

    private fun addMediaItemsInSingleMode(videoId: String) {
        val mediaItems = listOf(
            MediaItem.Builder()
                .setUri("asset:///15-seconds-of-silence.mp3")
                .also {
                    val metadata = MediaMetadata.Builder()
                        .setTitle("converting..")
                        .setAlbumTitle("converting..")
                        .setAlbumArtist("converting..")
                        .setArtist("converting..")
                        .build()
                    it.setMediaMetadata(metadata)
                }
                .setMediaId(videoId)
                .build()
        )
        controller?.setMediaItems(mediaItems, 0, 0)
        controller?.play()
    }

    private fun clearEachRecyclerView() {
        mainRecyclerViewAdapter.submitList(arrayListOf())
        playerPlaylistRecyclerViewAdapter.submitList(arrayListOf())
    }

    fun activatePlayerInPlaylistMode(nowPlaylistModel: NowPlaylistModel) {
        sharedViewModel.playbackMode = SharedViewModel.PlaybackMode.PLAYLIST
        val previousVideo = sharedViewModel.currentPlayingVideo.value?.videoDataModel
        Log.d("로그 확인","$nowPlaylistModel")
        if (previousVideo != nowPlaylistModel.currentMusicModel())
            clearEachRecyclerView()
        sharedViewModel.nowPlaylistModel = nowPlaylistModel
        addMediaItemsInPlaylistMode(nowPlaylistModel)
        activatePlaylistView(nowPlaylistModel)
        playerPlaylistRecyclerViewAdapter.submitList(
            nowPlaylistModel.getPlayMusicList().toMutableList()
        )
        activatePlayerBottomSheet()
    }

    fun activatePlayerInMyVideoFilesMode(nowPlaylistModel: NowPlaylistModel){
        sharedViewModel.playbackMode = SharedViewModel.PlaybackMode.MYAUDIOFILES
        val previousVideo = sharedViewModel.currentPlayingVideo.value?.videoDataModel
        if (previousVideo != nowPlaylistModel.currentMusicModel())
            clearEachRecyclerView()
        sharedViewModel.nowPlaylistModel = nowPlaylistModel
        addMediaItemsInMyVideoFilesMode(nowPlaylistModel)
        activatePlaylistView(nowPlaylistModel)
        playerPlaylistRecyclerViewAdapter.submitList(
            nowPlaylistModel.getPlayMusicList().toMutableList()
        )
        activatePlayerBottomSheet()
    }

    fun activatePlayerInMyAudioFilesMode(nowPlaylistModel: NowPlaylistModel) {
        sharedViewModel.playbackMode = SharedViewModel.PlaybackMode.MYAUDIOFILES
        val previousVideo = sharedViewModel.currentPlayingVideo.value?.videoDataModel
        if (previousVideo != nowPlaylistModel.currentMusicModel())
            clearEachRecyclerView()
        sharedViewModel.nowPlaylistModel = nowPlaylistModel
        addMediaItemsInMyAudioFilesMode(nowPlaylistModel)
        activatePlaylistView(nowPlaylistModel)
        playerPlaylistRecyclerViewAdapter.submitList(
            nowPlaylistModel.getPlayMusicList().toMutableList()
        )
        activatePlayerBottomSheet()
    }

    private fun getAlbumArtUri(albumId: Long): Uri {
        // 앨범 아트 URI를 얻는 로직
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }

    private fun addMediaItemsInMyAudioFilesMode(nowPlaylistModel: NowPlaylistModel) {
        val mediaItems = nowPlaylistModel.getPlayMusicList().map { videoData ->
            var albumArtUri: Uri? = null
            albumArtUri = try{
                getAlbumArtUri(videoData.thumbnail.toLong())
            } catch (e: Exception){
                null
            }
            MediaItem.Builder()
                .setUri(videoData.videoId)
                .also {
                    val metadata = MediaMetadata.Builder()
                        .setTitle(videoData.title)
                        .setAlbumTitle(videoData.channelTitle)
                        .setAlbumArtist(videoData.channelTitle)
                        .setArtist(videoData.channelTitle)
                        .setArtworkUri(albumArtUri)
                        .build()
                    it.setMediaMetadata(metadata)
                }
                .build()
        }
        controller?.setMediaItems(mediaItems, nowPlaylistModel.getCurrentPosition(), 0)
        controller?.play()
    }
    private fun getThumbnailUri(videoId: Long): Uri {
        // 비디오 썸네일 URI를 가져오는 로직
        // 예: MediaStore.Video.Thumbnails의 URI를 사용
        return ContentUris.withAppendedId(
            MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
            videoId
        )
    }
    private fun addMediaItemsInMyVideoFilesMode(nowPlaylistModel: NowPlaylistModel) {
        val mediaItems = nowPlaylistModel.getPlayMusicList().map { videoData ->
            val albumArtUri: Uri? = try{
                getThumbnailUri(videoData.thumbnail.toLong())
            } catch (e: Exception){
                Log.d("로그 확인","addMediaItemsInMyVideoFilesMode ${e.message}")
                null
            }
            Log.d("로그 확인","addMediaItemsInMyVideoFilesMode ${albumArtUri}")

            MediaItem.Builder()
                .setUri(videoData.videoId)
                .also {
                    val metadata = MediaMetadata.Builder()
                        .setTitle(videoData.title)
                        .setAlbumTitle(videoData.channelTitle)
                        .setAlbumArtist(videoData.channelTitle)
                        .setArtist(videoData.channelTitle)
                        .setArtworkUri(albumArtUri)

                        .build()
                    it.setMediaMetadata(metadata)
                }
                .build()
        }
        controller?.setMediaItems(mediaItems, nowPlaylistModel.getCurrentPosition(), 0)
        controller?.play()
    }

    private fun addMediaItemsInPlaylistMode(nowPlaylistModel: NowPlaylistModel) {

        val mediaItems = nowPlaylistModel.getPlayMusicList().map { videoData ->
            MediaItem.Builder()
                .setUri("asset:///15-seconds-of-silence.mp3")
                .also {
                    val metadata = MediaMetadata.Builder()
                        .setTitle("converting..")
                        .setAlbumTitle("converting..")
                        .setAlbumArtist("converting..")
                        .setArtist(videoData.title)
                        .setDescription(videoData.title)
                        .build()
                    it.setMediaMetadata(metadata)
                }
                .setMediaId(videoData.videoId)
                .build()
        }
        controller?.setMediaItems(mediaItems, nowPlaylistModel.getCurrentPosition(), 0)
        controller?.play()
    }

    private fun setMiniViewInPlaylistMode(videoData: VideoDataModel) {
        binding.bottomTitleTextView.text = videoData.title

        Glide.with(binding.playerThumbnailView)
            .load(videoData.thumbnail)
            .placeholder(R.color.before_getting_data_color)
            .into(binding.playerThumbnailView)
    }


    private fun initObserver() {

        sharedViewModel.bottomSheetState.observe(this) {
            // 바텀 시트만큼 마진을 줘서 잘 보이게해줌
            if (it != BottomSheetBehavior.STATE_HIDDEN) {
                val scale = resources.displayMetrics.density
                val maxBottomPadding = (56 * scale + 0.5f).toInt() // 56dp to pixels
                binding.mainNavHost.setPadding(
                    binding.mainNavHost.paddingLeft,
                    binding.mainNavHost.paddingTop,
                    binding.mainNavHost.paddingRight,
                    maxBottomPadding
                )
            } else {
                binding.mainNavHost.setPadding(
                    binding.mainNavHost.paddingLeft,
                    binding.mainNavHost.paddingTop,
                    binding.mainNavHost.paddingRight,
                    0,
                )
            }

        }

        sharedViewModel.currentPlayingVideo.observe(this) {
            if (it == null) return@observe
            val nowPlaylistModel = sharedViewModel.nowPlaylistModel ?: return@observe
            // 저장 공간 내 음악 파일을 재생할 때는, 타이틀과 다른 피치 템포 뷰만 띄우기
            when (sharedViewModel.playbackMode) {
                SharedViewModel.PlaybackMode.MYAUDIOFILES -> {
                    setMiniViewInPlaylistMode(it.videoDataModel)
                    playerPlaylistRecyclerViewAdapter.submitList(
                        nowPlaylistModel.getPlayMusicList().toMutableList()
                    )

                    val newList = mutableListOf<PlayerMainRecyclerViewAdapter.PlayerFragmentMainItem>()
                    val headerTitleData =
                        PlayerMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderTitleData(
                            it.videoDataModel.title ?: ""
                        )
                    val videoDetailDataModel = VideoDetailDataModel(
                        videoTime = it.videoDataModel.date,
                        videoTitle = it.videoDataModel.title,
                        channelId = "",
                        thumbnail = "",
                        videoViewCount = String.format(resources.getString(R.string.view_count_under_thousand),0)
                    )
                    val channelDataModel = ChannelDataModel("My Music", "", "", "", String.format(resources.getString(R.string.view_count_under_thousand),0), "0", "0", "")
                    newList.add(headerTitleData)
                    val headerRestData =
                        PlayerMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderRestData(
                            videoDetailDataModel,
                            channelDataModel
                        )
                    newList.add(headerRestData)
                    mainRecyclerViewAdapter.submitList(newList)
                }
                SharedViewModel.PlaybackMode.SINGLE_VIDEO -> {

                }
                SharedViewModel.PlaybackMode.PLAYLIST -> {
                    Log.d("로그 확인","플레이리스트 모드")
                    setMiniViewInPlaylistMode(it.videoDataModel)
                    clearEachRecyclerView()
                    playerPlaylistRecyclerViewAdapter.submitList(
                        nowPlaylistModel.getPlayMusicList().toMutableList()
                    )
                    sharedViewModel.fetchAllData(it.videoDataModel.videoId)

                    if (nowPlaylistModel.getAudioEffectList() != null){
                        val audioEffects = nowPlaylistModel.getAudioEffectList() ?: return@observe
                        Log.d("로그 확인","플레이리스트 모드 오디오이펙트")
                        audioEffects[nowPlaylistModel.getCurrentPosition()]?.let{
                            setAudioEffect(it)
                        }
                    }
                }
            }

        }

        sharedViewModel.currentVideoDetailData.observe(this) {

            Log.d("로그 확인","currentVideoDetailData")
            setMiniPlayerView(it)
        }

        sharedViewModel.currentChannelDetailData.observe(this) {

            Log.d("로그 확인","currentChannelDetailData")
            val videoDetailDataModel = sharedViewModel.currentVideoDetailData.value
            val channelDataModel = sharedViewModel.currentChannelDetailData.value
            addRecyclerViewHeaderTitleView(videoDetailDataModel)
            addRecyclerViewHeaderRestView(videoDetailDataModel, channelDataModel)
        }

        sharedViewModel.currentCommentThreadData.observe(this) {
            Log.d("로그 확인","currentCommentThreadData")
            addRecyclerViewCommentDataView(it)
        }

        sharedViewModel.fromChildFragmentInNavFragment.observe(this){
            when(it){
                R.id.myAudioFileItemsFragment -> {
                    if (::myFileSearchItem.isInitialized) {
                        Log.d("로그 확인","fromChildFragmentInNavFragment 1번 째")
                        myFileSearchItem.isVisible = true
                    }
                }
                R.id.myVideoFileItemsFragment -> {
                    if (::myFileSearchItem.isInitialized) {
                        Log.d("로그 확인","fromChildFragmentInNavFragment 2번 째")
                        myFileSearchItem.isVisible = true
                    }
                }
                else -> {
                    try{
                        if (::myFileSearchItem.isInitialized) {
                            Log.d("로그 확인","fromChildFragmentInNavFragment 세번 째")
                            myFileSearchItem.isVisible = false
                        }

                    }catch (e: Exception){

                    }
                }
            }
        }
    }

    private fun addRecyclerViewHeaderTitleView(videoDetailDataModel: VideoDetailDataModel?) {
        val currentList = mainRecyclerViewAdapter.currentList.toMutableList()
        videoDetailDataModel?.let {
            val headerTitleData =
                PlayerMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderTitleData(
                    it.videoTitle
                )

            currentList.add(headerTitleData)
            Log.d("로그 확인", "addRecyclerViewHeaderTitleView $currentList")
        }

        mainRecyclerViewAdapter.submitList(currentList)
    }

    private fun addRecyclerViewHeaderRestView(
        videoDetailDataModel: VideoDetailDataModel?,
        channelDataModel: ChannelDataModel?
    ) {

        val headerRestData = PlayerMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderRestData(
            videoDetailDataModel,
            channelDataModel
        )
        val currentList = mainRecyclerViewAdapter.currentList.toMutableList()
        if (currentList.isEmpty()) {
            videoDetailDataModel?.let {
                val headerTitleData =
                    PlayerMainRecyclerViewAdapter.PlayerFragmentMainItem.HeaderTitleData(
                        it.videoTitle
                    )
                currentList.add(headerTitleData)
            }
        }
        currentList.add(headerRestData)
        Log.d("로그 확인", "addRecyclerViewHeaderRestView $currentList")
        mainRecyclerViewAdapter.submitList(currentList)
    }

    private fun addRecyclerViewCommentDataView(commentDataModelList: List<CommentDataModel>?) {
        commentDataModelList ?: return
        val commentDataList = commentDataModelList.map {
            PlayerMainRecyclerViewAdapter.PlayerFragmentMainItem.ContentData(it)
        }
        val currentList = mainRecyclerViewAdapter.currentList.toMutableList()

        currentList.addAll(commentDataList)
        mainRecyclerViewAdapter.submitList(currentList)
    }

    private fun sendUrlConvertCommand(currentVideoDataId: String) {
        controller?.pause()

        val action = "convertUrl"
        val bundle = Bundle().apply { putString("videoId", currentVideoDataId) }
        val sessionCommand = SessionCommand(action, bundle)
        controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun initController() {
        val sessionToken = SessionToken(this, ComponentName(this, MediaService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            initListener()
        }, MoreExecutors.directExecutor())
    }

    private fun initListener() {
        val controller = controller ?: return
        val myListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    Log.d("플레잉", "${controller.currentMediaItem?.mediaMetadata?.title}")
                    if (controller.currentMediaItem?.mediaMetadata?.title == "converting..")
                        binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    else
                        binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_pause_24)
                } else {
                    if (controller.currentPosition >= controller.contentDuration)
                        return
                    binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        binding.bottomPlayerPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }

                    Player.STATE_READY -> {
                        Log.d("레디가 됐는데", "왜 안돼지?")
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val myController = this@Activity.controller ?: return
                val nowPlaylistModel = sharedViewModel.nowPlaylistModel
                Log.d(TAG, "나는 다음곡을 눌렀을 때 확인을 하고 싶다 $reason")

                if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.MYAUDIOFILES) {
                    sharedViewModel.updateVideoByPosition(myController.currentMediaItemIndex)
                    setPlayingUi()
                    return
                }

                // playlist에서 미디어아이템이 장착된 후 한 곡이 끝나고 다음 곡(변환 안된)으로 재생될 때 순서 1.  변환 되고 난후  -> 2
                // 곡이 끝나고 변환된 곡으로 넘어갈 때, 1만 호출

                // 리사이클러뷰에서 아이템을 클릭하여 넘어갈 때는 2(seek) 호출 이후 변환되도 2호출

                // 처음 미디어 아이템을 장착할 때만 3 호출, 변환된 곡을 재생할 때는 2 호출
                if (nowPlaylistModel == null) {

                    when (reason) {
                        // mediaItem을 집어넣었을 때, 이때는 seek(0,0)이 확실하기 때문에 이것이 두번 실행됨
                        // 이것은 맨 처음 mediaItem을 설정할 때 호출됨
                        Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
                            setConvertingUi()
                        }
                        // 변환 후 seek 가 될 때
                        Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                            setPlayingUi()
                        }
                    }
                } else {
                    when (reason) {
                        Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
                            // 처음 미디어 아이템이 장착되었을 때 호출
                            // 처음 세팅하기 위해 작성
                            val title = mediaItem?.mediaMetadata?.title
                            val currentPosition = myController.currentMediaItemIndex
                            Log.d("PLAYLIST", "CHANGE")
                            if (title == "converting.." && currentPosition == nowPlaylistModel.getCurrentPosition()) {
                                Log.d("PLAYLIST", "CHANGE 조건문")
                                sharedViewModel.updateVideoByPosition(currentPosition)
                                setConvertingUi()
                            }
                        }

                        Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> Log.d("리피트", "호출")
                        Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                            // 곡이 끝나고 저절로 다음곡이 재생될 때 호출됨

                            // converting중일 때는
                            if (mediaItem?.mediaMetadata?.title == "converting..") {
                                Log.d("SEEK조건문 1", "호출")
                                Log.d(
                                    "포지션 확인",
                                    "${nowPlaylistModel.getCurrentPosition()} ${myController.currentMediaItemIndex}"
                                )
                                sharedViewModel.updateVideoByPosition(myController.currentMediaItemIndex)
                                setConvertingUi()
                            } else {
                                Log.d("SEEK조건문 2", "호출")
                                sharedViewModel.updateVideoByPosition(myController.currentMediaItemIndex)
                                setPlayingUi()
                            }
                        }

                        Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                            Log.d("SEEK", "호출")
                            if (myController.mediaItemCount == nowPlaylistModel.getPlayMusicList()
                                    .count()
                            ) {
                                // 이 코드는 아이템 세팅 후 seek(0,0)이 아닌 다른 position 을 재생해야 할 때 호출
                                // 즉 미디어 아이템이 추가된 후, 아이템을 클릭하여 곡을 바꿀 때만 적용됨
                                // 두가지 조건문임 -> converting 되지 않은 미디어 클릭 또는
                                // 이미 된 것을 클릭
                                if (mediaItem?.mediaMetadata?.title == "converting..") {
                                    Log.d("SEEK조건문 1", "호출")
                                    Log.d(
                                        "포지션 확인",
                                        "${nowPlaylistModel.getCurrentPosition()} ${myController.currentMediaItemIndex}"
                                    )
                                    sharedViewModel.updateVideoByPosition(myController.currentMediaItemIndex)
                                    setConvertingUi()
                                } else {
                                    Log.d("SEEK조건문 2", "호출")
                                    sharedViewModel.updateVideoByPosition(myController.currentMediaItemIndex)
                                    setPlayingUi()
                                }
                            }
                            // 변한 후 next를 눌렀을 때 호출
                            else {
                                Log.d("SEEK조건문 3", "호출")
                                setPlayingUi()
                            }
                        }


                    }
                }

                super.onMediaItemTransition(mediaItem, reason)
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.d("에러가 뭘까","${error.cause}")
                showToastMessage("failed to get stream url")
                super.onPlayerError(error)
            }
        }
        controller.addListener(myListener)
        binding.playerView.player = controller


        if (sharedViewModel.playbackMode == SharedViewModel.PlaybackMode.PLAYLIST) {
            val playModePreferences =
                getSharedPreferences("play_mode_preferences", Context.MODE_PRIVATE)
            if (playModePreferences.getInt("play_mode", 0) == 0)
                controller.repeatMode = Player.REPEAT_MODE_ALL
            else {
                controller.repeatMode = Player.REPEAT_MODE_ONE
            }
        }
    }


    override fun onBackPressed() {
        Log.d(TAG, "액티비티 백프레스")
        if (playerBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            if (sharedViewModel.isFullScreenMode) {
                activatePlayerViewOriginalScreenMode()
            } else {
                if (playlistBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                    hidePlaylistBottomSheet()
                else
                    minimizePlayerBottomSheet()
            }
        } else {
            return super.onBackPressed()
        }
    }

    private fun setAudioEffect(audioEffect: AudioEffectsDataModel){
        Log.d("플레이리스트 추가setAudioEffect", "$audioEffect")
        sharedViewModel.setAudioEffectValues(audioEffect)
        Log.d("플레이리스트 추가setAudioEffect","${sharedViewModel.pitchValue.value} ${sharedViewModel.equalizerIndexValue.value} ${sharedViewModel.isEqualizerEnabled.value} ${sharedViewModel.presetReverbIndexValue.value} ${sharedViewModel.isPresetReverbEnabled.value} ${sharedViewModel.presetReverbSendLevel.value} ${sharedViewModel.equalizerIndexValue.value}")

        setPitch(audioEffect.pitchValue)
        setTempo(audioEffect.tempoValue)
        setBassBoost(audioEffect.bassBoostValue)
        setLoudnessEnhancer(audioEffect.loudnessEnhancerValue)
        setVirtualizer(audioEffect.virtualizerValue)
        setPresetReverb(audioEffect.presetReverbIndexValue, audioEffect.presetReverbSendLevel)
        setEqualizer(audioEffect.equalizerIndexValue)
    }

    fun setPitch(value: Int){
        val controller = controller ?: return

        val semitonesFromCenter = (value - 100) * 0.1
        val adjustedPitch = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentTempoValue = controller.playbackParameters.speed
        controller.playbackParameters = PlaybackParameters(currentTempoValue, adjustedPitch)
    }

    fun setTempo(value: Int){
        val controller = controller ?: return

        val semitonesFromCenter = (value - 100) * 0.1
        val adjustedTempo = 2.0.pow(semitonesFromCenter / 12.0).toFloat()
        val currentPitchValue = controller.playbackParameters.pitch
        controller.playbackParameters = PlaybackParameters(adjustedTempo, currentPitchValue)
    }

    fun setBassBoost(value: Int){
        Log.d("베이스","보냄")
        val action = Actions.SET_BASS_BOOST
        val bundle = Bundle().apply {
            putInt("value", value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        controller?.sendCustomCommand(sessionCommand, bundle)
    }

    fun setLoudnessEnhancer(value: Int){
        Log.d("라우드","보냄")
        val action = Actions.SET_LOUDNESS_ENHANCER
        val bundle = Bundle().apply {
            putInt("value", value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        controller?.sendCustomCommand(sessionCommand, bundle)
    }

    fun setVirtualizer(value: Int){
        Log.d("버튜얼","보냄")
        val action = Actions.SET_VIRTUALIZER
        val bundle = Bundle().apply {
            putInt("value", value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun setPresetReverb(presetReverbValue: Int, sendLevel: Int){

        val action = Actions.SET_REVERB
        val bundle = Bundle().apply {
            putInt("value",presetReverbValue)
            putInt("sendLevel",sendLevel)
        }
        val sessionCommand = SessionCommand(action, bundle)
        controller?.sendCustomCommand(sessionCommand, bundle)
    }

    private fun setEqualizer(index: Int){


        val action = Actions.SET_EQUALIZER
        val bundle = Bundle().apply {
            putInt("value",index)
        }
        val sessionCommand = SessionCommand(action, bundle)
        controller?.sendCustomCommand(sessionCommand, bundle)
    }

    override fun onStop() {
        Log.d("액티비티의", "온스탑")
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(rotationObserver)
        MediaController.releaseFuture(this.controllerFuture)
        val intent = Intent(this, MediaService::class.java)
        stopService(intent)
    }

}