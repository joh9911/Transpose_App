package com.myFile.transpose

import android.util.Log
import com.myFile.transpose.dto.ChannelSearchData
import com.myFile.transpose.dto.CommentThreadData
import com.myFile.transpose.dto.PlayListSearchData
import com.myFile.transpose.dto.PlayListVideoSearchData
import com.myFile.transpose.model.*

class YoutubeDataMapper {
    private val youtubeDigitConverter = YoutubeDigitConverter()

    fun mapVideoDataModelList(
        responseData: VideoSearchData,
        dateArray: Array<String>
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

    fun mapPlaylistDataModelList(responseData: PlayListSearchData,
    dateArray: Array<String>): PlaylistDataModel{
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

    fun mapPlaylistDataModelsInChannelId(body: PlayListSearchData, dateArray: Array<String>): List<PlaylistDataModel>{
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

    fun mapVideoDataModelByVideoDetailResponse(body: VideoDetailData, dateArray: Array<String>): VideoDataModel{
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
        body: PlayListVideoSearchData,
        dateArray: Array<String>
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
        videoDetailData: VideoDetailData,
        dateArray: Array<String>,
        viewArray: Array<String>
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
        channelDetailData: ChannelSearchData,
        dateArray: Array<String>,
        viewArray: Array<String>,
        subscriberArray: Array<String>
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

    fun mapCommentThreadData(commentThreadData: CommentThreadData, dateArray: Array<String>): List<CommentDataModel>{
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