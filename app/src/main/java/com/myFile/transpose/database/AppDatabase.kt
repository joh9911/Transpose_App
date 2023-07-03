package com.myFile.transpose.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MyPlaylist::class, Musics::class,CashedKeyword::class, YoutubeCashedData::class, PageToken::class], version = 6)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun myPlaylistDao(): MyPlaylistDao
    abstract fun youtubeCashedDataDao(): YoutubeCashedDataDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `PageToken` (`tokenId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nextPageToken` TEXT, `keyWord` TEXT, " +
                            "FOREIGN KEY(`keyWord`) REFERENCES `CashedKeyword`(`searchKeyword`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
            }
        }
        fun getDatabase(context: Context): AppDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database-name"
                ).addMigrations(MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}


val migration_3_4 = object: Migration(3,4){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLe CashedKeyword(searchKeyword TEXT PRIMARY KEY NOT NULL," +
                "savedTime INTEGER NOT NULL)")

        database.execSQL("CREATE TABLE YoutubeCashedData (dataId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "searchVideoData TEXT NOT NULL," +
                "keyWord TEXT NOT NULL," +
                "FOREIGN KEY(keyWord) REFERENCES CashedKeyword(searchKeyword) ON DELETE CASCADE)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Token 테이블 생성
        database.execSQL(
            "CREATE TABLE `PageToken` (`tokenId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nextPageToken` TEXT, `keyWord` TEXT, " +
                    "FOREIGN KEY(`keyWord`) REFERENCES `CashedKeyword`(`searchKeyword`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
    }
}

