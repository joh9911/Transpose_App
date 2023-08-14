package com.myFile.transpose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * 그냥 유튜브 검색시 모든 검색 결과를 저장하는 데이터 클래스
 */
data class AllSearchData (

    @SerializedName("kind"          ) var kind          : String?          = null,
    @SerializedName("etag"          ) var etag          : String?          = null,
    @SerializedName("nextPageToken" ) var nextPageToken : String?          = null,
    @SerializedName("regionCode"    ) var regionCode    : String?          = null,
    @SerializedName("pageInfo"      ) var pageInfo      : PageInfo?        = PageInfo(),
    @SerializedName("items"         ) var items         : ArrayList<Items> = arrayListOf()

)

data class PageInfo (

    @SerializedName("totalResults"   ) var totalResults   : Int? = null,
    @SerializedName("resultsPerPage" ) var resultsPerPage : Int? = null

)

data class Id (

    @SerializedName("kind"      ) var kind      : String? = null,
    @SerializedName("channelId" ) var channelId : String? = null

)

data class Default (

    @SerializedName("url" ) var url : String? = null

)

data class Medium (

    @SerializedName("url" ) var url : String? = null

)

data class High (

    @SerializedName("url" ) var url : String? = null

)

data class Thumbnails (

    @SerializedName("default" ) var default : Default? = Default(),
    @SerializedName("medium"  ) var medium  : Medium?  = Medium(),
    @SerializedName("high"    ) var high    : High?    = High()

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

data class Items (

    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : Id?      = Id(),
    @SerializedName("snippet" ) var snippet : Snippet? = Snippet()

)
