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
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SearchResultFragment(search: String, frameLayout: FrameLayout, searchView: SearchView): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    var fbinding: FragmentSearchResultBinding? = null
    val binding get() = fbinding!!

    var nextPageToken = ""
    private val searchWord = search
    private val videoDataList = ArrayList<VideoData>()
    private val channelDataList = ArrayList<ChannelData>()
    var videoDataListForPlayerFragment = ArrayList<VideoData>()
    var channelDataListForPlayerFragment = ArrayList<ChannelData>()
    lateinit var coroutineExceptionHandler: CoroutineExceptionHandler
    val parentFrameLayout = frameLayout
    val parentSearchView = searchView
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
        parentSearchView.setQuery(searchWord,false)
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
                    parentFragmentManager.beginTransaction()
                        .replace(parentFrameLayout.id,ChannelFragment(channelDataList[position],parentFrameLayout, parentSearchView))
                        .addToBackStack(null)
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun videoClick(v: View, position: Int) {
//                setDataListForPlayerFragment()
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.playerFragment.id,
                            PlayerFragment(videoDataList, channelDataList, position, "video"),"playerFragment")
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun optionButtonClick(v: View, position: Int) {
            }
        })

    }
//    fun setDataListForPlayerFragment(){
//        if (videoDataList.size > 48){
//            for (index in 0 until 49){
//                videoDataListForPlayerFragment.add(videoDataList[index])
//                channelDataListForPlayerFragment.add(channelDataList[index])
//            }
//        }
//        else{
//            videoDataListForPlayerFragment = videoDataList
//            channelDataListForPlayerFragment = channelDataList
//        }
//    }

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
        binding.progressBar.visibility = View.INVISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        for (index in 0 until responseData.items.size){

            val thumbnail = responseData.items[index].snippet?.thumbnails?.high?.url!!
            val rawDate = responseData.items[index].snippet?.publishedAt!!
            val date = intervalBetweenDateText(changeDateFormat(rawDate))
            val title = stringToHtmlSign(responseData.items[index].snippet?.title!!)
            val videoId = responseData.items[index].id?.videoId!!
            val channelId = responseData.items[index].snippet?.channelId!!
            val channelTitle = responseData.items[index].snippet?.channelTitle!!
            Log.d("videoId","$videoId")
            videoDataList.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date,  false))
        }
        searchResultAdapter.submitList(videoDataList.toMutableList())
    }
    /** 현재시간 구하기 ["yyyy-MM-dd HH:mm:ss"] (*HH: 24시간)*/
    fun getTime(): String {
        var now = System.currentTimeMillis()
        var date = Date(now)

        var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var getTime = dateFormat.format(date)

        return getTime
    }
    /** 두 날짜 사이의 간격 계산해서 텍스트로 반환 */
    fun intervalBetweenDateText(beforeDate: String): String {
        //현재 시간
        val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
        val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beforeDate)

        val diffSec     = (nowFormat.time - beforeFormat.time) / 1000                                           //초 차이
        val diffMin     = (nowFormat.time - beforeFormat.time) / (60*1000)                                      //분 차이
        val diffHor     = (nowFormat.time - beforeFormat.time) / (60 * 60 * 1000)                               //시 차이
        val diffDays    = diffSec / (24 * 60 * 60)                                                              //일 차이
        val diffMonths  = (nowFormat.year*12 + nowFormat.month) - (beforeFormat.year*12 + beforeFormat.month)   //월 차이
        val diffYears   = nowFormat.year - beforeFormat.year                                                    //연도 차이

        if(diffYears > 0){
            return String.format(activity.getString(R.string.publish_date_year),diffYears)
        }
        if(diffMonths > 0){
            return String.format(activity.getString(R.string.publish_date_month),diffMonths)
        }
        if (diffDays > 0){
            return String.format(activity.getString(R.string.publish_date_day),diffDays)
        }
        if(diffHor > 0){
            return String.format(activity.getString(R.string.publish_date_hour),diffHor)
        }
        if(diffMin > 0){
            return String.format(activity.getString(R.string.publish_date_minute),diffMin)
        }
        if(diffSec > 0){
            return String.format(activity.getString(R.string.publish_date_second),diffSec)
        }
        return ""
    }

    /** 날짜 형식변경 */
    fun changeDateFormat(date: String): String{
        // ["yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"] -> ["yyyy-MM-dd HH:mm"]
        try{
            val old_format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // 받은 데이터 형식
            old_format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val new_format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // 바꿀 데이터 형식
            val old_date = old_format.parse(date) //ex) "2016-11-01T15:25:31.000Z" // 000 - 밀리 세컨드
            return new_format.format(old_date)
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    private suspend fun getChannelData(videoDataList: ArrayList<VideoData>) {
        val retrofit = RetrofitYT.initRetrofit()
        for (index in videoDataList.indices) {
            val channelResponseData = retrofit.create(RetrofitService::class.java).getChannelData(
                "snippet, contentDetails, statistics, brandingSettings",
                videoDataList[index].channelId
            )
            if (channelResponseData.isSuccessful) {
                if (channelResponseData.body()?.items?.size != 0) {

                }
            }
        }
    }
    private fun addChannelData(channelResponseData: ChannelSearchData){
        val channelThumbnail = channelResponseData.items[0].snippet?.thumbnails?.default?.url!!
        val videoCount = channelResponseData.items[0].statistics?.videoCount!!
        val subscriberCount = channelResponseData.items[0].statistics?.subscriberCount!!
        val viewCount = channelResponseData.items[0].statistics?.viewCount!!
        val channelBanner = channelResponseData.items[0].brandingSettings?.image?.bannerExternalUrl
        val channelTitle = channelResponseData.items[0].snippet?.title!!
        val channelDescription = channelResponseData.items[0].snippet?.description!!
        val channelPlaylistId = channelResponseData.items[0].contentDetails?.relatedPlaylists?.uploads!!
        channelDataList.add(ChannelData(channelTitle, channelDescription, channelBanner, channelThumbnail, videoCount, viewCount, subscriberCount, channelPlaylistId))
    }

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

}