package com.myFile.transpose.viewModel

import androidx.lifecycle.*
import com.myFile.transpose.YoutubeDataMapper
import com.myFile.transpose.YoutubeDigitConverter
import com.myFile.transpose.dto.PlayListVideoSearchData
import com.myFile.transpose.model.ChannelDataModel
import com.myFile.transpose.model.VideoDataModel
import com.myFile.transpose.repository.YoutubeDataRepository
import kotlinx.coroutines.launch

class ChannelViewModel(private val youtubeDataRepository: YoutubeDataRepository, private val youtubeDigitConverter: YoutubeDigitConverter): ViewModel() {
    private val youtubeDataMapper = YoutubeDataMapper()

    private val _channelDataModel: MutableLiveData<ChannelDataModel> = MutableLiveData()
    val channelDataModel: LiveData<ChannelDataModel> get() = _channelDataModel

    private val _channelVideoDataList: MutableLiveData<ArrayList<VideoDataModel>> = MutableLiveData()
    val channelVideoDataList: LiveData<ArrayList<VideoDataModel>> get() = _channelVideoDataList

    var nextPageToken: String? = null

    fun setChannelData(channelDataModel: ChannelDataModel){
        _channelDataModel.value = channelDataModel
    }
    fun fetchChannelVideoData(dateArray: Array<String>) = viewModelScope.launch{
        val channelData = this@ChannelViewModel.channelDataModel.value ?: return@launch
        try {
            val response = youtubeDataRepository.fetchChannelVideoData(channelData.channelPlaylistId, nextPageToken)

            val body = response.body()
            if (response.isSuccessful && body != null){
                nextPageToken = body.nextPageToken
                val newItems = youtubeDataMapper.mapPlaylistItemsDataModelList(body, dateArray)
                val currentList = channelVideoDataList.value ?: arrayListOf()
                currentList.addAll(newItems)
                _channelVideoDataList.postValue(currentList)
            }
        }catch (e: Exception) {}
    }

}

class ChannelViewModelFactory(private val youtubeDataRepository: YoutubeDataRepository, private val youtubeDigitConverter: YoutubeDigitConverter) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChannelViewModel(youtubeDataRepository, youtubeDigitConverter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}