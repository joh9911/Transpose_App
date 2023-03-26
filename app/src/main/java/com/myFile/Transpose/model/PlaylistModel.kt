package com.myFile.Transpose.model

import com.myFile.Transpose.VideoData

data class PlaylistModel(
    val playlistName: String?,
    val playlistItems: List<VideoData>,
    val firstPosition: Int
)
