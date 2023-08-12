package com.myFile.transpose.model

data class ChannelDataModel(
    val channelTitle: String,
    val channelDescription: String,
    val channelBanner: String?,
    val channelThumbnail: String?,
    val channelVideoCount: String,
    val channelViewCount: String,
    val channelSubscriberCount: String,
    val channelPlaylistId: String
)