package com.myFile.transpose.dto

import com.google.gson.annotations.SerializedName

data class RelatedVideoData (

    @SerializedName("kind"  ) var kind  : String?          = null,
    @SerializedName("etag"  ) var etag  : String?          = null,
    @SerializedName("items" ) var RelatedVideoDataitems : ArrayList<RelatedVideoDataItems> = arrayListOf()

)
data class RelatedVideoDataId (

    @SerializedName("kind"    ) var kind    : String? = null,
    @SerializedName("videoId" ) var videoId : String? = null

)
data class RelatedVideoDataDefault (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class RelatedVideoDataMedium (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class RelatedVideoDataHigh (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class RelatedVideoDataStandard (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class RelatedVideoDataMaxres (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class RelatedVideoDataThumbnails (

    @SerializedName("default"  ) var default  : RelatedVideoDataDefault?  = RelatedVideoDataDefault(),
    @SerializedName("medium"   ) var medium   : RelatedVideoDataMedium?   = RelatedVideoDataMedium(),
    @SerializedName("high"     ) var high     : RelatedVideoDataHigh?     = RelatedVideoDataHigh(),
    @SerializedName("standard" ) var standard : RelatedVideoDataStandard? = RelatedVideoDataStandard(),
    @SerializedName("maxres"   ) var maxres   : RelatedVideoDataMaxres?   = RelatedVideoDataMaxres()

)
data class RelatedVideoDataSnippet (

    @SerializedName("publishedAt"          ) var publishedAt          : String?     = null,
    @SerializedName("channelId"            ) var channelId            : String?     = null,
    @SerializedName("title"                ) var title                : String?     = null,
    @SerializedName("description"          ) var description          : String?     = null,
    @SerializedName("thumbnails"           ) var thumbnails           : RelatedVideoDataThumbnails? = RelatedVideoDataThumbnails(),
    @SerializedName("channelTitle"         ) var channelTitle         : String?     = null,
    @SerializedName("liveBroadcastContent" ) var liveBroadcastContent : String?     = null,
    @SerializedName("publishTime"          ) var publishTime          : String?     = null

)
data class RelatedVideoDataItems (

    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : RelatedVideoDataId?      = RelatedVideoDataId(),
    @SerializedName("snippet" ) var snippet : RelatedVideoDataSnippet? = RelatedVideoDataSnippet()

)