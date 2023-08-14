package com.myFile.transpose.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.myFile.transpose.MyApplication
import com.myFile.transpose.utils.YoutubeDataMapper
import com.myFile.transpose.database.CashedKeyword
import com.myFile.transpose.database.PageToken
import com.myFile.transpose.database.YoutubeCashedData
import com.myFile.transpose.model.model.VideoDataModel
import kotlinx.coroutines.launch

class SearchResultViewModel
    (application: MyApplication): ViewModel() {
    private val youtubeDataRepository = application.youtubeDataRepository
    private val youtubeCashedDataRepository = application.youtubeCashedRepository

    private val youtubeDataMapper = YoutubeDataMapper(application.applicationContext)

    private val _searchKeyword: MutableLiveData<String> = MutableLiveData()
    val searchKeyword: LiveData<String> get() = _searchKeyword

    private val _videoSearchDataList: MutableLiveData<ArrayList<VideoDataModel>> = MutableLiveData()
    val videoSearchDataList: LiveData<ArrayList<VideoDataModel>> get() = _videoSearchDataList

    private val _isServerError: MutableLiveData<Boolean> = MutableLiveData()
    val isServerError: LiveData<Boolean> get() = _isServerError

    private val _isExceptionError: MutableLiveData<Exception> = MutableLiveData()
    val isExceptionError: LiveData<Exception> get() = _isExceptionError

    private var nextPageToken: String? = null

    fun setSearchKeyword(query: String){
        _searchKeyword.value = query
    }

    fun firstFetchOrGetData() = viewModelScope.launch{
        val searchKeyword = this@SearchResultViewModel.searchKeyword.value ?: ""
        val cashedKeyword = youtubeCashedDataRepository.getCashedKeywordDataBySearchKeyword(searchKeyword)
        if (cashedKeyword == null){
            fetchVideoSearchData()
        }else{
            getYoutubeCashedData()
        }
    }

    fun fetchVideoSearchData() = viewModelScope.launch{
        val searchKeyword = this@SearchResultViewModel.searchKeyword.value ?: return@launch
        try {
            val response = youtubeDataRepository.fetchVideoSearchData(searchKeyword, nextPageToken)

            val body = response.body()
            if (response.isSuccessful && body != null){
                nextPageToken = body.nextPageToken
                val newItems = youtubeDataMapper.mapVideoDataModelList(body)
                val currentList = videoSearchDataList.value ?: arrayListOf()
                currentList.addAll(newItems)
                _videoSearchDataList.postValue(currentList)
            }
            else
                _isServerError.postValue(true)
        }catch (e: Exception){
            _isExceptionError.postValue(e)
        }
    }

    private fun getYoutubeCashedData() = viewModelScope.launch{
        val searchKeyword = this@SearchResultViewModel.searchKeyword.value ?: ""
        nextPageToken = youtubeCashedDataRepository.getPageTokenBySearchKeyword(searchKeyword)?.nextPageToken
        val youtubeCashedDataList = youtubeCashedDataRepository.getYoutubeCashedDataFromDb(searchKeyword)?.map{
            it.searchVideoData
        } ?: arrayListOf()
        val currentList = videoSearchDataList.value ?: arrayListOf()
        currentList.addAll(youtubeCashedDataList)
        _videoSearchDataList.postValue(currentList)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("서치뷰모델이","클리어됨")
    }

    fun cashData(currentList: MutableList<VideoDataModel>) = viewModelScope.launch{
        val searchKeyword = this@SearchResultViewModel.searchKeyword.value ?: ""
        val pageTokenFromDB = youtubeCashedDataRepository.getPageTokenBySearchKeyword(searchKeyword)?.nextPageToken
        if (this@SearchResultViewModel.nextPageToken != pageTokenFromDB){
            val cashedKeyword = CashedKeyword(searchKeyword, System.currentTimeMillis())
            val youtubeDataList = currentList.map{ YoutubeCashedData(0, it, searchKeyword) }
            val pageToken = PageToken(0, nextPageToken, searchKeyword)
            youtubeCashedDataRepository.cashData(searchKeyword, cashedKeyword, pageToken, youtubeDataList)
        }
    }

    fun deleteOldData() = viewModelScope.launch {
        youtubeCashedDataRepository.deleteOldData()
    }

}

class SearchResultViewModelFactory(private val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchResultViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


//private suspend fun getSearchVideoDataWithNoKey(pageToken: String?){
//    val retrofit = RetrofitYT.initRetrofit()
//    val response = retrofit.create(RetrofitService::class.java).getVideoSearchResult(
//        null,"snippet",searchWord,"50","video",
//        pageToken
//    )
//    Log.d("yt 검색","$response")
//    Log.d("yt검색 결과","${response.body()}")
//    if (response.isSuccessful) {
//        if (response.body()?.items?.size != 0) {
//            withContext(Dispatchers.Main){
//                searchResultDataMapping(response.body()!!)
//            }
//            if (response.body()?.nextPageToken != null)
//                nextPageToken = response.body()?.nextPageToken!!
//        }
//    }
//    else{
//        /**
//         * 처음 검색했을 때, 실패가 떴다면 다시 처음부터 검색
//         */
//        if (searchResultDataList.isEmpty()){
//            withContext(Dispatchers.Main){
//                binding.progressBar.visibility = View.INVISIBLE
//                binding.recyclerView.visibility = View.INVISIBLE
//                binding.errorLinearLayout.visibility = View.VISIBLE
//            }
//        }
//
//        else{
//            withContext(Dispatchers.Main){
//                if (searchResultDataList[searchResultDataList.size - 1].title == " ")
//                    searchResultDataList.removeAt(searchResultDataList.size - 1)
//                Toast.makeText(activity, activity.getString(R.string.quota_error_message), Toast.LENGTH_SHORT).show()
//            }
//        }
//
//    }
//}