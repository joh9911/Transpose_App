package com.myFile.transpose.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.myFile.transpose.MyApplication
import com.myFile.transpose.utils.YoutubeDataMapper
import com.myFile.transpose.model.model.ChannelDataModel
import com.myFile.transpose.model.model.CommentDataModel
import com.myFile.transpose.model.model.VideoDataModel
import com.myFile.transpose.model.model.VideoDetailDataModel
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VideoPlayerViewModel(application: MyApplication): ViewModel() {
    private val youtubeDataRepository = application.youtubeDataRepository
    private val youtubeDataMapper = YoutubeDataMapper(application.applicationContext)

    private val _currentVideoDetailData: MutableLiveData<VideoDetailDataModel?> = MutableLiveData()
    val currentVideoDetailData: LiveData<VideoDetailDataModel?> get() = _currentVideoDetailData

    private val _currentChannelDetailData: MutableLiveData<ChannelDataModel?> = MutableLiveData()
    val currentChannelDetailData: LiveData<ChannelDataModel?> get() = _currentChannelDetailData

    private val _currentCommentThreadData: MutableLiveData<List<CommentDataModel>?> = MutableLiveData()
    val currentCommentThreadData: LiveData<List<CommentDataModel>?> get() = _currentCommentThreadData

    private val _currentVideoDataModel: MutableLiveData<VideoDataModel?> = MutableLiveData()
    val currentVideoDataModel: LiveData<VideoDataModel?> get() = _currentVideoDataModel

    private var convertUrlJob: Job? = null

    private val _currentVideoConvertedUrl: MutableLiveData<String?> = MutableLiveData()
    val currentVideoConvertedUrl: LiveData<String?> get() = _currentVideoConvertedUrl


    fun fetchAllData(videoId: String) = viewModelScope.launch{
        fetchDetailData(videoId)
        fetchCommentThreadData(videoId)
    }

    private suspend fun fetchDetailData(videoId: String){

        try {
            val videoDetailResponse = youtubeDataRepository.fetchVideoDetailData(videoId)
            val videoDetailBody = videoDetailResponse.body()

            if (videoDetailResponse.isSuccessful && videoDetailBody != null){
                val currentVideoDetailData = youtubeDataMapper.mapVideoDetailDataModel(videoDetailBody)
                _currentVideoDetailData.postValue(currentVideoDetailData)


                val currentVideoDataModel = youtubeDataMapper.mapVideoDataModelByVideoDetailResponse(videoDetailBody)
                _currentVideoDataModel.postValue(currentVideoDataModel)
                val channelDetailResponse = youtubeDataRepository.fetchChannelDetailData(currentVideoDetailData.channelId)
                val channelDetailBody = channelDetailResponse.body()

                if (channelDetailResponse.isSuccessful && channelDetailBody != null){
                    val channelDataModel = youtubeDataMapper.mapChannelDataModel(channelDetailBody)
                    _currentChannelDetailData.postValue(channelDataModel)
                }
            }
            else{
                _currentVideoDetailData.postValue(null)
                _currentVideoDataModel.postValue(null)
                _currentChannelDetailData.postValue(null)
            }
        }catch (e: Exception){
            Log.d("detailData오류", "$e")
            _currentVideoDetailData.postValue(null)
            _currentVideoDataModel.postValue(null)
            _currentChannelDetailData.postValue(null)
        }
    }


    private suspend fun fetchCommentThreadData(videoId: String){
        try {
            val response = youtubeDataRepository.fetchVideoCommentThreadData(videoId)

            val body = response.body()
            if (response.isSuccessful && body != null){
                val currentCommentDataModel = youtubeDataMapper.mapCommentThreadData(body)
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

class VideoPlayerViewModelFactory(private val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoPlayerViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}