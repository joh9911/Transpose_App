package com.myFile.Transpose

import com.google.gson.annotations.SerializedName
import com.myFile.Transpose.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("search")
    suspend fun getVideoSearchResult(
                        @Query("part") part: String,
                        @Query("q") q: String,
                        @Query("maxResults") maxResults: String,
                        @Query("type") type: String,
                        @Query("pageToken") pageToken: String?
    ): Response<VideoSearchData>

    @GET("search")
    suspend fun getAllSearchData(@Query("key") key: String,
                         @Query("part") part: String,
                         @Query("q") q: String,
                         @Query("maxResults") maxResults: String

    ): Response<AllSearchData>

    @GET("search")
    fun getSuggestionKeyword(@Query("client") client: String,
                             @Query("ds") ds: String,
                             @Query("q") q: String
    ): Call<ResponseBody>

    @GET("search")
    suspend fun getRelatedVideo(@Query("part") part: String,
                        @Query("relatedToVideoId") relatedToVideoId: String,
                        @Query("type") type: String,
                        @Query("maxResults") maxResults: String
    ): Response<RelatedVideoData>

    @GET("playlistItems")
    suspend fun getPlayListVideoItems(@Query("key") key: String,
                         @Query("part") part: String,
                         @Query("playlistId") playListId: String,
                         @Query("pageToken") pageToken: String?,
                         @Query("maxResults") maxResults: String

    ): Response<PlayListVideoSearchData>

    @GET("playlists")
    suspend fun getPlayLists(@Query("key") key: String,
                     @Query("part") part: String,
                     @Query("id") id: String, // id는 각 플레이리스트의 아이디
                     @Query("maxResults") maxResults: String
    ): Response<PlayListSearchData>
//    @Query("key") key: String,
    @GET("channels")
    suspend fun getChannelData(
                        @Query("key") key: String,
                       @Query("part") part: String,
                       @Query("id") id: String?
    ): Response<ChannelSearchData>

    @GET("videos")
    suspend fun getVideoDetail(
        @Query("key") key: String,
        @Query("part") part: String,
        @Query("id") id: String

    ): Response<VideoDetailData>
}


data class VideoData(
    val thumbnail: String,
    val title: String,
    val channelTitle: String,
    val channelId: String,
    val videoId: String,
    val date: String,
    val isPlaying: Boolean
)

data class PlayListData(
    val thumbnail: String,
    val title: String,
    val description: String,
    val playlistId: String
)

data class ChannelData(
    val channelTitle: String,
    val channelDescription: String,
    val channelBanner: String?,
    val channelThumbnail: String?,
    val channelVideoCount: String,
    val channelViewCount: String,
    val channelSubscriberCount: String,
    val channelPlaylistId: String
)
