package com.myFile.transpose.database

import androidx.room.*
import com.myFile.transpose.retrofit.VideoData

@Entity
data class CashedKeyword(
    @PrimaryKey val searchKeyword: String,
    @ColumnInfo val savedTime: Long
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CashedKeyword::class,
            parentColumns = arrayOf("searchKeyword"),
            childColumns = arrayOf("keyWord"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class YoutubeCashedData(
    @PrimaryKey(autoGenerate = true) val dataId: Int,
    val searchVideoData: VideoData,
    val keyWord: String
)

@Dao
interface YoutubeCashedDataDao{
    @Query("SELECT * FROM YoutubeCashedData WHERE keyWord = (:searchKeyword)")
    fun getAllCashedDataBySearchKeyword(searchKeyword: String): List<YoutubeCashedData>?

    @Query("SELECT * FROM CashedKeyword WHERE searchKeyword = (:searchKeyword)")
    fun getCashedKeywordDataBySearchKeyword(searchKeyword: String): CashedKeyword?


    @Insert (onConflict = OnConflictStrategy.IGNORE)
    fun insertCashedData(vararg youtubeCashedData: YoutubeCashedData)


    @Insert (onConflict = OnConflictStrategy.IGNORE)
    fun insertKeyword(vararg cashedKeyword: CashedKeyword)

    @Transaction
    fun insertData(searchKeyword: String, cashedKeyword: CashedKeyword, youtubeCashedDataList: List<YoutubeCashedData>) {
        deleteAllBySearchKeyword(searchKeyword)
        insertKeyword(cashedKeyword)
        youtubeCashedDataList.forEach { youtubeCashedData ->
            insertCashedData(youtubeCashedData)
        }
    }

    @Query("DELETE FROM CashedKeyword WHERE searchKeyword = (:searchKeyword)")
    fun deleteAllBySearchKeyword(searchKeyword: String)

}
