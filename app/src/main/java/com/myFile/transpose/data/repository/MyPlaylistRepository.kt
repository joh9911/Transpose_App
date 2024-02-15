package com.myFile.transpose.data.repository

import com.myFile.transpose.database.Musics
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.database.MyPlaylistDao


class MyPlaylistRepository(private val myPlaylistDao: MyPlaylistDao) {

    suspend fun getAllPlaylist(): List<MyPlaylist>{
        return myPlaylistDao.getAllPlaylist()
    }

    suspend fun addPlaylist(myPlaylist: MyPlaylist){
        myPlaylistDao.addPlaylist(myPlaylist)
    }

    suspend fun deletePlaylist(myPlaylist: MyPlaylist){
        myPlaylistDao.deletePlaylist(myPlaylist)
    }

    suspend fun getPlaylistItemsByPlaylistId(playlistId: Int): List<Musics>{
        return myPlaylistDao.getPlaylistItemByPlaylistId(playlistId)
    }

    suspend fun addMusicItem(music: Musics){
        myPlaylistDao.addMusicItem(music)
    }

    suspend fun deleteMusicItem(music: Musics){
        myPlaylistDao.deleteMusicItem(music)
    }
}