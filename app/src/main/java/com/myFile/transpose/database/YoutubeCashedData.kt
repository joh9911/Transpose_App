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
data class PageToken(
    @PrimaryKey(autoGenerate = true) val tokenId: Int,
    val nextPageToken: String,
    val keyWord: String
)


@Dao
interface YoutubeCashedDataDao{
    @Query("SELECT * FROM YoutubeCashedData WHERE keyWord = (:searchKeyword)")
    fun getAllCashedDataBySearchKeyword(searchKeyword: String): List<YoutubeCashedData>?

    @Query("SELECT * FROM CashedKeyword WHERE searchKeyword = (:searchKeyword)")
    fun getCashedKeywordDataBySearchKeyword(searchKeyword: String): CashedKeyword?

    @Query("SELECT * FROM PageToken WHERE keyWord = (:searchKeyword)")
    fun getPageTokenBySearchKeyword(searchKeyword: String): PageToken?


    @Insert (onConflict = OnConflictStrategy.IGNORE)
    fun insertCashedData(vararg youtubeCashedData: YoutubeCashedData)

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    fun insertKeyword(vararg cashedKeyword: CashedKeyword)

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    fun insertPageToken(vararg pageToken: PageToken)

    @Transaction
    fun insertData(searchKeyword: String, cashedKeyword: CashedKeyword, youtubeCashedDataList: List<YoutubeCashedData>, pageToken: PageToken) {
        deleteAllBySearchKeyword(searchKeyword)
        insertKeyword(cashedKeyword)
        youtubeCashedDataList.forEach { youtubeCashedData ->
            insertCashedData(youtubeCashedData)
        }
        insertPageToken(pageToken)
    }

    @Query("DELETE FROM CashedKeyword WHERE searchKeyword = (:searchKeyword)")
    fun deleteAllBySearchKeyword(searchKeyword: String)

}
