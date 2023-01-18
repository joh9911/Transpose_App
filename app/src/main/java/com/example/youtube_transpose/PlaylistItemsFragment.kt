package com.example.youtube_transpose

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.youtube_transpose.databinding.FragmentPlayerBinding
import com.example.youtube_transpose.databinding.FragmentPlaylistBinding
import com.example.youtube_transpose.databinding.FragmentSearchResultBinding
import com.example.youtube_transpose.databinding.MainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        getPlaylistItems(null)
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

    fun getPlaylistItems(nextPageToken: String?){
        val retrofit = RetrofitVideo.initRetrofit()
        retrofit.create(RetrofitService::class.java).getPlayListVideoItems(API_KEY,"snippet",playListData.playlistId,nextPageToken,"50")
            .enqueue(object : Callback<PlayListVideoSearchData> {
                override fun onResponse(call: Call<PlayListVideoSearchData>, response: Response<PlayListVideoSearchData>) {
                    Log.d(ContentValues.TAG, "onSusses하하${response.body()?.pageInfo?.resultsPerPage}: ${response.body()?.items?.size}")
                    for (index in 0 until response.body()?.items?.size!!){
                        val thumbnail = response?.body()?.items?.get(index)?.snippet?.thumbnails?.default?.url!!
                        val date = response?.body()?.items?.get(index)?.snippet?.publishedAt!!.substring(0, 10)
                        val account = response.body()?.items?.get(index)?.snippet?.videoOwnerChannelTitle?.replace(" - Topic","")!!
                        val title = stringToHtmlSign(response?.body()?.items?.get(index)?.snippet?.title!!)
                        val videoId = response?.body()?.items?.get(index)?.snippet?.resourceId?.videoId!!
                        playlistVideoData.add(VideoData(thumbnail, title, account, videoId, date, false))
                    }
                    playlistItemsRecyclerViewAdapter.notifyDataSetChanged()
                    binding.playlistProgressBar.visibility = View.GONE
                    binding.playlistItemRecyclerView.visibility = View.VISIBLE
//                    if (response.body()?.nextPageToken != null)      유튜브 뮤직 페이지여서 아직 못가져옴
//                        getPlaylistItems(response.body()?.nextPageToken)
                }
                override fun onFailure(call: Call<PlayListVideoSearchData>, t: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: ${t.message}")
                }
            })
    }
    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }
}