package com.myFile.Transpose

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.Transpose.BuildConfig.API_KEY
import com.myFile.Transpose.databinding.FragmentSearchResultBinding
import com.myFile.Transpose.databinding.MainBinding
import com.myFile.Transpose.model.ChannelSearchData
import com.myFile.Transpose.model.VideoSearchData
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SearchResultFragment(search: String): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    var fbinding: FragmentSearchResultBinding? = null
    val binding get() = fbinding!!

    var nextPageToken = ""
    private val searchWord = search
    private val videoDataList = ArrayList<VideoData>()
    private val channelDataList = ArrayList<ChannelData>()
    lateinit var coroutineExceptionHandler: CoroutineExceptionHandler


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentSearchResultBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initExceptionHandler()
        initRecyclerView()
        Log.d("searchResultFragment","실행")
        getData(null)
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

    override fun onResume() {
        super.onResume()
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
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    if (parentFragment is HomeFragment){
                        val fragment = parentFragment as HomeFragment
                        parentFragmentManager.beginTransaction()
                            .replace(fragment.binding.searchResultFrameLayout.id,ChannelFragment(channelDataList[position]))
                            .addToBackStack(null)
                            .commit()
                    }
                    if (parentFragment is MyPlaylistFragment){
                        val fragment = parentFragment as MyPlaylistFragment
                        parentFragmentManager.beginTransaction()
                            .replace(fragment.binding.resultFrameLayout.id,ChannelFragment(channelDataList[position]))
                            .addToBackStack(null)
                            .commit()
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun videoClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.playerFragment.id,
                            PlayerFragment(videoDataList[position],null))
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun optionButtonClick(v: View, position: Int) {
            }
        })
    }

    private fun getData(pageToken: String?) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            async { getSearchVideoData(pageToken) }
        }
    }


    private suspend fun getSearchVideoData(pageToken: String?) {
        val retrofit = RetrofitYT.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java).getVideoSearchResult("snippet",searchWord,"50","video",
            pageToken
        )
        if (response.isSuccessful) {
            if (response.body()?.items?.size != 0) {
                withContext(Dispatchers.Main){
                    searchResultDataMapping(response.body()!!)
                }
                if (response.body()?.nextPageToken != null)
                    nextPageToken = response.body()?.nextPageToken!!
            }
        }
    }
    private fun searchResultDataMapping(responseData: VideoSearchData){
        val youtubeDigitConverter = YoutubeDigitConverter(activity)
        binding.progressBar.visibility = View.INVISIBLE
        binding.recyclerView.visibility = View.VISIBLE
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
        searchResultAdapter.submitList(videoDataList.toMutableList())
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

}