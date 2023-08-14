package com.myFile.transpose.viewModel

import androidx.lifecycle.*
import com.myFile.transpose.MyApplication
import com.myFile.transpose.utils.YoutubeDataMapper
import com.myFile.transpose.model.model.ChannelDataModel
import com.myFile.transpose.model.model.VideoDataModel
import kotlinx.coroutines.launch

class ChannelViewModel(application: MyApplication): ViewModel() {
    private val youtubeDataMapper = YoutubeDataMapper(application.applicationContext)
    private val youtubeDataRepository = application.youtubeDataRepository

    private val _channelDataModel: MutableLiveData<ChannelDataModel> = MutableLiveData()
    val channelDataModel: LiveData<ChannelDataModel> get() = _channelDataModel

    private val _channelVideoDataList: MutableLiveData<ArrayList<VideoDataModel>> = MutableLiveData()
    val channelVideoDataList: LiveData<ArrayList<VideoDataModel>> get() = _channelVideoDataList

    var nextPageToken: String? = null


    fun fetchChannelVideoData(channelDataModel: ChannelDataModel?) = viewModelScope.launch{
        channelDataModel ?: return@launch
        try {
            val response = youtubeDataRepository.fetchChannelVideoData(channelDataModel.channelPlaylistId, nextPageToken)

            val body = response.body()
            if (response.isSuccessful && body != null){
                nextPageToken = body.nextPageToken
                val newItems = youtubeDataMapper.mapPlaylistItemsDataModelList(body)
                val currentList = channelVideoDataList.value ?: arrayListOf()
                currentList.addAll(newItems)
                _channelVideoDataList.postValue(currentList)
            }
        }catch (e: Exception) {}
    }

}

class ChannelViewModelFactory(private val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChannelViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}