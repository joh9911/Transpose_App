package com.myFile.transpose.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoDataModel(
    val thumbnail: String,
    val title: String,
    val channelTitle: String,
    val channelId: String,
    val videoId: String,
    val date: String,
    val isPlaying: Boolean
): Parcelable
