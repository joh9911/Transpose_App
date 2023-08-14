package com.myFile.transpose.database

import androidx.room.*
import com.myFile.transpose.model.model.VideoDataModel


@Entity
data class MyPlaylist(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "playlist_title") val playlistTitle: String?
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = MyPlaylist::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("playlistId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Musics(
    @PrimaryKey(autoGenerate = true) val musicId: Int,
    val musicData: VideoDataModel,
    val playlistId: Int
)


@Dao
interface MyPlaylistDao{
    @Query("SELECT * FROM MyPlaylist")
    suspend fun getAllPlaylist(): List<MyPlaylist>

    @Query("SELECT * FROM MyPlaylist WHERE uid = (:position)")
    suspend fun getPlaylistItemByPosition(position: Int): List<MyPlaylist>

    @Insert
    suspend fun addPlaylist(vararg myPlaylist: MyPlaylist)

    @Delete
    suspend fun deletePlaylist(myPlaylist: MyPlaylist)

    @Query("DELETE FROM MyPlaylist")
    suspend fun deleteAllPlaylists()

    @Query("SELECT * FROM Musics WHERE playlistId = (:playlistId)")
    suspend fun getPlaylistItemByPlaylistId(playlistId: Int): List<Musics>

    @Query("DELETE FROM musics")
    suspend fun deleteAllMusicItems()

    @Insert
    suspend fun addMusicItem(vararg musics: Musics)

    @Delete
    suspend fun deleteMusicItem(musics: Musics)
}




