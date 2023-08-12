package com.myFile.transpose.database

import androidx.room.*
import com.myFile.transpose.model.VideoDataModel

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
    val searchVideoData: VideoDataModel,
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
    val nextPageToken: String?,
    val keyWord: String
)


@Dao
interface YoutubeCashedDataDao{
    @Query("SELECT * FROM YoutubeCashedData WHERE keyWord = (:searchKeyword)")
    suspend fun getAllCashedDataBySearchKeyword(searchKeyword: String): List<YoutubeCashedData>?

    @Query("SELECT * FROM CashedKeyword WHERE searchKeyword = (:searchKeyword)")
    suspend fun getCashedKeywordDataBySearchKeyword(searchKeyword: String): CashedKeyword?

    @Query("SELECT * FROM PageToken WHERE keyWord = (:searchKeyword)")
    suspend fun getPageTokenBySearchKeyword(searchKeyword: String): PageToken?


    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCashedData(vararg youtubeCashedData: YoutubeCashedData)

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertKeyword(vararg cashedKeyword: CashedKeyword)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPageToken(vararg pageToken: PageToken)

    @Transaction
    suspend fun insertData(searchKeyword: String, cashedKeyword: CashedKeyword, youtubeCashedDataList: List<YoutubeCashedData>, pageToken: PageToken) {
        deleteAllBySearchKeyword(searchKeyword)
        insertKeyword(cashedKeyword)
        youtubeCashedDataList.forEach { youtubeCashedData ->
            insertCashedData(youtubeCashedData)
        }
        insertPageToken(pageToken)
    }

    @Query("DELETE FROM CashedKeyword WHERE searchKeyword = (:searchKeyword)")
    suspend fun deleteAllBySearchKeyword(searchKeyword: String)

    @Query("DELETE FROM CashedKeyword WHERE savedTime < :thirtyDaysAgo")
    suspend fun deleteOldCashedData(thirtyDaysAgo: Long)

}
