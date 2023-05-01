package com.myFile.transpose.model

import com.myFile.transpose.retrofit.CommentData
import com.myFile.transpose.retrofit.VideoData

sealed class PlayerFragmentMainItem{
    object LoadingHeader: PlayerFragmentMainItem()
    data class HeaderTitleData(val videoData: VideoData): PlayerFragmentMainItem()
    data class HeaderRestData(val headerViewData: HeaderViewData): PlayerFragmentMainItem()
    data class ContentData(val commentData: CommentData): PlayerFragmentMainItem()
}
