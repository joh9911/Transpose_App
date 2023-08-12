package com.myFile.transpose.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.myFile.transpose.model.VideoDataModel

class Converters {
    @TypeConverter
    fun dataToJson(value: VideoDataModel): String{
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToData(value: String): VideoDataModel {
        return Gson().fromJson(value, VideoDataModel::class.java)
    }
}