package com.myFile.transpose.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistModel(
    val playlistName: String?,
    val playlistItems: List<VideoDataModel>,
    val firstPosition: Int
): Parcelable
