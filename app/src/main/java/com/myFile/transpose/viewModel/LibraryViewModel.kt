package com.myFile.transpose.viewModel

import androidx.lifecycle.*
import com.myFile.transpose.MyApplication
import kotlinx.coroutines.launch

class LibraryViewModel(application: MyApplication): ViewModel() {
    private val suggestionKeywordRepository = application.suggestionKeywordRepository

    private val _suggestionKeywords: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val suggestionKeywords: LiveData<ArrayList<String>> get() = _suggestionKeywords


    fun clearSuggestionKeywords(){
        _suggestionKeywords.value = arrayListOf()
    }

    fun getSuggestionKeyword(newText: String) = viewModelScope.launch{
        try {
            val response = suggestionKeywordRepository.getSuggestionKeyword(newText)
            if (response.isSuccessful){
                response.body()?.string()?.let{
                    val responseString = convertStringUnicodeToKorean(it)
                    val splitBracketList = responseString.split('[')
                    val splitCommaList = splitBracketList[2].split(',')
                    if (splitCommaList[0] != "]]" && splitCommaList[0] != '"'.toString()) {
                        addSubstringToSuggestionKeyword(splitCommaList)
                    }
                }
            }else{
                clearSuggestionKeywords()
            }
        }catch (e: Exception){

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


class LibraryViewModelFactory(private val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}