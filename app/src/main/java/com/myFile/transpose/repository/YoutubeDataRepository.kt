package com.myFile.transpose.repository

import com.myFile.transpose.BuildConfig
import com.myFile.transpose.dto.ChannelSearchData
import com.myFile.transpose.dto.CommentThreadData
import com.myFile.transpose.dto.PlayListSearchData
import com.myFile.transpose.dto.PlayListVideoSearchData
import com.myFile.transpose.model.PlaylistDataModel
import com.myFile.transpose.model.VideoDetailData
import com.myFile.transpose.model.VideoSearchData
import com.myFile.transpose.retrofit.RetrofitService
import retrofit2.Response
import java.util.*

class YoutubeDataRepository(
    private val retrofitService: RetrofitService,
) {
    private val musicCategoryRepository = MusicCategoryRepository()


    suspend fun fetchNationalPlaylists(musicId :String): Response<PlayListSearchData> {
        val keyArr = arrayListOf(BuildConfig.API_KEY38954)
        val num = Random().nextInt(keyArr.size)

        return retrofitService.getPlayLists(keyArr[num], "snippet", musicId, "50")
    }

    suspend fun fetchRecommendedPlaylists(): Response<PlayListSearchData> {
        val channelId = musicCategoryRepository.recommendPlaylistChannelId

        return retrofitService.getPlayListsInChannel(BuildConfig.API_KEY38954, "snippet", channelId, "50", null)

    }

    suspend fun fetchTypedPlaylists(): Response<PlayListSearchData>{
        val channelId = musicCategoryRepository.typedPlaylistChannelId

        return retrofitService.getPlayListsInChannel(BuildConfig.API_KEY38954, "snippet", channelId, "50", null)

    }


    suspend fun fetchVideoDetailData(videoId: String): Response<VideoDetailData> {
        return retrofitService.getVideoDetail(BuildConfig.API_KEY3, "snippet, statistics",videoId)
    }

    suspend fun fetchChannelDetailData(channelId: String): Response<ChannelSearchData> {
        return retrofitService.getChannelData(
            BuildConfig.API_KEY4, "snippet, contentDetails, statistics, brandingSettings"
            ,channelId)
    }

    suspend fun fetchVideoCommentThreadData(videoId: String): Response<CommentThreadData> {
        val keyList = arrayListOf(BuildConfig.RAISE_DEVELOP, BuildConfig.API_KEY12)
        val num = Random().nextInt(keyList.size)
        return retrofitService.getCommentThreads(keyList[num],"snippet",videoId,"100","relevance",null, "plainText")
    }

    suspend fun fetchPlaylistItemsData(playlistDataModel: PlaylistDataModel, nextPageToken: String?): Response<PlayListVideoSearchData>{
        return retrofitService.getPlayListVideoItems(BuildConfig.API_KEY4,"snippet",playlistDataModel.playlistId,nextPageToken,"50")
    }

    suspend fun fetchVideoSearchData(searchKeyword: String, nextPageToken: String?): Response<VideoSearchData> {
        val random = Random()
        val keyList = listOf(BuildConfig.API_KEY6,  BuildConfig.API_KEY11,
            BuildConfig.TOY_PROJECT, BuildConfig.API_KEY110901_3, BuildConfig.API_KEY11098608_3,
            BuildConfig.API_KEY110999_3, BuildConfig.API_KEY38922_3,BuildConfig.API_KEY389251_3,
            BuildConfig.API_KEY860801_3,BuildConfig.API_KEY991101_3,BuildConfig.API_KEY38923_1, BuildConfig.API_KEY38924_3,BuildConfig.API_KEY38926_3,
            BuildConfig.API_KEY38931_3,BuildConfig.API_KEY38933_3,BuildConfig.API_KEY38934_3, BuildConfig.API_KEY38935_1, BuildConfig.API_KEY38936_1, BuildConfig.API_KEY38937_1
            ,BuildConfig.API_KEY38941, BuildConfig.API_KEY38942, BuildConfig.API_KEY38943, BuildConfig.API_KEY38944, BuildConfig.API_KEY38945, BuildConfig.API_KEY38946
            ,BuildConfig.API_KEY38947, BuildConfig.API_KEY38948, BuildConfig.API_KEY38949, BuildConfig.API_KEY38950, BuildConfig.API_KEY38951,BuildConfig.API_KEY38952,
            BuildConfig.API_KEY38954)

        val num = random.nextInt(keyList.size)
        return retrofitService.getVideoSearchResult(
            keyList[num],"snippet",searchKeyword,"50","video",
            nextPageToken
        )
    }

    suspend fun fetchChannelVideoData(playlistId: String, pageToken: String?): Response<PlayListVideoSearchData> {
        return retrofitService.getPlayListVideoItems(
            BuildConfig.API_KEY2, "snippet", playlistId, pageToken, "50")
    }
}