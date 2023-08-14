package com.myFile.transpose.model

import com.google.gson.annotations.SerializedName

data class VideoDetailData (

    @SerializedName("kind"     ) var kind     : String?          = null,
    @SerializedName("etag"     ) var etag     : String?          = null,
    @SerializedName("items"    ) var items    : ArrayList<VideoDetailItems> = arrayListOf(),
    @SerializedName("pageInfo" ) var pageInfo : VideoDetailPageInfo?        = VideoDetailPageInfo()

)
data class VideoDetailThumbnails (

    @SerializedName("default"  ) var default  : VideoDetailDefault?  = VideoDetailDefault(),
    @SerializedName("medium"   ) var medium   : VideoDetailMedium?   = VideoDetailMedium(),
    @SerializedName("high"     ) var high     : VideoDetailHigh?     = VideoDetailHigh(),
    @SerializedName("standard" ) var standard : VideoDetailStandard? = VideoDetailStandard(),
    @SerializedName("maxres"   ) var maxres   : VideoDetailMaxres?   = VideoDetailMaxres()

)
data class VideoDetailMaxres (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class VideoDetailStandard (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class VideoDetailHigh (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class VideoDetailMedium (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class VideoDetailDefault (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class VideoDetailSnippet (

    @SerializedName("publishedAt"          ) var publishedAt          : String?           = null,
    @SerializedName("channelId"            ) var channelId            : String?           = null,
    @SerializedName("title"                ) var title                : String?           = null,
    @SerializedName("description"          ) var description          : String?           = null,
    @SerializedName("thumbnails"           ) var thumbnails           : VideoDetailThumbnails?       = VideoDetailThumbnails(),
    @SerializedName("channelTitle"         ) var channelTitle         : String?           = null,
    @SerializedName("tags"                 ) var tags                 : ArrayList<String> = arrayListOf(),
    @SerializedName("categoryId"           ) var categoryId           : String?           = null,
    @SerializedName("liveBroadcastContent" ) var liveBroadcastContent : String?           = null,
    @SerializedName("defaultLanguage"      ) var defaultLanguage      : String?           = null,
    @SerializedName("localized"            ) var localized            : VideoDetailLocalized?        = VideoDetailLocalized(),
    @SerializedName("defaultAudioLanguage" ) var defaultAudioLanguage : String?           = null

)
data class VideoDetailLocalized (

    @SerializedName("title"       ) var title       : String? = null,
    @SerializedName("description" ) var description : String? = null

)
data class VideoDetailStatistics (

    @SerializedName("viewCount"     ) var viewCount     : String? = null,
    @SerializedName("likeCount"     ) var likeCount     : String? = null,
    @SerializedName("favoriteCount" ) var favoriteCount : String? = null,
    @SerializedName("commentCount"  ) var commentCount  : String? = null

)
data class VideoDetailItems (

    @SerializedName("kind"       ) var kind       : String?     = null,
    @SerializedName("etag"       ) var etag       : String?     = null,
    @SerializedName("id"         ) var id         : String?     = null,
    @SerializedName("snippet"    ) var snippet    : VideoDetailSnippet?    = VideoDetailSnippet(),
    @SerializedName("statistics" ) var statistics : VideoDetailStatistics? = VideoDetailStatistics()

)
data class VideoDetailPageInfo (

    @SerializedName("totalResults"   ) var totalResults   : Int? = null,
    @SerializedName("resultsPerPage" ) var resultsPerPage : Int? = null

)