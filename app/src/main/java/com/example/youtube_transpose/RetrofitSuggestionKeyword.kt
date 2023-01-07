package com.example.youtube_transpose

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitSuggestionKeyword {
    fun initRetrofit(): Retrofit {
        if(RetrofitVideo.instance == null) { // 이 객체를 또 다른 클래스에서 쓰면 안되기 때문에 이렇게 narrowing을 해줌줌
            RetrofitVideo.instance = Retrofit
                .Builder()
                .baseUrl("https://suggestqueries.google.com/complete/")//ip나 도메인: 포트번호
                .addConverterFactory(GsonConverterFactory.create()) //Gson을 쓰겠다.
                .build()
        }
        return RetrofitVideo.instance!!
    }
}