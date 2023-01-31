package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.Transpose.databinding.FragmentChannelBinding
import com.myFile.Transpose.databinding.MainBinding
import kotlinx.coroutines.*

class ChannelFragment(val channelData: ChannelData): Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentChannelBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    val videoDataList = ArrayList<VideoData>()
    var pageToken = ""

    val API_KEY = com.myFile.Transpose.BuildConfig.API_KEY
    lateinit var playlistId: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentChannelBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initPlaylistId()
        initRecyclerView()
        initView()
        getData()
        return view
    }

    fun initRecyclerView(){
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchResultAdapter = SearchResultFragmentRecyclerViewAdapter()
        searchResultAdapter.setItemClickListener(object: SearchResultFragmentRecyclerViewAdapter.OnItemClickListener{

            override fun channelClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.channelFragment.id,ChannelFragment(channelData))
                        .addToBackStack(null)
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun videoClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.playerFragment.id,PlayerFragment(videoDataList, position, "video"),"playerFragment")
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun optionButtonClick(v: View, position: Int) {
            }
        })
        binding.videoRecyclerView.adapter = searchResultAdapter
    }

    fun initPlaylistId(){
        playlistId = channelData.channelPlaylistId
        Log.d("플레이리스트 아이디","$playlistId")
    }

    fun initView(){
        binding.channelTitle.text = channelData.channelTitle
        Glide.with(binding.channelBanner)
            .load(channelData.channelBanner)
            .into(binding.channelBanner)
        binding.channelDescription.text = channelData.channelDescription
        binding.channelInfo.text = "동영상 ${channelData.channelVideoCount}개"
        binding.scrollView.viewTreeObserver?.addOnScrollChangedListener {
            val view = binding.scrollView.getChildAt(binding.scrollView.childCount - 1)

            val diff = view.bottom - (binding.scrollView.height + binding.scrollView.scrollY)

            if (diff == 0) {
                if (pageToken != "")
                    getData()
                else{
                    videoDataList.remove(VideoData(" ", " ", " ", " ", " ", " ", false))
                    searchResultAdapter.submitList(videoDataList.toMutableList())
                }
            }
        }

    }
    private fun getData(){
        val job = CoroutineScope(Dispatchers.IO).launch {
            getVideoData()
        }
    }
    private suspend fun getVideoData(){
        val retrofit = RetrofitData.initRetrofit()
        Log.d("요청 할 때의 토큰","$pageToken")
        val response = retrofit.create(RetrofitService::class.java).getPlayListVideoItems(API_KEY, "snippet", playlistId, pageToken, "50")
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    videoMapping(response.body()!!)
                }
                pageToken = if (response.body()?.nextPageToken != null){
                    response.body()?.nextPageToken!!
                } else
                    ""
            }
        }
    }

    fun videoMapping(responseData: PlayListVideoSearchData) {
        if (videoDataList.contains(VideoData(" ", " ", " ", " ", " ", " ", false)))
            videoDataList.remove(VideoData(" ", " ", " ", " ", " ", " ", false))
        for (index in responseData.items.indices){
            val thumbnail = responseData.items[index].snippet?.thumbnails?.high?.url!!
            val date = responseData.items[index].snippet?.publishedAt!!.substring(0, 10)
            val title = stringToHtmlSign(responseData.items[index].snippet?.title!!)
            val videoId = responseData.items[index].snippet?.resourceId?.videoId!!
            val channelThumbnail = channelData.channelThumbnail
            val channelTitle = channelData.channelTitle
            videoDataList.add(VideoData(thumbnail, title, channelTitle, videoId, date, channelThumbnail, false))
        }
        videoDataList.add(VideoData(" ", " ", " ", " ", " ", " ", false))
        searchResultAdapter.submitList(videoDataList.toMutableList())
        binding.progressBar.visibility = View.GONE
        binding.videoRecyclerView.visibility = View.VISIBLE
    }

    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

}