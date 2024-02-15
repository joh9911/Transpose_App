package com.myFile.transpose.data.repository

import com.myFile.transpose.BuildConfig
import com.myFile.transpose.network.dto.ChannelSearchData
import com.myFile.transpose.network.dto.CommentThreadData
import com.myFile.transpose.network.dto.PlayListSearchData
import com.myFile.transpose.network.dto.PlayListVideoSearchData
import com.myFile.transpose.data.model.PlaylistDataModel
import com.myFile.transpose.data.VideoDetailData
import com.myFile.transpose.data.VideoSearchData
import com.myFile.transpose.network.retrofit.RetrofitService
import retrofit2.Response
import java.util.*

class YoutubeDataRepository(
    private val retrofitService: RetrofitService,
) {
    private val musicCategoryRepository = MusicCategoryRepository()



    suspend fun fetchNationalPlaylists(musicId :String): Response<PlayListSearchData> {
        val keyList = listOf(BuildConfig.API_KEY, BuildConfig.API_KEY2, BuildConfig.API_KEY3, BuildConfig.API_KEY4)
        val num = Random().nextInt(keyList.size)
        return retrofitService.getPlayLists(keyList[num], "snippet", musicId, "50")
    }

    suspend fun fetchRecommendedPlaylists(): Response<PlayListSearchData> {
        val channelId = musicCategoryRepository.recommendPlaylistChannelId
        val keyList = listOf(BuildConfig.API_KEY6)

        return retrofitService.getPlayListsInChannel(keyList.first(), "snippet", channelId, "50", null)

    }

    suspend fun fetchTypedPlaylists(): Response<PlayListSearchData>{
        val channelId = musicCategoryRepository.typedPlaylistChannelId
        val keyList = listOf(BuildConfig.TOY_PROJECT)

        return retrofitService.getPlayListsInChannel(keyList.first(), "snippet", channelId, "50", null)

    }

    suspend fun fetchVideoDetailData(videoId: String): Response<VideoDetailData> {
        val keyList = listOf(
            BuildConfig.API_KEY110999_3, BuildConfig.API_KEY38922_3,
            BuildConfig.API_KEY860801_3,BuildConfig.API_KEY991101_3,BuildConfig.API_KEY38924_3,
            BuildConfig.API_KEY38931_3,)
        val num = Random().nextInt(keyList.size)

        return retrofitService.getVideoDetail(keyList[num], "snippet, statistics",videoId)
    }

    suspend fun fetchChannelDetailData(channelId: String): Response<ChannelSearchData> {
        val keyList = listOf(
            BuildConfig.API_KEY12, BuildConfig.RAISE_DEVELOP,
            BuildConfig.API_KEY110901_3, BuildConfig.API_KEY11098608_3,BuildConfig.API_KEY38934_3, BuildConfig.API_KEY38933_3)
        val num = Random().nextInt(keyList.size)
        return retrofitService.getChannelData(
            keyList[num], "snippet, contentDetails, statistics, brandingSettings"
            ,channelId)
    }

    suspend fun fetchVideoCommentThreadData(videoId: String): Response<CommentThreadData> {
        val keyList = listOf(BuildConfig.API_KEY38947, BuildConfig.API_KEY38948, BuildConfig.API_KEY38949)
        val num = Random().nextInt(keyList.size)
        return retrofitService.getCommentThreads(keyList[num],"snippet",videoId,"100","relevance",null, "plainText")
    }

    suspend fun fetchPlaylistItemsData(playlistDataModel: PlaylistDataModel, nextPageToken: String?): Response<PlayListVideoSearchData>{
        val keyList = listOf(BuildConfig.API_KEY38947, BuildConfig.API_KEY38948, BuildConfig.API_KEY38949)
        val num = Random().nextInt(keyList.size)
        return retrofitService.getPlayListVideoItems(keyList[num],"snippet",playlistDataModel.playlistId,nextPageToken,"50")
    }

    suspend fun fetchVideoSearchData(searchKeyword: String, nextPageToken: String?): Response<VideoSearchData> {
        val random = Random()
        val keyList = listOf(BuildConfig.API_KEY38954)
        val num = random.nextInt(keyList.size)

        return retrofitService.getVideoSearchResult(
            keyList[num],"snippet",searchKeyword,"50","video",
            nextPageToken
        )
    }

    suspend fun fetchChannelVideoData(playlistId: String, pageToken: String?): Response<PlayListVideoSearchData> {
        val keyList = listOf(BuildConfig.API_KEY38952)
        val num = Random().nextInt(keyList.size)
        return retrofitService.getPlayListVideoItems(
            keyList[num], "snippet", playlistId, pageToken, "50")
    }
}