package com.myFile.transpose.repository

import com.myFile.transpose.retrofit.RetrofitService
import okhttp3.ResponseBody
import retrofit2.Response

class SuggestionKeywordRepository(private val retrofitService: RetrofitService) {

    suspend fun getSuggestionKeyword(newText: String): Response<ResponseBody> {
        return retrofitService.getSuggestionKeyword("firefox", "yt", newText)
    }

}