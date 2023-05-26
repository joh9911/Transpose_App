package com.myFile.transpose.model

import android.os.Parcelable
import com.myFile.transpose.retrofit.VideoData
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistModel(
    val playlistName: String?,
    val playlistItems: List<VideoData>,
    val firstPosition: Int
): Parcelable
