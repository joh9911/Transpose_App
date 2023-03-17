package com.myFile.Transpose.model

import com.google.gson.annotations.SerializedName

/**
 * 해당 플레이리스트의 비디오들을 저장하는 데이터 클래스
 */
data class PlayListVideoSearchData (

    @SerializedName("kind"          ) var kind          : String?          = null,
    @SerializedName("etag"          ) var etag          : String?          = null,
    @SerializedName("nextPageToken" ) var nextPageToken : String?          = null,
    @SerializedName("items"         ) var items         : ArrayList<PlayListVideoItems> = arrayListOf(),
    @SerializedName("pageInfo"      ) var pageInfo      : PlayListVideoPageInfo?        = PlayListVideoPageInfo()

)
data class PlayListVideoDefault (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListVideoMedium (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListVideoHigh (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListVideoStandard (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListVideoMaxres (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListVideoThumbnails (

    @SerializedName("default"  ) var default  : PlayListVideoDefault?  = PlayListVideoDefault(),
    @SerializedName("medium"   ) var medium   : PlayListVideoMedium?   = PlayListVideoMedium(),
    @SerializedName("high"     ) var high     : PlayListVideoHigh?     = PlayListVideoHigh(),
    @SerializedName("standard" ) var standard : PlayListVideoStandard? = PlayListVideoStandard(),
    @SerializedName("maxres"   ) var maxres   : PlayListVideoMaxres?   = PlayListVideoMaxres()

)
data class PlayListVideoResourceId (

    @SerializedName("kind"    ) var kind    : String? = null,
    @SerializedName("videoId" ) var videoId : String? = null

)
data class PlayListVideoSnippet (

    @SerializedName("publishedAt"            ) var publishedAt            : String?     = null,
    @SerializedName("channelId"              ) var channelId              : String?     = null,
    @SerializedName("title"                  ) var title                  : String?     = null,
    @SerializedName("description"            ) var description            : String?     = null,
    @SerializedName("thumbnails"             ) var thumbnails             : PlayListVideoThumbnails? = PlayListVideoThumbnails(),
    @SerializedName("channelTitle"           ) var channelTitle           : String?     = null,
    @SerializedName("playlistId"             ) var playlistId             : String?     = null,
    @SerializedName("position"               ) var position               : Int?        = null,
    @SerializedName("resourceId"             ) var resourceId             : PlayListVideoResourceId? = PlayListVideoResourceId(),
    @SerializedName("videoOwnerChannelTitle" ) var videoOwnerChannelTitle : String?     = null,
    @SerializedName("videoOwnerChannelId"    ) var videoOwnerChannelId    : String?     = null

)
data class PlayListVideoItems (

    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : String?  = null,
    @SerializedName("snippet" ) var snippet : PlayListVideoSnippet? = PlayListVideoSnippet()

)
data class PlayListVideoPageInfo (

    @SerializedName("totalResults"   ) var totalResults   : Int? = null,
    @SerializedName("resultsPerPage" ) var resultsPerPage : Int? = null

)
