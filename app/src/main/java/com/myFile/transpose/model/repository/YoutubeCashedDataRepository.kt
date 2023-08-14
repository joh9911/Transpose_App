package com.myFile.transpose.model.repository

import com.myFile.transpose.others.constants.TimeTarget
import com.myFile.transpose.database.CashedKeyword
import com.myFile.transpose.database.PageToken
import com.myFile.transpose.database.YoutubeCashedData
import com.myFile.transpose.database.YoutubeCashedDataDao

class YoutubeCashedDataRepository(private val youtubeCashedDataDao: YoutubeCashedDataDao) {

    suspend fun getYoutubeCashedDataFromDb(searchKeyword: String): List<YoutubeCashedData>? {
        return youtubeCashedDataDao.getAllCashedDataBySearchKeyword(searchKeyword)
    }

    suspend fun getPageTokenBySearchKeyword(searchKeyword: String): PageToken? {
        return youtubeCashedDataDao.getPageTokenBySearchKeyword(searchKeyword)
    }

    suspend fun getCashedKeywordDataBySearchKeyword(searchKeyword: String): CashedKeyword? {
        return youtubeCashedDataDao.getCashedKeywordDataBySearchKeyword(searchKeyword)
    }

    suspend fun cashData(searchKeyword: String, cashedKeyword: CashedKeyword, pageToken: PageToken, youtubeCashedDataList: List<YoutubeCashedData>){
        youtubeCashedDataDao.insertData(searchKeyword, cashedKeyword, youtubeCashedDataList, pageToken)
    }

    suspend fun deleteOldData(){
        youtubeCashedDataDao.deleteOldCashedData(System.currentTimeMillis() - TimeTarget.DATA_DELETE_TARGET_DURATION)
    }

}