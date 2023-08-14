package com.myFile.transpose.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myFile.transpose.model.model.NowPlaylistModel
import com.myFile.transpose.model.model.ChannelDataModel
import com.myFile.transpose.model.model.PlaylistDataModel
import com.myFile.transpose.model.model.VideoDataModel


class SharedViewModel: ViewModel() {

    /*
    VideoPlayerFragment 를 위한 playbackMode.
    PlaybackMode 가 같을 경우 현재 실행중인 VideoPlayerFragment 의 뷰만 바꿔줌
    다를 경우 replace
     */
    enum class PlaybackMode {
        SINGLE_VIDEO,
        PLAYLIST
    }
    var playbackMode: PlaybackMode = PlaybackMode.SINGLE_VIDEO

    // VideoPlayerFragment 를 위한 변수
    private val _currentVideoData: MutableLiveData<VideoDataModel> = MutableLiveData()
    val currentVideoData: LiveData<VideoDataModel> get() = _currentVideoData

    private val _nowPlaylistModel: MutableLiveData<NowPlaylistModel> = MutableLiveData()
    val nowPlaylistModel: LiveData<NowPlaylistModel> get() = _nowPlaylistModel

    private val _singleModeVideoId: MutableLiveData<String> = MutableLiveData()
    val singleModeVideoId: LiveData<String> get() = _singleModeVideoId


    // PlaylistsFragment 를 위한 변수
    private val _playlistData: MutableLiveData<PlaylistDataModel> = MutableLiveData()
    val playlistData: LiveData<PlaylistDataModel> get() = _playlistData

    // SearchResultFragment 를 위한 변수
    private val _searchKeyword: MutableLiveData<String> = MutableLiveData()
    val searchKeyword: LiveData<String> get() = _searchKeyword

    // ChannelFragment 를 위한 변수
    private val _channelData: MutableLiveData<ChannelDataModel?> = MutableLiveData()
    val channelData: LiveData<ChannelDataModel?> get() = _channelData

    // MyPlaylistItemFragment 를 위한 변수
    private val _myPlaylistId: MutableLiveData<Int> = MutableLiveData(0)
    val myPlaylistId: LiveData<Int> get() = _myPlaylistId

    var myPlaylistTitle: String? = null

    fun setMyPlaylistId(myPlaylistId: Int){
        _myPlaylistId.value = myPlaylistId
    }


    fun setSingleModeVideoId(videoId: String){
        _singleModeVideoId.postValue(videoId)
    }

    fun setChannelData(channelData: ChannelDataModel?){
        _channelData.value = channelData
    }

    fun setSearchKeywordData(query: String){
        _searchKeyword.value = query
    }

    fun setPlaylistsFragmentData(playListData: PlaylistDataModel){
        _playlistData.value = playListData
    }

    fun setVideoPlayerFragmentData(videoData: VideoDataModel, nowPlaylistModel: NowPlaylistModel){
        _currentVideoData.value = videoData
        _nowPlaylistModel.value = nowPlaylistModel
    }

    fun replaceVideoByPosition(position: Int) {
        nowPlaylistModel.value?.updateCurrentPosition(position)
        nowPlaylistModel.value?.refreshPlaylist()
        _currentVideoData.value = nowPlaylistModel.value?.currentMusicModel()
    }

    fun playPrevVideo(){
        nowPlaylistModel.value?.prevMusic()
        nowPlaylistModel.value?.refreshPlaylist()
        _currentVideoData.value = nowPlaylistModel.value?.currentMusicModel()
    }

    fun playNextVideo(){
        nowPlaylistModel.value?.nextMusic()
        nowPlaylistModel.value?.refreshPlaylist()
        _currentVideoData.value = nowPlaylistModel.value?.currentMusicModel()
    }
}

class SharedViewModelModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}