package com.myFile.transpose.view.fragment

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.myFile.transpose.*
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.SearchResultFragmentRecyclerViewAdapter
import com.myFile.transpose.databinding.FragmentSearchResultBinding
import com.myFile.transpose.view.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.data.model.VideoDataModel
import com.myFile.transpose.others.constants.Actions.TAG
import com.myFile.transpose.viewModel.SearchResultViewModel
import com.myFile.transpose.viewModel.SearchResultViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel
import java.net.URLEncoder


class SearchResultFragment: Fragment() {
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    var fbinding: FragmentSearchResultBinding? = null
    val binding get() = fbinding!!

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var searchResultViewModel: SearchResultViewModel

    var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentSearchResultBinding.inflate(inflater, container, false)
        val view = binding.root
        initViewModel()
        Log.d(TAG, "searchResultFragment onCreate")
//        initRecyclerView()
//        errorEvent()
        initWebView()
        return view
    }



    private fun initWebView(){
        val searchKeyword = sharedViewModel.searchKeyword
        val encodedSearchTerm = URLEncoder.encode(searchKeyword, "UTF-8")



        binding.webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("시작 페이지","$url")
                view?.post{
                    view.scrollTo(0, currentPosition)
                }
                binding.progressBar.visibility = View.GONE
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("페이지 로딩 끝","$url")

            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                Log.d("shouldOverride", request?.url.toString())


                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val urlString = request?.url.toString()

                Log.d("요청된 url","$urlString")
                if (urlString.contains("m.youtube.com/api/stats/qoe") && urlString.contains("docid=") && urlString.contains("&view=")) {

                    Log.d("서치 리저트 선택된 url","${urlString}")

                    if (!searchResultViewModel.isIntercepted){
                        val videoId = Regex("docid=([^&]*)").find(urlString)?.groups?.get(1)?.value
                        searchResultViewModel.isIntercepted = true
                        videoId?.let {
                            Log.d("서치 리저트", it)
                            if (sharedViewModel.searchResultMode == SharedViewModel.SearchResultMode.SearchKeyword){

                                view?.post {
                                    Log.d("서치 리저트", "goBack")
                                    Log.d("서치 현재 url","${view.url}")
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        view.stopLoading()
                                        view.loadUrl("https://www.youtube.com/results?search_query=$encodedSearchTerm")
                                    },0)

                                }
                            }
                            else{
                                view?.post{
                                    view.stopLoading()
                                    view.destroy()
                                    findNavController().navigateUp()
                                }
                            }
                            sharedViewModel.setSingleModeVideoId(videoId)
                            activity.activatePlayerInSingleMode(videoId)
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }


        binding.webView.settings.javaScriptEnabled = true

        binding.webView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                searchResultViewModel.isIntercepted = false
            }
            false // 이벤트를 다음 대상으로 전달하기 위해 false 반환
        }

        binding.webView.settings.useWideViewPort = false
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.mediaPlaybackRequiresUserGesture = true
        binding.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.webView.setOnScrollChangeListener { p0, p1, p2, p3, p4 -> currentPosition = p4 }
        }

        try{
            if (sharedViewModel.searchResultMode == SharedViewModel.SearchResultMode.SearchKeyword)
                binding.webView.loadUrl("https://www.youtube.com/results?search_query=$encodedSearchTerm")
            else
                binding.webView.loadUrl(sharedViewModel.sharedLink)
        }catch (e: Exception){
            Log.d("웹뷰","로드중 예외 발생${e}")
            binding.webView.post {
                binding.webView.stopLoading()
                binding.webView.destroy()
                findNavController().navigateUp()
            }
        }
    }

    override fun onPause() {
        searchResultViewModel.isIntercepted = true
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
    }


    fun extractVideoIdFromUrl(url: String): String? {
        // 정규식 패턴들
        val patterns = listOf(
            "https://www\\.youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)", // 일반적인 형태
            "https://youtu\\.be/([a-zA-Z0-9_-]+)" // 짧은 형태
        )

        for (pattern in patterns) {
            val matcher = Regex(pattern).find(url)
            if (matcher != null && matcher.groupValues.size > 1) {
                return matcher.groupValues[1]
            }
        }
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        sharedViewModel.fromChildFragmentInNavFragment.value = findNavController().currentDestination?.id
//        searchResultViewModel.deleteOldData()
//        searchResultViewModel.firstFetchOrGetData(StringUtils.getDateStrings(requireContext()))
    }

    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = SearchResultViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        searchResultViewModel = ViewModelProvider(this, viewModelFactory)[SearchResultViewModel::class.java]

    }

    private fun initObserver(){

//        searchResultViewModel.videoSearchDataList.observe(viewLifecycleOwner){
//            addRecyclerViewItem(it)
//            searchResultViewModel.cashData(it)
//            visibilityEventWhenSuccess()
//        }

//        searchResultViewModel.isServerError.observe(viewLifecycleOwner){ isServerError ->
//            val currentList = searchResultViewModel.videoSearchDataList.value ?: arrayListOf()
//            if (currentList.isEmpty()){
//                if (isServerError){
//                    binding.errorMessage.text = getString(R.string.quota_error_message)
//                    visibilityEventWhenError()
//                }
//            }else{
//                activity.showToastMessage(getString(R.string.quota_error_message))
//            }
//        }

//        searchResultViewModel.isExceptionError.observe(viewLifecycleOwner){ exception ->
//            val currentList = searchResultViewModel.videoSearchDataList.value ?: arrayListOf()
//            if (currentList.isEmpty()){
//                when(exception){
//                    is UnknownHostException -> binding.errorMessage.text = getString(R.string.network_error_text)
//                    is SocketTimeoutException -> binding.errorMessage.text = getString(R.string.load_fail_message)
//                }
//                visibilityEventWhenError()
//            }
//            else{
//                when(exception){
//                    is UnknownHostException -> activity.showToastMessage(getString(R.string.network_error_text))
//                    is SocketTimeoutException -> activity.showToastMessage(getString(R.string.load_fail_message))
//                }
//            }
//        }
    }

    private fun addRecyclerViewItem(arrayList: ArrayList<VideoDataModel>) {
        val currentList = searchResultAdapter.currentList.toMutableList()
        val loadingData = SearchResultFragmentRecyclerViewAdapter.SearchResultRecyclerViewItem.LoadingData
        if (currentList.isNotEmpty() && currentList.last() == loadingData)
            currentList.removeLast()
        val newItems = arrayList.map{
            SearchResultFragmentRecyclerViewAdapter.SearchResultRecyclerViewItem.ItemData(it)
        }
        currentList.addAll(newItems)
        currentList.add(loadingData)
        searchResultAdapter.submitList(currentList)
    }

