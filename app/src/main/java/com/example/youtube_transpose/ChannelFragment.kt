package com.example.youtube_transpose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.youtube_transpose.databinding.FragmentChannelBinding
import com.example.youtube_transpose.databinding.FragmentPlayerBinding
import com.example.youtube_transpose.databinding.MainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class ChannelFragment(val channelData: ChannelData): Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentChannelBinding? = null
    val binding get() = fbinding!!

    val API_KEY = com.example.youtube_transpose.BuildConfig.API_KEY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentChannelBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initView()
        return view
    }
    fun initView(){
        binding.channelTitle.text = channelData.channelTitle
        Glide.with(binding.channelBanner)
            .load(channelData.channelBanner)
            .into(binding.channelBanner)
        binding.channelDescription.text = channelData.channelDescription
        binding.channelInfo.text = "동영상 ${channelData.channelVideoCount}개"

    }


//    private fun getChannelData(){
//        val retrofit = RetrofitData.initRetrofit()
//        retrofit.create(RetrofitService::class.java).getChannelData(API_KEY,"snippet",channelId)
//            .enqueue(object : Callback<ChannelSearchData>{
//                override fun onResponse(
//                    call: Call<ChannelSearchData>,
//                    response: Response<ChannelSearchData>
//                ) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onFailure(call: Call<ChannelSearchData>, t: Throwable) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//    }
}