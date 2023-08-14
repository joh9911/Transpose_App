package com.myFile.transpose.model

import com.google.gson.annotations.SerializedName

data class YTVideoData (

    @SerializedName("kind"          ) var kind          : String?          = null,
    @SerializedName("etag"          ) var etag          : String?          = null,
    @SerializedName("nextPageToken" ) var nextPageToken : String?          = null,
    @SerializedName("items"         ) var items         : ArrayList<YTVideoItems> = arrayListOf()

)

data class YTVideoId (

    @SerializedName("kind"    ) var kind    : String? = null,
    @SerializedName("videoId" ) var videoId : String? = null

)

data class YTVideoThumbnails (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)


data class YTVideoChannelThumbnails (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)

data class YTVideoDetailedMetadataSnippet (

    @SerializedName("text" ) var text : String? = null

)

data class YTVideoSnippet (

    @SerializedName("channelId"               ) var channelId               : String?                            = null,
    @SerializedName("title"                   ) var title                   : String?                            = null,
    @SerializedName("thumbnails"              ) var thumbnails              : ArrayList<YTVideoThumbnails>              = arrayListOf(),
    @SerializedName("channelTitle"            ) var channelTitle            : String?                            = null,
    @SerializedName("channelHandle"           ) var channelHandle           : String?                            = null,
    @SerializedName("timestamp"               ) var timestamp               : String?                            = null,
    @SerializedName("duration"                ) var duration                : Int?                               = null,
    @SerializedName("views"                   ) var views                   : Int?                               = null,
    @SerializedName("badges"                  ) var badges                  : ArrayList<String>                  = arrayListOf(),
    @SerializedName("channelApproval"         ) var channelApproval         : String?                            = null,
    @SerializedName("channelThumbnails"       ) var channelThumbnails       : ArrayList<YTVideoChannelThumbnails>       = arrayListOf(),
    @SerializedName("detailedMetadataSnippet" ) var detailedMetadataSnippet : ArrayList<YTVideoDetailedMetadataSnippet> = arrayListOf()

)

data class YTVideoItems (
    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : YTVideoId?      = YTVideoId(),
    @SerializedName("snippet" ) var snippet : YTVideoSnippet? = YTVideoSnippet()

)