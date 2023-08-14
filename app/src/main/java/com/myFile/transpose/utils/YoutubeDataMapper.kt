package com.myFile.transpose.utils

import android.content.Context
import com.myFile.transpose.network.dto.ChannelSearchData
import com.myFile.transpose.network.dto.CommentThreadData
import com.myFile.transpose.network.dto.PlayListSearchData
import com.myFile.transpose.network.dto.PlayListVideoSearchData
import com.myFile.transpose.R
import com.myFile.transpose.model.*
import com.myFile.transpose.model.model.*

class YoutubeDataMapper(context: Context) {
    private val youtubeDigitConverter = YoutubeDigitConverter()
    private val dateArray = context.resources.getStringArray(R.array.publish_date_formats)
    private val viewArray = context.resources.getStringArray(R.array.view_count_formats)
    private val subscriberArray = context.resources.getStringArray(R.array.subscriber_count_formats)

    fun mapVideoDataModelList(
        responseData: VideoSearchData

    ): List<VideoDataModel> {
        return responseData.items.let { items ->
            items.map { item ->
                val thumbnail = item.snippet?.thumbnails?.high?.url!!
                val rawDate = item.snippet?.publishedAt!!
                val date = youtubeDigitConverter.intervalBetweenDateText(rawDate, dateArray)
                val title = stringToHtmlSign(item.snippet?.title!!)
                val videoId = item.id?.videoId!!
                val channelId = item.snippet?.channelId!!
                val channelTitle = item.snippet?.channelTitle!!
                VideoDataModel(thumbnail, title, channelTitle, channelId, videoId, date, false)
            }
        }
    }

    fun mapPlaylistDataModelList(responseData: PlayListSearchData
    ): PlaylistDataModel {
        return responseData.items[0].let{ item ->
            val thumbnail = item.snippet?.thumbnails?.maxres?.url!!
            val title = item.snippet?.title!!
            val description = item.snippet?.description!!
            val playlistId = item.id!!
            val rawDate = item.snippet?.publishedAt ?: ""
            val date = youtubeDigitConverter.intervalBetweenDateText(rawDate, dateArray)
            val channelTitle = item.snippet?.channelTitle ?: "unKnown"
            PlaylistDataModel(thumbnail, title, description, playlistId, date, channelTitle)
        }
    }

    fun mapPlaylistDataModelsInChannelId(body: PlayListSearchData): List<PlaylistDataModel>{
        return body.items.let { items ->
            items.map { item ->
                val thumbnail = item.snippet?.thumbnails?.maxres?.url!!
                val title = item.snippet?.title!!
                val description = item.snippet?.description!!
                val playlistId = item.id!!
                val rawDate = item.snippet?.publishedAt ?: ""
                val date = youtubeDigitConverter.intervalBetweenDateText(rawDate, dateArray)
                val channelTitle = item.snippet?.channelTitle ?: "unKnown"
                PlaylistDataModel(thumbnail, title, description, playlistId, date, channelTitle)
            }
        }
    }

    fun mapVideoDataModelByVideoDetailResponse(body: VideoDetailData): VideoDataModel {
        return body.items.first().let {
            val thumbnail = it.snippet?.thumbnails?.maxres?.url!!
            val rawDate = it.snippet?.publishedAt!!
            val date = youtubeDigitConverter.intervalBetweenDateText(rawDate, dateArray)
            val title = stringToHtmlSign(it.snippet?.title!!)
            val videoId = it.id!!
            val channelId = it.snippet?.channelId!!
            val channelTitle = it.snippet?.channelTitle!!
            VideoDataModel(thumbnail, title, channelTitle, channelId, videoId, date, false)
        }
    }

    fun mapPlaylistItemsDataModelList(
        body: PlayListVideoSearchData
    ): List<VideoDataModel> {
        return body.items.let { items ->
            items.map { item ->
                val thumbnail = item.snippet?.thumbnails?.high?.url ?: ""
                val rawDate = item.snippet?.publishedAt ?: ""
                val date = youtubeDigitConverter.intervalBetweenDateText(rawDate, dateArray)
                val channelTitle =
                    item.snippet?.videoOwnerChannelTitle?.replace(" - Topic", "") ?: ""
                val title = stringToHtmlSign(item.snippet?.title ?: "")
                val videoId = item.snippet?.resourceId?.videoId ?: ""
                val channelId = item.snippet?.channelId ?: ""
                VideoDataModel(thumbnail, title, channelTitle, channelId, videoId, date, false)
            }
        }
    }


    fun mapVideoDetailDataModel(
        videoDetailData: VideoDetailData
    ): VideoDetailDataModel {
        return videoDetailData.items[0].let { video ->
            val title = video.snippet?.title ?: ""
            val viewCount = youtubeDigitConverter.viewCountCalculator(
                viewArray,
                video.statistics?.viewCount ?: "0"
            )
            val publishDate = youtubeDigitConverter.intervalBetweenDateText(
                video.snippet?.publishedAt ?: "2023-07-26T12:00:00.000Z", dateArray
            )
            val channelId = video.snippet?.channelId!!
            VideoDetailDataModel(title, viewCount, publishDate, channelId)
        }
    }

    fun mapChannelDataModel(
        channelDetailData: ChannelSearchData
    ): ChannelDataModel {
        return channelDetailData.items[0].let { item ->
            val channelThumbnail = item.snippet?.thumbnails?.default?.url ?: ""
            val videoCount = item.statistics?.videoCount ?: ""
            val subscriberCount = youtubeDigitConverter.subscriberCountConverter(
                item.statistics?.subscriberCount ?: "0", subscriberArray
            )
            val viewCount = youtubeDigitConverter.viewCountCalculator(
                viewArray,
                item.statistics?.viewCount ?: "0"
            )
            val channelBanner = item.brandingSettings?.image?.bannerExternalUrl ?: ""
            val channelTitle = item.snippet?.title ?: ""
            val channelDescription = item.snippet?.description ?: ""
            val channelPlaylistId = item.contentDetails?.relatedPlaylists?.uploads ?: ""
            ChannelDataModel(
                channelTitle,
                channelDescription,
                channelBanner,
                channelThumbnail,
                videoCount,
                viewCount,
                subscriberCount,
                channelPlaylistId
            )
        }
    }

    fun mapCommentThreadData(commentThreadData: CommentThreadData): List<CommentDataModel>{
        return commentThreadData.items.map{
            val authorName = it.snippet?.topLevelComment?.snippet?.authorDisplayName ?: ""
            val authorImage = it.snippet?.topLevelComment?.snippet?.authorProfileImageUrl ?: ""
            val commentTime = youtubeDigitConverter.intervalBetweenDateText(it.snippet?.topLevelComment?.snippet?.publishedAt ?: "", dateArray)
            val commentText = it.snippet?.topLevelComment?.snippet?.textDisplay ?: ""
            CommentDataModel(authorName, authorImage, commentTime, commentText)
        }
    }

    //영상 제목을 받아올때 &quot; &#39; 문자가 그대로 출력되기 때문에 다른 문자로 대체 해주기 위해 사용하는 메서드
    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }
}