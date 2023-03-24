package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.Transpose.databinding.FragmentPlaylistBinding
import com.myFile.Transpose.databinding.MainBinding
import com.myFile.Transpose.model.PlayListVideoSearchData
import com.myFile.Transpose.model.PlaylistModel
import kotlinx.coroutines.*

class PlaylistItemsFragment(playListData: PlayListData): Fragment() {
    private val playListData = playListData

    val API_KEY = com.myFile.Transpose.BuildConfig.API_KEY
    lateinit var activity: Activity
    lateinit var playlistItemsRecyclerViewAdapter: PlaylistItemsRecyclerViewAdapter
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentPlaylistBinding? = null
    val binding get() = fbinding!!
    lateinit var coroutineExceptionHandler: CoroutineExceptionHandler

    val playlistVideoData = arrayListOf<VideoData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentPlaylistBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initExceptionHandler()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    fun initView(){
        Glide.with(binding.playlistThumbnail)
            .load(playListData.thumbnail)
            .into(binding.playlistThumbnail)
        binding.playlistTitle.text = playListData.title
        binding.playlistDescription.text = playListData.description
        binding.playlistItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        playlistItemsRecyclerViewAdapter = PlaylistItemsRecyclerViewAdapter()
        playlistItemsRecyclerViewAdapter.setItemClickListener(object: PlaylistItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistModel = PlaylistModel(playListData.title,playlistVideoData)
                activity.supportFragmentManager.beginTransaction()
                    .replace(activity.binding.playerFragment.id,PlayerFragment(playlistVideoData[position],playlistModel))
                    .commit()
            }

        })
        binding.playlistItemRecyclerView.adapter = playlistItemsRecyclerViewAdapter
    }

    fun getData(){
        val job = CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            getPlaylistItemsData(null)
        }
    }

    private suspend fun getPlaylistItemsData(nextPageToken: String?){
        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java).getPlayListVideoItems(API_KEY,"snippet",playListData.playlistId,nextPageToken,"50")
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0) {
                withContext(Dispatchers.Main) {
                    playlistItemsMapper(response.body()!!)
                }
            }
        }
    }
    private fun playlistItemsMapper(playlistItemsResponse: PlayListVideoSearchData) {
        for (index in 0 until playlistItemsResponse.items.size!!) {
            Log.d("맴퍼","진행햇음")
            val thumbnail =
                playlistItemsResponse.items[index].snippet?.thumbnails?.high?.url!!
            val date =
                playlistItemsResponse.items[index].snippet?.publishedAt!!
            val channelTitle = playlistItemsResponse.items[index].snippet?.videoOwnerChannelTitle?.replace(" - Topic", "")!!
            val title = stringToHtmlSign(playlistItemsResponse.items[index].snippet?.title!!)
            val videoId = playlistItemsResponse.items[index].snippet?.resourceId?.videoId!!
            val channelId = playlistItemsResponse.items[index].snippet?.channelId!!
            playlistVideoData.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date,  false))
        }
        playlistItemsRecyclerViewAdapter.submitList(playlistVideoData.toMutableList())
        binding.playlistProgressBar.visibility = View.GONE
        binding.playlistItemRecyclerView.visibility = View.VISIBLE
    }

    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }
    override fun onDestroy() {
        super.onDestroy()
        fbinding = null
    }
}