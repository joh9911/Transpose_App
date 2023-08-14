package com.myFile.transpose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * 채널 정보
 */
data class ChannelSearchData (

    @SerializedName("kind"     ) var kind     : String?          = null,
    @SerializedName("etag"     ) var etag     : String?          = null,
    @SerializedName("pageInfo" ) var pageInfo : ChannelPageInfo? = ChannelPageInfo(),
    @SerializedName("items"    ) var items    : ArrayList<ChannelItems> = arrayListOf()

)

data class ChannelPageInfo (

    @SerializedName("totalResults"   ) var totalResults   : Int? = null,
    @SerializedName("resultsPerPage" ) var resultsPerPage : Int? = null

)

data class ChannelDefault (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)

data class ChannelMedium (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)
data class ChannelHigh (

    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : Int?    = null,
    @SerializedName("height" ) var height : Int?    = null

)

data class ChannelThumbnails (

    @SerializedName("default" ) var default : ChannelDefault? = ChannelDefault(),
    @SerializedName("medium"  ) var medium  : ChannelMedium?  = ChannelMedium(),
    @SerializedName("high"    ) var high    : ChannelHigh?    = ChannelHigh()

)

data class ChannelLocalized (

    @SerializedName("title"       ) var title       : String? = null,
    @SerializedName("description" ) var description : String? = null

)

data class ChannelSnippet (

    @SerializedName("title"           ) var title           : String?     = null,
    @SerializedName("description"     ) var description     : String?     = null,
    @SerializedName("customUrl"       ) var customUrl       : String?     = null,
    @SerializedName("publishedAt"     ) var publishedAt     : String?     = null,
    @SerializedName("thumbnails"      ) var thumbnails      : ChannelThumbnails? = ChannelThumbnails(),
    @SerializedName("defaultLanguage" ) var defaultLanguage : String?     = null,
    @SerializedName("localized"       ) var localized       : ChannelLocalized?  = ChannelLocalized()

)

data class ChannelRelatedPlaylists (

    @SerializedName("likes"   ) var likes   : String? = null,
    @SerializedName("uploads" ) var uploads : String? = null

)

data class ChannelContentDetails (

    @SerializedName("relatedPlaylists" ) var relatedPlaylists : ChannelRelatedPlaylists? = ChannelRelatedPlaylists()

)

data class ChannelStatistics (

    @SerializedName("viewCount"             ) var viewCount             : String?  = null,
    @SerializedName("subscriberCount"       ) var subscriberCount       : String?  = null,
    @SerializedName("hiddenSubscriberCount" ) var hiddenSubscriberCount : Boolean? = null,
    @SerializedName("videoCount"            ) var videoCount            : String?  = null

)

data class ChannelChannel (

    @SerializedName("title"       ) var title       : String? = null,
    @SerializedName("description" ) var description : String? = null,
    @SerializedName("keywords"    ) var keywords    : String? = null

)

data class ChannelImage (

    @SerializedName("bannerExternalUrl" ) var bannerExternalUrl : String? = null

)

data class ChannelBrandingSettings (

    @SerializedName("channel" ) var channel : ChannelChannel? = ChannelChannel(),
    @SerializedName("image"   ) var image   : ChannelImage?   = ChannelImage()

)

data class ChannelItems (

    @SerializedName("kind"           ) var kind           : String?         = null,
    @SerializedName("etag"           ) var etag           : String?         = null,
    @SerializedName("id"             ) var id             : String?         = null,
    @SerializedName("snippet"        ) var snippet        : ChannelSnippet?        = ChannelSnippet(),
    @SerializedName("contentDetails" ) var contentDetails : ChannelContentDetails? = ChannelContentDetails(),
    @SerializedName("statistics"     ) var statistics     : ChannelStatistics?     = ChannelStatistics(),
    @SerializedName("brandingSettings" ) var brandingSettings : ChannelBrandingSettings? = ChannelBrandingSettings()
)