//    private fun errorEvent(){
//        binding.refreshButton.setOnClickListener {
//            visibilityEventWhenLoading()
//            binding.errorLinearLayout.visibility = View.INVISIBLE
//            searchResultViewModel.firstFetchOrGetData(StringUtils.getDateStrings(requireContext()))
//        }
//    }

//    override fun onResume() {
//        super.onResume()
////        if (parentFragment is HomeFragment){
////            val fragment =  parentFragment as HomeFragment
////            val searchKeyword = searchResultViewModel.searchKeyword.value ?: ""
////            fragment.searchView.setQuery(searchKeyword,false)
////        }
////
////        if (parentFragment is MyPlaylistsFragment){
////            val fragment = parentFragment as MyPlaylistsFragment
////            val searchKeyword = searchResultViewModel.searchKeyword.value ?: ""
////            fragment.searchView.setQuery(searchKeyword,false)
////        }
//    }
//
//    private fun initRecyclerView(){
//        activity.binding.bottomNavigationView.visibility = View.VISIBLE
//        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
//        searchResultAdapter = SearchResultFragmentRecyclerViewAdapter()
//        binding.recyclerView.adapter = searchResultAdapter
//        searchResultAdapter.setItemClickListener(object: SearchResultFragmentRecyclerViewAdapter.OnItemClickListener{
//
//            override fun channelClick(v: View, position: Int) {
//                    if (parentFragment is HomeFragment){
//                        val channelData = channelDataList[position]
//                        val channelFragment = ChannelFragment().apply {
//                            arguments = Bundle().apply {
//                                putParcelable("channelData",channelData)
//                            }
//                        }
//                        val fragment = parentFragment as HomeFragment
//                        parentFragmentManager.beginTransaction()
//                            .replace(fragment.binding.searchResultFrameLayout.id,
//                                channelFragment
//                            )
//                            .addToBackStack(null)
//                            .commit()
//                    }
//                    if (parentFragment is MyPlaylistsFragment){
//                        val channelData = channelDataList[position]
//                        val channelFragment = ChannelFragment().apply {
//                            arguments = Bundle().apply {
//                                putParcelable("channelData",channelData)
//                            }
//                        }
//                        val fragment = parentFragment as MyPlaylistsFragment
//                        parentFragmentManager.beginTransaction()
//                            .replace(fragment.binding.resultFrameLayout.id,
//                                channelFragment
//                            )
//                            .addToBackStack(null)
//                            .commit()
//
//                }
//            }
//            override fun videoClick(v: View, position: Int) {
//                val videoSearchList = searchResultViewModel.videoSearchDataList.value
//                if (videoSearchList != null){
//
//                }
//            }
//            override fun optionButtonClick(v: View, position: Int) {
//                val videoSearchList = searchResultViewModel.videoSearchDataList.value
//                if (videoSearchList != null){
//                    val videoData = videoSearchList[position]
//
//                    val popUp = PopupMenu(activity, v)
//                    popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
//                    popUp.setOnMenuItemClickListener {
//                        when (it.itemId) {
//                            R.id.add_my_playlist -> {
//                                showNoticeDialog(videoData)
//                            }
//                        }
//                        true
//                    }
//                    popUp.show()
//                }
//            }
//        })
//
//         binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val lastVisibleItemPosition =
//                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
//                val itemTotalCount = recyclerView.adapter!!.itemCount-1
//                // 스크롤이 끝에 도달했는지 확인
//                if (!binding.recyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
//                    Log.d("스크롤 끝에","도달")
//                    searchResultViewModel.fetchVideoSearchData(StringUtils.getDateStrings(requireContext()))
//                }
//            }
//        })
//    }
    fun showNoticeDialog(videoData: VideoDataModel) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }
