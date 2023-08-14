package com.myFile.transpose.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.myFile.transpose.MyApplication
import com.myFile.transpose.utils.YoutubeDataMapper
import com.myFile.transpose.model.model.PlaylistDataModel
import com.myFile.transpose.model.repository.MusicCategoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class HomeViewModel(application: MyApplication,
): ViewModel() {
    private val youtubeDataRepository = application.youtubeDataRepository
    private val suggestionKeywordRepository = application.suggestionKeywordRepository
    private val youtubeDataMapper = YoutubeDataMapper(application.applicationContext)

    private val _nationalPlaylists: MutableLiveData<ArrayList<PlaylistDataModel>> = MutableLiveData()
    val nationalPlaylist: LiveData<ArrayList<PlaylistDataModel>> get() = _nationalPlaylists

    private val _recommendedPlaylists: MutableLiveData<ArrayList<PlaylistDataModel>> = MutableLiveData()
    val recommendedPlaylists: LiveData<ArrayList<PlaylistDataModel>> get() = _recommendedPlaylists

    private val _typedPlaylists: MutableLiveData<ArrayList<PlaylistDataModel>> = MutableLiveData()
    val typedPlaylists: LiveData<ArrayList<PlaylistDataModel>> get() = _typedPlaylists

    private val _severErrorCode: MutableLiveData<Int> = MutableLiveData()
    val serverErrorCode: LiveData<Int> get() = _severErrorCode

    private val _severErrorMessage: MutableLiveData<String> = MutableLiveData()
    val severErrorMessage: LiveData<String> get() = _severErrorMessage

    private val _errorException: MutableLiveData<Exception> = MutableLiveData()
    val errorException: LiveData<Exception> get() = _errorException

    private val _suggestionKeywords: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val suggestionKeywords: LiveData<ArrayList<String>> get() = _suggestionKeywords

    init {
        loadAllData()
    }

    fun clearPlaylistData(){
        _nationalPlaylists.value = arrayListOf()
        _recommendedPlaylists.value = arrayListOf()
        _typedPlaylists.value = arrayListOf()
    }
    fun loadAllData() = viewModelScope.launch{
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

    fun clearSuggestionKeywords(){
        _suggestionKeywords.value = arrayListOf()
    }

    fun getSuggestionKeyword(newText: String) = viewModelScope.launch{
        val response = suggestionKeywordRepository.getSuggestionKeyword(newText)
        if (response.isSuccessful){
            val responseString = convertStringUnicodeToKorean(response.body()?.string()!!)
            val splitBracketList = responseString.split('[')
            val splitCommaList = splitBracketList[2].split(',')
            if (splitCommaList[0] != "]]" && splitCommaList[0] != '"'.toString()) {
                addSubstringToSuggestionKeyword(splitCommaList)
            }
        }else{
            clearSuggestionKeywords()
        }
    }

    /**
    문자열 정보가 이상하게 들어와 알맞게 나눠주고 리스트에 추가
     **/
    private fun addSubstringToSuggestionKeyword(splitList: List<String>){
        val currentList = splitList.filter { it.length >= 3 }
            .map { if (it.last() == ']') it.substring(1, it.length - 2) else it.substring(1, it.length - 1) }
        _suggestionKeywords.value = ArrayList(currentList)
    }

    private fun convertStringUnicodeToKorean(data: String): String {
        val sb = StringBuilder() // 단일 쓰레드이므로 StringBuilder 선언
        var i = 0
        /**
         * \uXXXX 로 된 아스키코드 변경
         * i+2 to i+6 을 16진수의 int 계산 후 char 타입으로 변환
         */
        while (i < data.length) {
            if (data[i] == '\\' && data[i + 1] == 'u') {
                val word = data.substring(i + 2, i + 6).toInt(16).toChar()
                sb.append(word)
                i += 5
            } else {
                sb.append(data[i])
            }
            i++
        }
        return sb.toString()
    }

}

class HomeFragmentViewModelFactory(private val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}