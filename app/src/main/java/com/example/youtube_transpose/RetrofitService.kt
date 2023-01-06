package com.example.youtube_transpose

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("search")
    fun getVideoDetails(@Query("key") key: String,
                        @Query("part") part: String,
                        @Query("q") q: String,
                        @Query("maxResults") maxResult: String,
                        @Query("type") type: String
    ): Call<YoutubeSearchData>

    @GET("search")
    fun getSuggestionKeyword(@Query("client") client: String,
                             @Query("ds") ds: String,
                             @Query("q") q: String
    ): Call<ResponseBody>
}

data class A(
    val cookie: String
)

data class YoutubeSearchData (

    @SerializedName("kind"          ) var kind          : String?          = null,
    @SerializedName("etag"          ) var etag          : String?          = null,
    @SerializedName("nextPageToken" ) var nextPageToken : String?          = null,
    @SerializedName("regionCode"    ) var regionCode    : String?          = null,
    @SerializedName("items"         ) var items         : ArrayList<Items> = arrayListOf()

)

data class Items (

    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : Id?      = Id(),
    @SerializedName("snippet" ) var snippet : Snippet? = Snippet()

)
data class Snippet (

    @SerializedName("publishedAt"          ) var publishedAt          : String?     = null,
    @SerializedName("channelId"            ) var channelId            : String?     = null,
    @SerializedName("title"                ) var title                : String?     = null,
    @SerializedName("description"          ) var description          : String?     = null,
    @SerializedName("thumbnails"           ) var thumbnails           : Thumbnails? = Thumbnails(),
    @SerializedName("channelTitle"         ) var channelTitle         : String?     = null,
    @SerializedName("liveBroadcastContent" ) var liveBroadcastContent : String?     = null,
    @SerializedName("publishTime"          ) var publishTime          : String?     = null

)
data class Thumbnails (

    @SerializedName("default" ) var default : Default? = Default(),
    @SerializedName("medium"  ) var medium  : Medium?  = Medium(),
    @SerializedName("high"    ) var high    : High?    = High()

)
data class High (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class Medium (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class Default (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class Id (

    @SerializedName("kind"    ) var kind    : String? = null,
    @SerializedName("videoId" ) var videoId : String? = null

)

data class VideoData(
    val thumbnail: String,
    val title: String,
    val account: String,
    val videoId: String,
    val date: String,
)

//var vodid = if (kind == "youtube#video") {
//    c.getJSONObject("id").getString("videoId") // 유튜브
//} else {
//    c.getJSONObject("id").getString("playlistId") // 유튜브
//}
//val title = c.getJSONObject("snippet").getString("title") //유튜브 제목을 받아옵니다
//val ranking = (index + 1).toString()
//val changString: String = stringToHtmlSign(title)
//val date = c.getJSONObject("snippet").getString("publishedAt") //등록날짜
//    .substring(0, 10)
//val account = c.getJSONObject("snippet").getString("channelTitle")
//val extraInfo = c.getJSONObject("snippet").getString("channelTitle")
//val thumbnail = c.getJSONObject("snippet").getJSONObject("thumbnails")
//    .getJSONObject("default").getString("url") //썸네일 이미지 URL값