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
import com.myFile.transpose.data.model.VideoDetailDataModel
import com.myFile.transpose.others.constants.Actions
import com.myFile.transpose.utils.YoutubeDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAudioFileViewModel(val application: MyApplication): ViewModel() {
    private val _myAudioFilesOrigin:MutableLiveData<List<MusicFileDataModel>> = MutableLiveData()
    val myAudioFilesOrigin:LiveData<List<MusicFileDataModel>> get() = _myAudioFilesOrigin

    private val _myAudioFiles: MutableLiveData<List<VideoDataModel>> = MutableLiveData()
    val myAudioFiles: LiveData<List<VideoDataModel>> get() = _myAudioFiles

    private val mapper = YoutubeDataMapper(application.applicationContext)

    var isLoaded: Boolean = false

    fun deleteMusicFile(context: Context, contentUri: Uri): Boolean {
        return try {
            val deletedRows = context.contentResolver.delete(contentUri, null, null)
            deletedRows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteMusicFileFromList(position: Int){
        val currentList = myAudioFiles.value?.toMutableList() ?: return
        currentList.removeAt(position)
        _myAudioFiles.value = currentList
    }

    private fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }

    private suspend fun loadAlbumArt(context: Context, albumId: Long): Bitmap? {
        val albumArtUri = getAlbumArtUri(albumId)
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(albumArtUri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: FileNotFoundException) {
                null
            }
        }
    }




    fun fetchMusicFiles(context: Context) = viewModelScope.launch {
        val musicFilesList = mutableListOf<MusicFileDataModel>()
        val contentResolver = context.contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val musicCursor = contentResolver.query(musicUri, projection, selection, null, null)

        musicCursor?.use {
            Log.d(Actions.TAG,"과연 null인지 ${musicCursor.count}")
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID) // 추가
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)



            while (it.moveToNext()) {
                val id = it.getLong(idColumn) // 추가
                val path = it.getString(pathColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val albumId = it.getLong(albumIdColumn)

                val dateAddedInSeconds = it.getLong(dateColumn)



                musicFilesList.add(MusicFileDataModel(id, path, title, artist, albumId, dateAddedInSeconds))
            }
        }

        isLoaded = true

        _myAudioFilesOrigin.value = musicFilesList

        _myAudioFiles.value = mapper.mapMyAudioFileToVideoDataModel(musicFilesList)
    }


}

class MyAudioFileViewModelFactory(val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyAudioFileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyAudioFileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}