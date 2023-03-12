package com.myFile.Transpose

import androidx.room.*

@Entity
data class MyPlaylist(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "playlist_title") val playlistTitle: String?
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
}

@Database(entities = [MyPlaylist::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun myPlaylistDao(): MyPlaylistDao
}
