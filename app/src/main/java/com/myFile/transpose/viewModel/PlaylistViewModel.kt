package com.myFile.transpose.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.myFile.transpose.MyApplication
import com.myFile.transpose.data.model.PlaylistDataModel
import com.myFile.transpose.data.repository.MusicCategoryRepository
import com.myFile.transpose.utils.YoutubeDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PlaylistViewModel(application: MyApplication): ViewModel() {
    private val youtubeDataRepository = application.youtubeDataRepository
    private val youtubeDataMapper = YoutubeDataMapper(application.applicationContext)

    private val _nationalPlaylists: MutableLiveData<ArrayList<PlaylistDataModel>> by lazy{
        MutableLiveData<ArrayList<PlaylistDataModel>>().also {
            viewModelScope.launch {
                async { fetchNationalPlaylists() }
            }
        }
    }

    val nationalPlaylist: LiveData<ArrayList<PlaylistDataModel>> get() = _nationalPlaylists

    private val _recommendedPlaylists: MutableLiveData<ArrayList<PlaylistDataModel>> by lazy{
        MutableLiveData<ArrayList<PlaylistDataModel>>().also {
            viewModelScope.launch {
                async { fetchRecommendedPlaylists() }
            }
        }
    }
    val recommendedPlaylists: LiveData<ArrayList<PlaylistDataModel>> get() = _recommendedPlaylists

    private val _typedPlaylists: MutableLiveData<ArrayList<PlaylistDataModel>> by lazy {
        MutableLiveData<ArrayList<PlaylistDataModel>>().also {
            viewModelScope.launch {
                async { fetchTypedPlaylists() }
            }
        }
    }
    val typedPlaylists: LiveData<ArrayList<PlaylistDataModel>> get() = _typedPlaylists

    private val _severErrorCode: MutableLiveData<Int> = MutableLiveData()
    val serverErrorCode: LiveData<Int> get() = _severErrorCode

    private val _severErrorMessage: MutableLiveData<String> = MutableLiveData()
    val severErrorMessage: LiveData<String> get() = _severErrorMessage

    private val _errorException: MutableLiveData<Exception> = MutableLiveData()
    val errorException: LiveData<Exception> get() = _errorException

    fun clearPlaylistData(){
        _nationalPlaylists.value = arrayListOf()
        _recommendedPlaylists.value = arrayListOf()
        _typedPlaylists.value = arrayListOf()
    }
    fun loadAllData() = viewModelScope.launch(Dispatchers.IO){
        Log.d("로드 올 데이타 실행","ㅇ")
        async { fetchRecommendedPlaylists() }
        async { fetchNationalPlaylists() }
        async { fetchTypedPlaylists() }
    }

    private suspend fun fetchNationalPlaylists() {
        val nationPlaylistIds = MusicCategoryRepository().nationalPlaylistIds
        nationPlaylistIds.forEach {
            try {
                val responses = youtubeDataRepository.fetchNationalPlaylists(it)
                val currentList = nationalPlaylist.value ?: arrayListOf()

                val responseBody = responses.body()
                if (responses.isSuccessful && responseBody != null) {
                    val newItem =
                        youtubeDataMapper.mapPlaylistDataModelList(responseBody)
                    currentList.add(newItem)
                    _nationalPlaylists.postValue(currentList)
                }
            } catch (e: Exception) {
                Log.d("nationException", "sadf")
            }

        }
    }

    private suspend fun fetchRecommendedPlaylists(){
        try {
            val responses = youtubeDataRepository.fetchRecommendedPlaylists()
            val body = responses.body()
            val currentList = recommendedPlaylists.value ?: arrayListOf()
            if (responses.isSuccessful && body != null){
                currentList.addAll(youtubeDataMapper.mapPlaylistDataModelsInChannelId(body))
                Log.d("커런트 리스트!","$currentList")
                _recommendedPlaylists.postValue(currentList)
            }else{
                Log.d("recommendedFail","sadf")
            }

        }catch (e: Exception){
            Log.d("recommendedExcpetion","$e")
        }
    }

    private suspend fun fetchTypedPlaylists(){
        try {
            val responses = youtubeDataRepository.fetchTypedPlaylists()
            val body = responses.body()
            val currentList = typedPlaylists.value ?: arrayListOf()
            if (responses.isSuccessful && body != null){
                currentList.addAll(youtubeDataMapper.mapPlaylistDataModelsInChannelId(body))
                _typedPlaylists.postValue(currentList)
            }else{
                Log.d("typePlaylistFail","asdf")
            }

        }catch (e: Exception){
            Log.d("typePlaylistexception","$e")
        }
    }
}

class PlaylistViewModelFactory(private val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}