package com.myFile.transpose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * 플레이리스트 정보를 저장하는 데이터 클래스
 */
data class PlayListSearchData (

    @SerializedName("kind"     ) var kind     : String?          = null,
    @SerializedName("etag"     ) var etag     : String?          = null,
    @SerializedName("nextPageToken" ) var nextPageToken : String?          = null,
    @SerializedName("pageInfo" ) var pageInfo : PlayListPageInfo?        = PlayListPageInfo(),
    @SerializedName("items"    ) var items    : ArrayList<PlayListItems> = arrayListOf()

)
data class PlayListPageInfo (

    @SerializedName("totalResults"   ) var totalResults   : Int? = null,
    @SerializedName("resultsPerPage" ) var resultsPerPage : Int? = null

)
data class PlayListMedium (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListStandard (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListMaxres (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class PlayListThumbnails (

    @SerializedName("medium"   ) var medium   : PlayListMedium?   = PlayListMedium(),
    @SerializedName("standard" ) var standard : PlayListStandard? = PlayListStandard(),
    @SerializedName("maxres"   ) var maxres   : PlayListMaxres?   = PlayListMaxres()

)
data class PlayListLocalized (

    @SerializedName("title"       ) var title       : String? = null,
    @SerializedName("description" ) var description : String? = null

)
data class PlayListSnippet (

    @SerializedName("publishedAt"     ) var publishedAt     : String?     = null,
    @SerializedName("channelId"       ) var channelId       : String?     = null,
    @SerializedName("title"           ) var title           : String?     = null,
    @SerializedName("description"     ) var description     : String?     = null,
    @SerializedName("thumbnails"      ) var thumbnails      : PlayListThumbnails? = PlayListThumbnails(),
    @SerializedName("channelTitle"    ) var channelTitle    : String?     = null,
    @SerializedName("defaultLanguage" ) var defaultLanguage : String?     = null,
    @SerializedName("localized"       ) var localized       : PlayListLocalized?  = PlayListLocalized()

)
data class PlayListItems (

    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : String?  = null,
    @SerializedName("snippet" ) var snippet : PlayListSnippet? = PlayListSnippet()

)