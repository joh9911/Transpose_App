package com.myFile.transpose

import android.app.Application
import com.myFile.transpose.database.AppDatabase
import com.myFile.transpose.model.repository.*
import com.myFile.transpose.network.retrofit.RetrofitData
import com.myFile.transpose.network.retrofit.RetrofitService
import com.myFile.transpose.network.retrofit.RetrofitSuggestionKeyword

class MyApplication: Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val retrofitSuggestionKeyword: RetrofitService by lazy { RetrofitSuggestionKeyword.initRetrofit().create(
        RetrofitService::class.java)}
    private val retrofitData: RetrofitService by lazy { RetrofitData.initRetrofit().create(
        RetrofitService::class.java) }

    val youtubeDataRepository by lazy { YoutubeDataRepository(retrofitData)}
    val myPlaylistRepository by lazy { MyPlaylistRepository(database.myPlaylistDao()) }
    val suggestionKeywordRepository by lazy { SuggestionKeywordRepository(retrofitSuggestionKeyword)}
    val youtubeCashedRepository by lazy { YoutubeCashedDataRepository(database.youtubeCashedDataDao())}

}