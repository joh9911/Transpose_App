package com.myFile.transpose.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.myFile.transpose.YoutubeDataMapper

import com.myFile.transpose.model.PlaylistDataModel
import com.myFile.transpose.model.VideoDataModel
import com.myFile.transpose.repository.YoutubeDataRepository
import kotlinx.coroutines.launch

class PlaylistItemsViewModel(private val youtubeDataRepository: YoutubeDataRepository): ViewModel() {
    private val youtubeDataMapper = YoutubeDataMapper()
    private val _playlistItems: MutableLiveData<List<VideoDataModel>> = MutableLiveData()
    val playlistItems: LiveData<List<VideoDataModel>> get() = _playlistItems

    var nextPageToken: String? = null

    fun fetchPlaylistItemsData(playListDataModel: PlaylistDataModel, dateArray: Array<String>) = viewModelScope.launch {
        try {
            val response = youtubeDataRepository.fetchPlaylistItemsData(playListDataModel, nextPageToken)

            val body = response.body()

            if (response.isSuccessful && body != null){
                nextPageToken = body.nextPageToken
                val newItems = youtubeDataMapper.mapPlaylistItemsDataModelList(body, dateArray)
                _playlistItems.postValue(newItems)
            }
        }catch (e: Exception){
            Log.d("읻겟볏","$e")
        }
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

class PlaylistItemsViewModelFactory(private val youtubeDataRepository: YoutubeDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistItemsViewModel(youtubeDataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}