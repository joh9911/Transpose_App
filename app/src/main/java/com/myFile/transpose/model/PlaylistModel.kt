package com.myFile.transpose.model

import com.myFile.transpose.retrofit.VideoData

data class PlaylistModel(
    val playlistName: String?,
    val playlistItems: List<VideoData>,
    val firstPosition: Int
)
