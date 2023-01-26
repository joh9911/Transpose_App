package com.example.youtube_transpose

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youtube_transpose.databinding.FragmentSearchResultBinding
import com.example.youtube_transpose.databinding.MainBinding
import kotlinx.coroutines.*

class SearchResultFragment(search: String): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    var fbinding: FragmentSearchResultBinding? = null
    val binding get() = fbinding!!

    val search = search
    val API_KEY = "AIzaSyBZlnQ_kRZ7mvs0wL31ezbBeEPYAoIM3EM"
    val videoData = ArrayList<VideoData>()
    val channelData = ArrayList<ChannelData>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentSearchResultBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        Log.d("searchResultFragment","onccreateview")
        initRecyclerView()
        getData()
//        getResultData()
        return view
    }

    fun initRecyclerView(){
        activity = context as Activity
        activity.binding.bottomNavigationView.visibility = View.VISIBLE
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        searchResultAdapter = SearchResultFragmentRecyclerViewAdapter(videoData)
        binding.recyclerView.adapter = searchResultAdapter
        searchResultAdapter.setItemClickListener(object: SearchResultFragmentRecyclerViewAdapter.OnItemClickListener{

            override fun channelClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.channelFragment.id,ChannelFragment(channelData[position]))
                        .addToBackStack(null)
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }

            override fun videoClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.playerFragment.id,PlayerFragment(videoData, position, "video"),"playerFragment")
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }

            override fun optionButtonClick(v: View, position: Int) {

            }
        })
    }
    private fun getData(){
        val job = CoroutineScope(Dispatchers.IO).launch {
            getSearchVideoData()
        }
    }
    suspend fun getSearchVideoData(){
        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java).getVideoDetails(API_KEY,"snippet",search,"50","video")
        if (response.isSuccessful) {
            if (response.body()?.items?.size != 0) {
                getChannelData(response.body()!!)
            }
        }
    }
    suspend fun getChannelData(responseData: VideoSearchData) {
        for (index in 0 until responseData.items.size) {
            Log.d("채널 데이터가","가져와짐")
            val retrofit = RetrofitData.initRetrofit()
            val channelResponseData = retrofit.create(RetrofitService::class.java).getChannelData(
                API_KEY,
                "snippet, contentDetails, statistics, brandingSettings",
                responseData.items[index].snippet?.channelId!!
            )
            if (channelResponseData.isSuccessful){
                if (channelResponseData.body()?.items?.size != 0){
                    withContext(Dispatchers.Main){
                        resultDataMapping(responseData,channelResponseData.body()!!, index)
                    }
                }
            }
        }
    }

    private fun resultDataMapping(
        searchResponseData: VideoSearchData,
        channelResponseData: ChannelSearchData,
        index: Int
    ){
        val thumbnail = searchResponseData.items[index].snippet?.thumbnails?.high?.url!!
        val date = searchResponseData.items[index].snippet?.publishedAt!!.substring(0, 10)
        val title = stringToHtmlSign(searchResponseData.items[index].snippet?.title!!)
        val videoId = searchResponseData.items[index].id?.videoId!!
        val channelThumbnail = channelResponseData.items[0].snippet?.thumbnails?.default?.url!!
        val videoCount = channelResponseData.items[0].statistics?.videoCount!!
        val subscriberCount = channelResponseData.items[0].statistics?.subscriberCount!!
        val viewCount = channelResponseData.items[0].statistics?.viewCount!!
        val channelBanner =
            channelResponseData.items[0].brandingSettings?.image?.bannerExternalUrl
        val channelTitle = channelResponseData.items[0].snippet?.title!!
        val channelDescription = channelResponseData.items[0].snippet?.description!!
        val channelPlaylistId =
            channelResponseData.items[0].contentDetails?.relatedPlaylists?.uploads!!
        binding.progressBar.visibility = View.INVISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        videoData.add(VideoData(thumbnail, title, channelTitle, videoId, date, channelThumbnail))
        channelData.add(ChannelData(channelTitle, channelDescription, channelBanner, channelThumbnail, videoCount, viewCount, subscriberCount, channelPlaylistId))
        searchResultAdapter.notifyDataSetChanged()
    }


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