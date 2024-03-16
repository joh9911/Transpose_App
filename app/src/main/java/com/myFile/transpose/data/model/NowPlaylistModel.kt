package com.myFile.transpose.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class NowPlaylistModel (
    private var playMusicList: List<VideoDataModel> = emptyList(),
    private var currentPosition: Int = -1, // -1: 초기화 되지 않은 값
    private var playlistTitle: String?,
    private var audioEffectsDataModels: List<AudioEffectsDataModel?>?
){
    init {
        refreshPlaylist()
    }
    fun getCurrentPosition(): Int{
        return currentPosition
    }

    fun getPlayMusicList(): List<VideoDataModel>{
        return playMusicList
    }

    fun getAudioEffectList(): List<AudioEffectsDataModel?>? {
        return audioEffectsDataModels
    }

    fun getPlaylistTitle(): String?{
        return playlistTitle
    }

    // 가져 갈 때마다 position 위치 보고 반환
    fun refreshPlaylist() {
        CoroutineScope(Dispatchers.IO).launch {
            playMusicList = playMusicList.mapIndexed { index, musicModel ->
                val newItem = musicModel.copy(
                    isPlaying = index == currentPosition
                )
                newItem
            }
        }
    }

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun nextMusic(): VideoDataModel? {
        if (playMusicList.isEmpty()) return null

        currentPosition = if ((currentPosition + 1) == playMusicList.size) 0 else currentPosition + 1

        return playMusicList[currentPosition]
    }

    fun prevMusic(): VideoDataModel? {
        if (playMusicList.isEmpty()) return null

        // kotlin 의 lastIndex 사용 해보기
        currentPosition = if ((currentPosition - 1) < 0) playMusicList.lastIndex else currentPosition - 1

        return playMusicList[currentPosition]
    }

    fun currentMusicModel(): VideoDataModel {
        return playMusicList[currentPosition]
    }
}