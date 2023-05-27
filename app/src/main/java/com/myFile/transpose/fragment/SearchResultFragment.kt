package com.myFile.transpose.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.*
import com.myFile.transpose.retrofit.*
import com.myFile.transpose.adapter.SearchResultFragmentRecyclerViewAdapter
import com.myFile.transpose.databinding.FragmentSearchResultBinding
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.model.PlayerFragmentBundle
import com.myFile.transpose.model.PlaylistModel
import com.myFile.transpose.model.VideoSearchData
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class SearchResultFragment: Fragment() {
    private lateinit var searchWord: String
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    var fbinding: FragmentSearchResultBinding? = null
    val binding get() = fbinding!!

    var nextPageToken = ""
    private val videoDataList = ArrayList<VideoData>()
    private val channelDataList = ArrayList<ChannelData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentSearchResultBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        getSearchWord()
        initRecyclerView()
        errorEvent()
        getData(null)
        return view
    }
    private fun getSearchWord(){
        searchWord = arguments?.getString("searchWord")!!
    }

    private fun errorEvent(){
        binding.refreshButton.setOnClickListener {
            binding.refreshButton.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.errorLinearLayout.visibility = View.INVISIBLE
                videoDataList.clear()
                getData(null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("서치프레그먼트","온리즘")
        if (parentFragment is HomeFragment){
            val fragment =  parentFragment as HomeFragment
            fragment.searchView.setQuery(searchWord,false)
        }

        if (parentFragment is MyPlaylistFragment){
            val fragment = parentFragment as MyPlaylistFragment
            fragment.searchView.setQuery(searchWord,false)
        }
    }

    fun initRecyclerView(){
        activity = context as Activity
        activity.binding.bottomNavigationView.visibility = View.VISIBLE
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        searchResultAdapter = SearchResultFragmentRecyclerViewAdapter()
        binding.recyclerView.adapter = searchResultAdapter
        searchResultAdapter.setItemClickListener(object: SearchResultFragmentRecyclerViewAdapter.OnItemClickListener{

            override fun channelClick(v: View, position: Int) {
                    if (parentFragment is HomeFragment){
                        val channelData = channelDataList[position]
                        val bundle = Bundle().apply {
                            putParcelable("channelData",channelData)
                        }
                        val channelFragment = ChannelFragment().apply {
                            arguments = bundle
                        }
                        val fragment = parentFragment as HomeFragment
                        parentFragmentManager.beginTransaction()
                            .replace(fragment.binding.searchResultFrameLayout.id,
                                channelFragment
                            )
                            .addToBackStack(null)
                            .commit()
                    }
                    if (parentFragment is MyPlaylistFragment){
                        val channelData = channelDataList[position]
                        val bundle = Bundle().apply {
                            putParcelable("channelData",channelData)
                        }
                        val channelFragment = ChannelFragment().apply {
                            arguments = bundle
                        }
                        val fragment = parentFragment as MyPlaylistFragment
                        parentFragmentManager.beginTransaction()
                            .replace(fragment.binding.resultFrameLayout.id,
                                channelFragment
                            )
                            .addToBackStack(null)
                            .commit()

                }
            }
            override fun videoClick(v: View, position: Int) {
                val playlistModel = null
                val videoData = videoDataList[position]
                val playerFragmentBundle = PlayerFragmentBundle(videoData, playlistModel)

                val bundle = Bundle().apply {
                    putParcelable("playerFragment", playerFragmentBundle)
                }
                val playerFragment = PlayerFragment().apply {
                    arguments = bundle
                }
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.playerFragment.id,
                            playerFragment
                        )
                        .commit()

            }
            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showNoticeDialog(videoDataList[position])
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
         binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount-1
                // 스크롤이 끝에 도달했는지 확인
                if (!binding.recyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    Log.d("스크롤 끝에","도달!")
                    getData(nextPageToken)
                }
            }
        })
    }
    fun showNoticeDialog(videoData: VideoData) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

    private fun getData(pageToken: String?) {
        CoroutineScope(Dispatchers.IO + CoroutineExceptionObject.coroutineExceptionHandler).launch {
            async { getSearchVideoData(pageToken) }
        }
    }

    private suspend fun getSearchVideoData(pageToken: String?) {
        val random = Random()
        val keyList = listOf(BuildConfig.API_KEY6,  BuildConfig.API_KEY11,
         BuildConfig.TOY_PROJECT, BuildConfig.API_KEY110901_1, BuildConfig.API_KEY110901_2,BuildConfig.API_KEY11098608_1, BuildConfig.API_KEY11098608_2,
        BuildConfig.API_KEY110999_1, BuildConfig.API_KEY110999_2, BuildConfig.API_KEY38922_1, BuildConfig.API_KEY38922_2, BuildConfig.API_KEY389251_1, BuildConfig.API_KEY389251_2,
        BuildConfig.API_KEY860801_1,BuildConfig.API_KEY860801_2, BuildConfig.API_KEY991101_1, BuildConfig.API_KEY991101_2,BuildConfig.API_KEY38924_1,BuildConfig.API_KEY38924_2,BuildConfig.API_KEY38926_1,
        BuildConfig.API_KEY38926_2,BuildConfig.API_KEY38928_1, BuildConfig.API_KEY38928_2, BuildConfig.API_KEY38929_1, BuildConfig.API_KEY38929_2,BuildConfig.API_KEY38930_1,BuildConfig.API_KEY38930_2,
        BuildConfig.API_KEY38931_1,BuildConfig.API_KEY38931_2,BuildConfig.API_KEY38933_1,BuildConfig.API_KEY38933_2,BuildConfig.API_KEY38934_1,BuildConfig.API_KEY38934_2)
        val num = random.nextInt(keyList.size)

        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java).getVideoSearchResult(
            keyList[num],"snippet",searchWord,"50","video",
            pageToken
        )
        Log.d("검색 기능0","$response")
        if (response.isSuccessful) {
            if (response.body()?.items?.size != 0) {
                withContext(Dispatchers.Main){
                    searchResultDataMapping(response.body()!!)
                }
                if (response.body()?.nextPageToken != null)
                    nextPageToken = response.body()?.nextPageToken!!
            }
        }
        else{
            /**
             * 처음 검색했을 때, 실패가 떴다면 다시 처음부터 검색
             */
            if (videoDataList.isEmpty()){
                withContext(Dispatchers.Main){
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.recyclerView.visibility = View.INVISIBLE
                    binding.errorLinearLayout.visibility = View.VISIBLE
                }
            }

            else{
                withContext(Dispatchers.Main){
                    if (videoDataList[videoDataList.size - 1].title == " ")
                        videoDataList.removeAt(videoDataList.size - 1)
                    Toast.makeText(activity, activity.getString(R.string.quota_error_message),Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
    private fun searchResultDataMapping(responseData: VideoSearchData){
        val youtubeDigitConverter = YoutubeDigitConverter(activity)
        binding.progressBar.visibility = View.INVISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        if (videoDataList.isNotEmpty()){
            if (videoDataList[videoDataList.size - 1].title == " ")
                videoDataList.removeAt(videoDataList.size - 1)
        }
        for (index in 0 until responseData.items.size){
            val thumbnail = responseData.items[index].snippet?.thumbnails?.high?.url!!
            val rawDate = responseData.items[index].snippet?.publishedAt!!
            val date = youtubeDigitConverter.intervalBetweenDateText(rawDate)
            val title = stringToHtmlSign(responseData.items[index].snippet?.title!!)
            val videoId = responseData.items[index].id?.videoId!!
            val channelId = responseData.items[index].snippet?.channelId!!
            val channelTitle = responseData.items[index].snippet?.channelTitle!!
            Log.d("videoId","$videoId")
            videoDataList.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date,  false))
        }
        videoDataList.add(VideoData(" ", " ", " ", " ", " ", " ", false))
        searchResultAdapter.submitList(videoDataList.toMutableList())
        Log.d("어댑터의 아이템 개수","${searchResultAdapter.itemCount}")
    }

//    private suspend fun getChannelData(videoDataList: ArrayList<VideoData>) {
//        val retrofit = RetrofitYT.initRetrofit()
//        for (index in videoDataList.indices) {
//            val channelResponseData = retrofit.create(RetrofitService::class.java).getChannelData(
//                "snippet, contentDetails, statistics, brandingSettings",
//                videoDataList[index].channelId
//            )
//            if (channelResponseData.isSuccessful) {
//                if (channelResponseData.body()?.items?.size != 0) {
//
//                }
//            }
//        }
//    }

//    private suspend fun getChannelData(responseData: VideoSearchData) {
//        for (index in 0 until responseData.items.size) {
//            val retrofit = RetrofitYT.initRetrofit()
//            val channelResponseData = retrofit.create(RetrofitService::class.java).getChannelData(
//                "snippet, contentDetails, statistics, brandingSettings",
//                responseData.items[index].snippet?.channelId
//            )
//            if (channelResponseData.isSuccessful){
//                if (channelResponseData.body()?.items?.size != 0){
//                    withContext(Dispatchers.Main){
//                        resultDataMapping(responseData,channelResponseData.body()!!, index)
//                    }
//                }
//            }
//        }
//    }


//    private fun resultDataMapping(
//        searchResponseData: VideoSearchData,
//        channelResponseData: ChannelSearchData,
//        index: Int
//    ){
//        if (videoDataList.contains(VideoData(" ", " ", " ", " ", " ",  false)))
//            videoDataList.remove(VideoData(" ", " ", " ", " ", " ",  false))
//        val thumbnail = searchResponseData.items[index].snippet?.thumbnails?.high?.url!!
//        val date = searchResponseData.items[index].snippet?.publishedAt!!.substring(0, 10)
//        val title = stringToHtmlSign(searchResponseData.items[index].snippet?.title!!)
//        val videoId = searchResponseData.items[index].id?.videoId!!
//        val channelThumbnail = channelResponseData.items[0].snippet?.thumbnails?.default?.url!!
//        val videoCount = channelResponseData.items[0].statistics?.videoCount!!
//        val subscriberCount = channelResponseData.items[0].statistics?.subscriberCount!!
//        val viewCount = channelResponseData.items[0].statistics?.viewCount!!
//        val channelBanner = channelResponseData.items[0].brandingSettings?.image?.bannerExternalUrl
//        val channelTitle = channelResponseData.items[0].snippet?.title!!
//        val channelDescription = channelResponseData.items[0].snippet?.description!!
//        val channelPlaylistId = channelResponseData.items[0].contentDetails?.relatedPlaylists?.uploads!!
//
//        binding.progressBar.visibility = View.INVISIBLE
//        binding.recyclerView.visibility = View.VISIBLE
//        videoDataList.add(VideoData(thumbnail, title, channelTitle, videoId, date, channelThumbnail, false))
//        channelDataList.add(ChannelData(channelTitle, channelDescription, channelBanner, channelThumbnail, videoCount, viewCount, subscriberCount, channelPlaylistId))
//        searchResultAdapter.submitList(videoDataList.toMutableList())
//
//        if (index == searchResponseData.items.size-1){
//            videoDataList.add(VideoData(" ", " ", " ", " ", " ", " ", false))
//            searchResultAdapter.submitList(videoDataList.toMutableList())
//            binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val lastVisibleItemPosition =
//                        (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
//                    val itemTotalCount = recyclerView.adapter!!.itemCount-1
//                    // 스크롤이 끝에 도달했는지 확인
//                    if (!binding.recyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
//                        Log.d("스크롤","도달")
//                        getData(nextPageToken)
//                    }
//                }
//            })
//        }
//    }


    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }

    override fun onDetach() {
        Log.d("searchResultFragment","종료")
        super.onDetach()
    }
    override fun onDestroy() {
        super.onDestroy()
        fbinding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is HomeFragment){
            val fragment =  parentFragment as HomeFragment
            fragment.childFragmentManager.addOnBackStackChangedListener {
                if (this@SearchResultFragment.isResumed)
                    fragment.searchView.setQuery(searchWord,false)
            }
        }
        if (parentFragment is MyPlaylistFragment){
            val fragment = parentFragment as MyPlaylistFragment
            fragment.childFragmentManager.addOnBackStackChangedListener {
                if (this@SearchResultFragment.isResumed){
                    fragment.searchView.setQuery(searchWord,false)
                }
            }
        }

    }

}