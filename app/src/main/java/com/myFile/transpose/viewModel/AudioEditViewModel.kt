package com.myFile.transpose.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myFile.transpose.MyApplication

class AudioEditViewModel: ViewModel() {
    var scrollPosition = 0



}

class AudioEditViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioEditViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}