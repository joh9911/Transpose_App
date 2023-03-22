package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.Transpose.BuildConfig.TOY_PROJECT
import com.myFile.Transpose.databinding.FragmentChannelBinding
import com.myFile.Transpose.databinding.MainBinding
import com.myFile.Transpose.model.ChannelSearchData
import com.myFile.Transpose.model.PlayListVideoSearchData
import kotlinx.coroutines.*

class ChannelFragment(
    private val channelData: ChannelData
): Fragment() {
    var fbinding: FragmentChannelBinding? = null
    val binding get() = fbinding!!
    lateinit var channelVideoRecyclerViewAdapter: ChannelVideoRecyclerViewAdapter
    val videoDataList = ArrayList<VideoData>()
    val channelDataList = arrayListOf<ChannelData>()
    var pageToken = ""
    private lateinit var coroutineExceptionHandler: CoroutineExceptionHandler

    private lateinit var playlistId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentChannelBinding.inflate(inflater, container, false)
        val view = binding.root
        initExceptionHandler()
        initPlaylistId()
        initRecyclerView()
        initView()
        getData()
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
        Log.d("채널프레그먼트","onResume${parentFragment}")

        if (parentFragment is HomeFragment){
            val fragment = parentFragment as HomeFragment
            fragment.searchView.setQuery(channelData.channelTitle,false)
        }
        if (parentFragment is MyPlaylistFragment){
            val fragment = parentFragment as MyPlaylistFragment
            fragment.searchView.setQuery(channelData.channelTitle,false)
        }

    }

    fun initRecyclerView(){
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(context)
        channelVideoRecyclerViewAdapter = ChannelVideoRecyclerViewAdapter(channelData)
        channelVideoRecyclerViewAdapter.setItemClickListener(object: ChannelVideoRecyclerViewAdapter.OnItemClickListener{

            override fun channelClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    if (parentFragment is HomeFragment){
                        val fragment = parentFragment as HomeFragment
                        fragment.childFragmentManager.beginTransaction()
                            .replace(fragment.binding.searchResultFrameLayout.id, ChannelFragment(
                                channelDataList[position]
                            ))
                            .addToBackStack(null)
                            .commit()
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun videoClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
//                    activity.supportFragmentManager.beginTransaction()
//                        .replace(activity.binding.playerFragment.id,PlayerFragment(videoDataList,  position),"playerFragment")
//                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun optionButtonClick(v: View, position: Int) {
            }
        })
        Log.d("submint","보냄")

        binding.videoRecyclerView.adapter = channelVideoRecyclerViewAdapter
        channelVideoRecyclerViewAdapter.submitList(videoDataList)
    }

    fun initPlaylistId(){
        playlistId = channelData.channelPlaylistId
    }

    fun initView(){

//        binding.scrollView.viewTreeObserver?.addOnScrollChangedListener {
//            val view = binding.scrollView.getChildAt(binding.scrollView.childCount - 1)
//
//            val diff = view.bottom - (binding.scrollView.height + binding.scrollView.scrollY)
//
//            if (diff == 0) {
//                if (pageToken != ""){
//                    Log.d("ㅇ;ㄱ{","계속불리고이서")
//                    getData()
//                }
//
//                else{
//                    videoDataList.remove(VideoData(" ", " ", " ", " ", " ",  " ",false))
//                    channelVideoRecyclerViewAdapter.submitList(videoDataList.toMutableList())
//                }
//            }
//        }

    }
    private fun getData(){
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            getVideoData()
        }
    }
    private suspend fun getVideoData(){
        val retrofit = RetrofitData.initRetrofit()
        Log.d("요청 할 때의 토큰","$pageToken")
        val response = retrofit.create(RetrofitService::class.java).getPlayListVideoItems(TOY_PROJECT, "snippet", playlistId, pageToken, "50")
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
//        if (videoDataList.contains(VideoData(" ", " ", " ", " ", " ", " ", false))){
//            videoDataList.remove(VideoData(" ", " ", " ", " ", " ", " ", false))
//            channelDataList.removeAt(channelDataList.size-1)
//        }
        for (index in responseData.items.indices){
            val thumbnail = responseData.items[index].snippet?.thumbnails?.high?.url!!
            val date = responseData.items[index].snippet?.publishedAt!!.substring(0, 10)
            val title = stringToHtmlSign(responseData.items[index].snippet?.title!!)
            val videoId = responseData.items[index].snippet?.resourceId?.videoId!!
            val channelId = responseData.items[index].snippet?.channelId!!
            val channelTitle = channelData.channelTitle
            videoDataList.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date,  false))
            channelDataList.add(channelData) // 재생 프레그먼트에 전달하기 위해 걍 인자 개수를 비디오 리스트와 맞춰줌
        }
//        videoDataList.add(VideoData(" ", " ", " ", " ", " ", " ", false))
        channelDataList.add(channelData)
        Log.d("매핑","$videoDataList")
        channelVideoRecyclerViewAdapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
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

//        activity.searchView.setQuery(channelData.channelTitle,false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("채널프레그먼트","onDestroy")
    }

}