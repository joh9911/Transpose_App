package com.myFile.transpose.data.model

import android.graphics.Bitmap

data class VideoFileDataModel(
    val id: Long?,
    val path: String?,
    val title: String?,
    val thumbnail: Bitmap?,
    val date: Long?,
)