//
//    private fun visibilityEventWhenError(){
//        binding.progressBar.visibility = View.GONE
//        binding.recyclerView.visibility = View.INVISIBLE
//        binding.errorLinearLayout.visibility = View.VISIBLE
//    }
//    private fun visibilityEventWhenLoading(){
//        binding.progressBar.visibility = View.VISIBLE
//        binding.recyclerView.visibility = View.INVISIBLE
//    }
//    private fun visibilityEventWhenSuccess(){
//        binding.progressBar.visibility = View.GONE
//        binding.recyclerView.visibility = View.VISIBLE
//    }



//    private suspend fun getChannelData(searchResultDataList: ArrayList<VideoData>) {
//        val retrofit = RetrofitYT.initRetrofit()
//        for (index in searchResultDataList.indices) {
//            val channelResponseData = retrofit.create(RetrofitService::class.java).getChannelData(
//                "snippet, contentDetails, statistics, brandingSettings",
//                searchResultDataList[index].channelId
//            )
//            if (channelResponseData.isSuccessful) {
//                if (channelResponseData.body()?.items?.size != 0) {
//
//                }
//            }
//        }
//    }


    override fun onDetach() {

        super.onDetach()
    }
    override fun onDestroy() {
        Log.d("searchResultFragment","종료")
        super.onDestroy()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.stopLoading()
        binding.webView.destroy()
        fbinding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
//        if (parentFragment is HomeFragment){
//            val fragment =  parentFragment as HomeFragment
//            fragment.childFragmentManager.addOnBackStackChangedListener {
//                if (this@SearchResultFragment.isResumed){
//                    val searchKeyword = searchResultViewModel.searchKeyword.value ?: ""
//                    fragment.searchView.setQuery(searchKeyword,false)
//                }
//
//            }
//        }
//        if (parentFragment is MyPlaylistsFragment){
//            val fragment = parentFragment as MyPlaylistsFragment
//            fragment.childFragmentManager.addOnBackStackChangedListener {
//                if (this@SearchResultFragment.isResumed){
//                    val searchKeyword = searchResultViewModel.searchKeyword.value ?: ""
//                    fragment.searchView.setQuery(searchKeyword,false)
//                }
//            }
//        }
    }

}