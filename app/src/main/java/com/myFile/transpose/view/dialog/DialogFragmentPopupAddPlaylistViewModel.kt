package com.myFile.transpose.view.dialog

import androidx.lifecycle.*
import com.myFile.transpose.model.repository.MyPlaylistRepository
import com.myFile.transpose.database.Musics
import com.myFile.transpose.database.MyPlaylist
import kotlinx.coroutines.launch

class DialogFragmentPopupAddPlaylistViewModel(private val myPlaylistRepository: MyPlaylistRepository): ViewModel() {
    private val _myPlaylists: MutableLiveData<List<MyPlaylist>> = MutableLiveData()
    val myPlaylists: LiveData<List<MyPlaylist>> get() = _myPlaylists

    fun getAllPlaylist() = viewModelScope.launch {
        _myPlaylists.postValue(myPlaylistRepository.getAllPlaylist())
    }

    fun addMusicItem(music: Musics) = viewModelScope.launch {
        myPlaylistRepository.addMusicItem(music)
    }
}

class DialogFragmentPopupAddPlaylistViewModelFactory(private val myPlaylistRepository: MyPlaylistRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialogFragmentPopupAddPlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DialogFragmentPopupAddPlaylistViewModel(myPlaylistRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}