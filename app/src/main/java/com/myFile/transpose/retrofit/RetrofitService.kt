package com.myFile.transpose.retrofit

import android.os.Parcelable
import com.myFile.transpose.dto.*
import com.myFile.transpose.model.*
import kotlinx.parcelize.Parcelize
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("search")
    suspend fun getVideoSearchResult(@Query("key") key: String,
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
    suspend fun getRelatedVideo(@Query("key") key: String,
                        @Query("part") part: String,
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

    @GET("playlists")
    suspend fun getPlayListsInChannel(@Query("key") key: String,
                             @Query("part") part: String,
                             @Query("channelId") id: String, // id는 각 플레이리스트의 아이디
                             @Query("maxResults") maxResults: String,
                            @Query("pageToken") pageToken: String?
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

    @GET("commentThreads")
    suspend fun getCommentThreads(
        @Query("key") key: String,
        @Query("part") part: String,
        @Query("videoId") videoId: String,
        @Query("maxResults") maxResults: String,
        @Query("order") order: String,
        @Query("pageToken") pageToken: String?,
        @Query("textFormat") textFormat: String
    ): Response<CommentThreadData>

}

data class CommentData(
    val authorName: String,
    val authorImage: String,
    val commentTime: String,
    val commentText: String
)

@Parcelize
data class VideoData(
    val thumbnail: String,
    val title: String,
    val channelTitle: String,
    val channelId: String,
    val videoId: String,
    val date: String,
    val isPlaying: Boolean
): Parcelable

@Parcelize
data class PlayListData(
    val thumbnail: String,
    val title: String,
    val description: String,
    val playlistId: String
): Parcelable

@Parcelize
data class ChannelData(
    val channelTitle: String,
    val channelDescription: String,
    val channelBanner: String?,
    val channelThumbnail: String?,
    val channelVideoCount: String,
    val channelViewCount: String,
    val channelSubscriberCount: String,
    val channelPlaylistId: String
): Parcelable
