package com.myFile.transpose.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.myFile.transpose.retrofit.VideoData

class Converters {
    @TypeConverter
    fun dataToJson(value: VideoData): String{
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToData(value: String): VideoData {
        return Gson().fromJson(value, VideoData::class.java)
    }
}