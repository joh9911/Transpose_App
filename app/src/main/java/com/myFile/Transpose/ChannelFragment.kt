package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    var pageToken = ""
    var moreVideos = true
    lateinit var activity: Activity

    private lateinit var playlistId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentChannelBinding.inflate(inflater, container, false)
        val view = binding.root
        initPlaylistId()
        initRecyclerView()
        //initRecyclerView 시 headerView가 있으므로, 스크롤이 바닥에 있다고 인식 됨 -> getData 실행됨 따라서 밑에 getData를 주석처리 했음
//        getData()
        return view
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
            override fun videoClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    activity.supportFragmentManager.beginTransaction()
                        .replace(activity.binding.playerFragment.id,PlayerFragment(videoDataList[position], null))
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showNoticeDialog(videoDataList[position])
                        }
                    }
                    true
                })
                popUp.show()
            }
        })
        binding.videoRecyclerView.adapter = channelVideoRecyclerViewAdapter
        channelVideoRecyclerViewAdapter.submitList(videoDataList)

        binding.videoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1
                // 스크롤이 끝에 도달했는지 확인
                if (!binding.videoRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    Log.d("스크롤 끝에","도달!")
                    getData()
                }
            }
        })
    }
    fun showNoticeDialog(videoData: VideoData) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

    fun initPlaylistId(){
        playlistId = channelData.channelPlaylistId
    }

    private fun getData(){
        CoroutineScope(Dispatchers.IO + CoroutineExceptionObject.coroutineExceptionHandler).launch {
            getVideoData()
        }
    }
    private suspend fun getVideoData(){
        if (!moreVideos)
            return
        val retrofit = RetrofitData.initRetrofit()
        Log.d("요청 할 때의 토큰","$pageToken")
        val response = retrofit.create(RetrofitService::class.java).getPlayListVideoItems(TOY_PROJECT, "snippet", playlistId, pageToken, "50")
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    videoMapping(response.body()!!)
                }
                if (response.body()?.nextPageToken != null){
                    pageToken = response.body()?.nextPageToken!!
                } else{
                    Log.d("페이지 토큰이","없을 때")
                    moreVideos = false
                }
            }
        }
    }

    private fun videoMapping(responseData: PlayListVideoSearchData) {
        val youtubeDigitConverter = YoutubeDigitConverter(activity)
        if (videoDataList.isNotEmpty()){
            if (videoDataList[videoDataList.size - 1].title == " ")
                videoDataList.removeAt(videoDataList.size - 1)
        }
        for (index in responseData.items.indices){
            val thumbnail = responseData.items[index].snippet?.thumbnails?.high?.url!!
            val rawDate = responseData.items[index].snippet?.publishedAt!!
            val date = youtubeDigitConverter.intervalBetweenDateText(rawDate)
            val title = stringToHtmlSign(responseData.items[index].snippet?.title!!)
            val videoId = responseData.items[index].snippet?.resourceId?.videoId!!
            val channelId = responseData.items[index].snippet?.channelId!!
            val channelTitle = channelData.channelTitle
            videoDataList.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date,  false))
        }
        if (pageToken != "")
            videoDataList.add(VideoData(" ", " ", " ", " ", " ", " ", false))
        channelVideoRecyclerViewAdapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
        Log.d("어댑터의 아이템 개수","${channelVideoRecyclerViewAdapter.itemCount}")

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
//        activity.searchView.setQuery(channelData.channelTitle,false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("채널프레그먼트","onDestroy")
        fbinding = null
    }

}