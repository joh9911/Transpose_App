package com.myFile.transpose.viewModel

import android.content.ContentValues
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.myFile.transpose.YoutubeDataMapper
import com.myFile.transpose.model.*
import com.myFile.transpose.repository.YoutubeDataRepository
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException

class VideoPlayerViewModel(private val youtubeDataRepository: YoutubeDataRepository): ViewModel() {

    private val youtubeDataMapper = YoutubeDataMapper()

    private val _currentVideoDetailData: MutableLiveData<VideoDetailDataModel> = MutableLiveData()
    val currentVideoDetailData: LiveData<VideoDetailDataModel> get() = _currentVideoDetailData

    private val _currentChannelDetailData: MutableLiveData<ChannelDataModel> = MutableLiveData()
    val currentChannelDetailData: LiveData<ChannelDataModel> get() = _currentChannelDetailData

    private val _currentCommentThreadData: MutableLiveData<List<CommentDataModel>> = MutableLiveData()
    val currentCommentThreadData: LiveData<List<CommentDataModel>> get() = _currentCommentThreadData

    private val _currentVideoDataModel: MutableLiveData<VideoDataModel> = MutableLiveData()
    val currentVideoDataModel: LiveData<VideoDataModel> get() = _currentVideoDataModel

    private var convertUrlJob: Job? = null

    private val _currentVideoConvertedUrl: MutableLiveData<String?> = MutableLiveData()
    val currentVideoConvertedUrl: LiveData<String?> get() = _currentVideoConvertedUrl


    fun fetchAllData(videoId: String, dateArray: Array<String>, viewArray: Array<String>, subscriberArray: Array<String>) = viewModelScope.launch{
        fetchDetailData(videoId, dateArray, viewArray, subscriberArray)
        fetchCommentThreadData(videoId, dateArray)
    }

    private suspend fun fetchDetailData(videoId: String, dateArray: Array<String>, viewArray: Array<String>, subscriberArray: Array<String>){

        try {
            val videoDetailResponse = youtubeDataRepository.fetchVideoDetailData(videoId)
            val videoDetailBody = videoDetailResponse.body()

            if (videoDetailResponse.isSuccessful && videoDetailBody != null){
                val currentVideoDetailData = youtubeDataMapper.mapVideoDetailDataModel(videoDetailBody, dateArray, viewArray)
                _currentVideoDetailData.postValue(currentVideoDetailData)

                val currentVideoDataModel = youtubeDataMapper.mapVideoDataModelByVideoDetailResponse(videoDetailBody, dateArray)
                _currentVideoDataModel.postValue(currentVideoDataModel)
                val channelDetailResponse = youtubeDataRepository.fetchChannelDetailData(currentVideoDetailData.channelId)
                val channelDetailBody = channelDetailResponse.body()

                if (channelDetailResponse.isSuccessful && channelDetailBody != null){
                    val channelDataModel = youtubeDataMapper.mapChannelDataModel(channelDetailBody, dateArray, viewArray, subscriberArray)
                    _currentChannelDetailData.postValue(channelDataModel)
                }
            }
        }catch (e: Exception){
            Log.d("detailData오류", "$e")
        }
    }


    private suspend fun fetchCommentThreadData(videoId: String, dateArray: Array<String>){
        try {
            val response = youtubeDataRepository.fetchVideoCommentThreadData(videoId)

            val body = response.body()
            if (response.isSuccessful && body != null){
                val currentCommentDataModel = youtubeDataMapper.mapCommentThreadData(body, dateArray)
                _currentCommentThreadData.postValue(currentCommentDataModel)
            }
            else{
                Log.d("왜 성공이지 않았지?","${response.message()}")
            }
        }catch (e: Exception){
            Log.d("코멘트 데이타","$e")
        }

    }

    fun convertUrl(videoId: String) {
        convertUrlJob?.cancel()
        convertUrlJob = viewModelScope.launch(Dispatchers.IO) {
            val request = YoutubeDLRequest(videoId)
            request.addOption("-f b")
            try {
                val response = YoutubeDL.getInstance().getInfo(request)
                _currentVideoConvertedUrl.postValue(response.url)
                Log.d("뷰모델 유알엘","${response.url}")
            }catch (e: Exception){
                Log.d("익셉션","$e")
            }
        }
    }

}

class VideoPlayerViewModelFactory(private val youtubeDataRepository: YoutubeDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoPlayerViewModel(youtubeDataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}