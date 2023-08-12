package com.myFile.transpose.viewModel

import androidx.lifecycle.*
import com.myFile.transpose.database.Musics
import com.myFile.transpose.model.VideoDataModel
import com.myFile.transpose.repository.MyPlaylistRepository
import com.myFile.transpose.repository.SuggestionKeywordRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MyPlaylistItemsViewModel(private val myPlaylistRepository: MyPlaylistRepository): ViewModel() {
    private val _myPlaylistItems: MutableLiveData<List<Musics>> = MutableLiveData(arrayListOf())
    val myPlaylistItems: LiveData<List<Musics>> get() = _myPlaylistItems


    fun getPlaylistItemsByPlaylistId(playlistId: Int) = viewModelScope.launch{
        _myPlaylistItems.postValue(myPlaylistRepository.getPlaylistItemsByPlaylistId(playlistId))
    }

    fun deletePlaylistItem(music: Musics, playlistId: Int) = viewModelScope.launch {
        val job = async { myPlaylistRepository.deleteMusicItem(music) }
        job.await()
        getPlaylistItemsByPlaylistId(playlistId)
    }

    fun getPlaylistVideoItems(): List<VideoDataModel>?{
        return _myPlaylistItems.value?.map { it.musicData }
    }
}

class MyPlaylistItemsViewModelFactory(private val myPlaylistRepository: MyPlaylistRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPlaylistItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPlaylistItemsViewModel(myPlaylistRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}