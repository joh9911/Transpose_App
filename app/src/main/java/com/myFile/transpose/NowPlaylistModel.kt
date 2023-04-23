package com.myFile.transpose

import com.myFile.transpose.retrofit.VideoData


data class NowPlaylistModel (
    private var playMusicList: List<VideoData> = emptyList(),
    private var currentPosition: Int = -1, // -1: 초기화 되지 않은 값
    private var playlistTitle: String?
){
    init {
        refreshPlaylist()
    }
    fun getCurrentPosition(): Int{
        return currentPosition
    }

    fun getPlayMusicList(): List<VideoData>{
        return playMusicList
    }

    fun getPlaylistTitle(): String?{
        return playlistTitle
    }

    // 가져 갈 때마다 position 위치 보고 반환
    fun refreshPlaylist() {
        playMusicList = playMusicList.mapIndexed { index, musicModel ->
            val newItem = musicModel.copy(
                isPlaying = index == currentPosition
            )
            newItem
        }
    }

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun nextMusic(): VideoData? {
        if (playMusicList.isEmpty()) return null

        currentPosition = if ((currentPosition + 1) == playMusicList.size) 0 else currentPosition + 1

        return playMusicList[currentPosition]
    }

    fun prevMusic(): VideoData? {
        if (playMusicList.isEmpty()) return null

        // kotlin 의 lastIndex 사용 해보기
        currentPosition = if ((currentPosition - 1) < 0) playMusicList.lastIndex else currentPosition - 1

        return playMusicList[currentPosition]
    }

    fun currentMusicModel(): VideoData {
        return playMusicList[currentPosition]
    }
}