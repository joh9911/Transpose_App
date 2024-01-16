package com.myFile.transpose.viewModel

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myFile.transpose.MyApplication
import com.myFile.transpose.data.model.MusicFileDataModel
import com.myFile.transpose.data.model.VideoDataModel
import com.myFile.transpose.data.model.VideoFileDataModel
import com.myFile.transpose.others.constants.Actions
import com.myFile.transpose.utils.YoutubeDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class MyVideoFileViewModel(val application: MyApplication): ViewModel() {
    private val _myVideoFilesOrigin: MutableLiveData<List<VideoFileDataModel>> = MutableLiveData()
    val myVideoFilesOrigin: LiveData<List<VideoFileDataModel>> get() = _myVideoFilesOrigin

    private val _myVideoFiles: MutableLiveData<List<VideoDataModel>> = MutableLiveData()
    val myVideoFiles: LiveData<List<VideoDataModel>> get() = _myVideoFiles

    private val mapper = YoutubeDataMapper(application.applicationContext)

    var isLoaded: Boolean = false

    fun deleteVideoFile(context: Context, contentUri: Uri): Boolean {
        return try {
            val deletedRows = context.contentResolver.delete(contentUri, null, null)
            deletedRows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteVideoFileFromList(position: Int){
        val currentList = myVideoFiles.value?.toMutableList() ?: return
        currentList.removeAt(position)
        _myVideoFiles.value = currentList
    }

    private suspend fun getVideoThumbnail(context: Context, videoId: Long): Bitmap? {
        return withContext(Dispatchers.IO){
            MediaStore.Video.Thumbnails.getThumbnail(
                context.contentResolver,
                videoId,
                MediaStore.Video.Thumbnails.MINI_KIND,
                null
            )
        }
    }


    fun fetchVideoFiles(context: Context) = viewModelScope.launch {
        val videoFilesList = mutableListOf<VideoFileDataModel>()
        val contentResolver = context.contentResolver
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
        )

        val videoCursor = contentResolver.query(videoUri, projection, null, null, null)

        videoCursor?.use {

            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val path = it.getString(pathColumn)
                val title = it.getString(titleColumn)
                val thumbnail = getVideoThumbnail(context, id)
                val dateAddedInSeconds = it.getLong(dateColumn)

                videoFilesList.add(
                    VideoFileDataModel(
                        id,
                        path,
                        title,
                        thumbnail,
                        dateAddedInSeconds
                    )
                )

            }
        }

        isLoaded = true

        _myVideoFilesOrigin.postValue(videoFilesList)
        _myVideoFiles.postValue(mapper.mapMyVideoFileToVideoDataModel(videoFilesList))
    }
}


    class MyVideoFileViewModelFactory(val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyVideoFileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyVideoFileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}