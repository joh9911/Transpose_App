package com.myFile.transpose.database

import androidx.room.*
import com.myFile.transpose.retrofit.VideoData

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
    val musicData: VideoData,
    val playlistId: Int
)


@Dao
interface MyPlaylistDao{
    @Query("SELECT * FROM MyPlaylist")
    fun getAll(): List<MyPlaylist>

    @Query("SELECT * FROM MyPlaylist WHERE uid = (:position)")
    fun getPlaylistByPosition(position: Int): MyPlaylist

    @Insert
    fun insertAll(vararg myPlaylist: MyPlaylist)

    @Delete
    fun delete(myPlaylist: MyPlaylist)

    @Query("DELETE FROM MyPlaylist")
    fun deleteAll()

    @Query("SELECT * FROM Musics WHERE playlistId = (:playlistId)")
    fun getMusicItemsByPlaylistId(playlistId: Int): List<Musics>

    @Query("DELETE FROM musics")
    fun deleteMusicAll()

    @Insert
    fun insertMusic(vararg musics: Musics)

    @Delete
    fun deleteMusic(musics: Musics)
}

@Database(entities = [MyPlaylist::class, Musics::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun myPlaylistDao(): MyPlaylistDao
}

