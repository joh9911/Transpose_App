package com.myFile.transpose.network.retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitYT {
    var instance: Retrofit? = null
    var gson= GsonBuilder().setLenient().create()
    private const val BASE_URL = "https://yt.lemnoslife.com/noKey/"
    fun initRetrofit(): Retrofit {
        if(instance == null) { // 이 객체를 또 다른 클래스에서 쓰면 안되기 때문에 이렇게 narrowing을 해줌줌
            instance = Retrofit
                .Builder()
                .baseUrl(BASE_URL)//ip나 도메인: 포트번호
                .addConverterFactory(GsonConverterFactory.create(gson)) //Gson을 쓰겠다.
                .build()
        }
        return instance!!
    }
}