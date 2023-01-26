package com.example.youtube_transpose

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.youtube_transpose.databinding.FragmentPlaylistBinding
import com.example.youtube_transpose.databinding.MainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistItemsFragment(playListData: PlayListData): Fragment() {
    private val playListData = playListData

    val API_KEY = com.example.youtube_transpose.BuildConfig.API_KEY
    lateinit var activity: Activity
    lateinit var playlistItemsRecyclerViewAdapter: PlaylistItemsRecyclerViewAdapter
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentPlaylistBinding? = null
    val binding get() = fbinding!!

    val playlistVideoData = arrayListOf<VideoData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentPlaylistBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initView()
        getData()
        return view
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
        playlistItemsRecyclerViewAdapter = PlaylistItemsRecyclerViewAdapter(playlistVideoData, -1)
        playlistItemsRecyclerViewAdapter.setItemClickListener(object: PlaylistItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                activity.supportFragmentManager.beginTransaction()
                    .replace(activity.binding.playerFragment.id,PlayerFragment(playlistVideoData, position,"playlist"),"playerFragment")
                    .commit()
            }

        })
        binding.playlistItemRecyclerView.adapter = playlistItemsRecyclerViewAdapter
    }

    fun getData(){
        val job = CoroutineScope(Dispatchers.IO).launch {
            getPlaylistItemsData(null)
        }
    }
    suspend fun getPlaylistItemsData(nextPageToken: String?){
        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java).getPlayListVideoItems(API_KEY,"snippet",playListData.playlistId,nextPageToken,"50")
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    playlistItemsMapper(response.body()!!)
                }
                if (response.body()?.nextPageToken != null)
                    getPlaylistItemsData(response.body()?.nextPageToken)
            }
        }
    }
    private fun playlistItemsMapper(playlistItemsResponse: PlayListVideoSearchData) {
        for (index in 0 until playlistItemsResponse.items.size!!) {
            val thumbnail =
                playlistItemsResponse.items[index].snippet?.thumbnails?.default?.url!!
            val date =
                playlistItemsResponse.items[index].snippet?.publishedAt!!.substring(0, 10)
            val account = playlistItemsResponse.items[index].snippet?.videoOwnerChannelTitle?.replace(" - Topic", "")!!
            val title = stringToHtmlSign(playlistItemsResponse.items[index].snippet?.title!!)
            val videoId = playlistItemsResponse.items[index].snippet?.resourceId?.videoId!!
            playlistVideoData.add(VideoData(thumbnail, title, account, videoId, date, thumbnail))
            playlistItemsRecyclerViewAdapter.notifyDataSetChanged()
            binding.playlistProgressBar.visibility = View.GONE
            binding.playlistItemRecyclerView.visibility = View.VISIBLE
        }
    }


    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }
}