package com.myFile.transpose.dto

import com.google.gson.annotations.SerializedName

/**
 * 댓글 스레드 정보
 */
data class CommentThreadData (

    @SerializedName("kind"          ) var kind          : String?          = null,
    @SerializedName("etag"          ) var etag          : String?          = null,
    @SerializedName("nextPageToken" ) var nextPageToken : String?          = null,
    @SerializedName("pageInfo"      ) var pageInfo      : CommendThreadDataPageInfo?        = CommendThreadDataPageInfo(),
    @SerializedName("items"         ) var items         : ArrayList<CommendThreadDataItems> = arrayListOf()

)
data class CommendThreadDataPageInfo (

    @SerializedName("totalResults"   ) var totalResults   : Int? = null,
    @SerializedName("resultsPerPage" ) var resultsPerPage : Int? = null

)
data class CommendThreadDataSnippetDownLevel (

    @SerializedName("videoId"               ) var videoId               : String?          = null,
    @SerializedName("textDisplay"           ) var textDisplay           : String?          = null,
    @SerializedName("textOriginal"          ) var textOriginal          : String?          = null,
    @SerializedName("authorDisplayName"     ) var authorDisplayName     : String?          = null,
    @SerializedName("authorProfileImageUrl" ) var authorProfileImageUrl : String?          = null,
    @SerializedName("authorChannelUrl"      ) var authorChannelUrl      : String?          = null,
    @SerializedName("authorChannelId"       ) var authorChannelId       : AuthorChannelId? = AuthorChannelId(),
    @SerializedName("canRate"               ) var canRate               : Boolean?         = null,
    @SerializedName("viewerRating"          ) var viewerRating          : String?          = null,
    @SerializedName("likeCount"             ) var likeCount             : Int?             = null,
    @SerializedName("publishedAt"           ) var publishedAt           : String?          = null,
    @SerializedName("updatedAt"             ) var updatedAt             : String?          = null

)
data class CommendThreadDataTopLevelComment (

    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : String?  = null,
    @SerializedName("snippet" ) var snippet : CommendThreadDataSnippetDownLevel? = CommendThreadDataSnippetDownLevel()

)
data class CommendThreadDataSnippet (

    @SerializedName("videoId"         ) var videoId         : String?          = null,
    @SerializedName("topLevelComment" ) var topLevelComment : CommendThreadDataTopLevelComment? = CommendThreadDataTopLevelComment(),
    @SerializedName("canReply"        ) var canReply        : Boolean?         = null,
    @SerializedName("totalReplyCount" ) var totalReplyCount : Int?             = null,
    @SerializedName("isPublic"        ) var isPublic        : Boolean?         = null

)
data class CommendThreadDataItems (

    @SerializedName("kind"    ) var kind    : String?  = null,
    @SerializedName("etag"    ) var etag    : String?  = null,
    @SerializedName("id"      ) var id      : String?  = null,
    @SerializedName("snippet" ) var snippet : CommendThreadDataSnippet? = CommendThreadDataSnippet()

)
data class AuthorChannelId (

    @SerializedName("value" ) var value : String? = null

)