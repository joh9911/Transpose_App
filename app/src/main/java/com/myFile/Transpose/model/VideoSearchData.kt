package com.myFile.Transpose.model

import com.google.gson.annotations.SerializedName

/**
 * video만 찾아서 저장하는 클래스
 */
data class VideoSearchData (
    @SerializedName("kind"          ) var kind          : String?          = null,
    @SerializedName("etag"          ) var etag          : String?          = null,
    @SerializedName("nextPageToken" ) var nextPageToken : String?          = null,
    @SerializedName("regionCode"    ) var regionCode    : String?          = null,
    @SerializedName("items"         ) var items         : ArrayList<VideoItems> = arrayListOf()
)
data class VideoItems (
    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : VideoId?      = VideoId(),
    @SerializedName("snippet" ) var snippet : VideoSnippet? = VideoSnippet()
)
data class VideoSnippet (
    @SerializedName("publishedAt"          ) var publishedAt          : String?     = null,
    @SerializedName("channelId"            ) var channelId            : String?     = null,
    @SerializedName("title"                ) var title                : String?     = null,
    @SerializedName("description"          ) var description          : String?     = null,
    @SerializedName("thumbnails"           ) var thumbnails           : VideoThumbnails? = VideoThumbnails(),
    @SerializedName("channelTitle"         ) var channelTitle         : String?     = null,
    @SerializedName("liveBroadcastContent" ) var liveBroadcastContent : String?     = null,
    @SerializedName("publishTime"          ) var publishTime          : String?     = null
)
data class VideoThumbnails (
    @SerializedName("default" ) var default : VideoDefault? = VideoDefault(),
    @SerializedName("medium"  ) var medium  : VideoMedium?  = VideoMedium(),
    @SerializedName("high"    ) var high    : VideoHigh?    = VideoHigh()
)
data class VideoHigh (
    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null
)
data class VideoMedium (
    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null
)
data class VideoDefault (
    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null
)
data class VideoId (
    @SerializedName("kind"    ) var kind    : String? = null,
    @SerializedName("videoId" ) var videoId : String? = null
)
