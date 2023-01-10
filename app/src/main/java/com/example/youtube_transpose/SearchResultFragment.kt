package com.example.youtube_transpose

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youtube_transpose.databinding.FragmentPlayerBinding
import com.example.youtube_transpose.databinding.FragmentSearchResultBinding
import com.example.youtube_transpose.databinding.MainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultFragment(search: String): Fragment() {
    lateinit var mainBinding: MainBinding
    lateinit var activity: Activity
    lateinit var searchResultAdapter: SearchResultFragmentRecyclerViewAdapter
    private lateinit var callback: OnBackPressedCallback
    var fbinding: FragmentSearchResultBinding? = null
    val binding get() = fbinding!!

    val search = search
    val API_KEY = "AIzaSyBZlnQ_kRZ7mvs0wL31ezbBeEPYAoIM3EM"
    val videoData = ArrayList<VideoData>()

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
        getHomeVideo()
        return view
    }

    fun initRecyclerView(){
        activity = context as Activity
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        searchResultAdapter = SearchResultFragmentRecyclerViewAdapter(videoData)
        binding.recyclerView.adapter = searchResultAdapter
        searchResultAdapter.setItemClickListener(object: SearchResultFragmentRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                activity.replaceFragmentToPlayerFragment(videoData[position])
            }
        })
    }

    private fun getHomeVideo() {
        val retrofit = RetrofitVideo.initRetrofit()
        retrofit.create(RetrofitService::class.java).getVideoDetails(API_KEY,"snippet",search,"50","video")
            .enqueue(object : Callback<VideoSearchData> {
                override fun onResponse(call: Call<VideoSearchData>, response: Response<VideoSearchData>) {
                    videoData.clear()
                    for (index in 0 until response.body()?.items?.size!!){
                        val thumbnail = response?.body()?.items?.get(index)?.snippet?.thumbnails?.default?.url!!
                        val date = response?.body()?.items?.get(index)?.snippet?.publishedAt!!.substring(0, 10)
                        val account = response?.body()?.items?.get(index)?.snippet?.channelTitle!!
                        val title = stringToHtmlSign(response?.body()?.items?.get(index)?.snippet?.title!!)
                        val videoId = response?.body()?.items?.get(index)?.id?.videoId!!
                        videoData.add(VideoData(thumbnail, title, account, videoId, date))
                    }
                    Log.d("fragment 비디오 목록","$videoData")

                    searchResultAdapter.notifyDataSetChanged()

                }
                override fun onFailure(call: Call<VideoSearchData>, t: Throwable) {
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

    override fun onDetach() {
        Log.d("searchResultFragment","종료")
        super.onDetach()
    }

}