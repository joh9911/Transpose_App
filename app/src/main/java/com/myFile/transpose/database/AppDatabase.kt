package com.myFile.transpose.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MyPlaylist::class, Musics::class,CashedKeyword::class, YoutubeCashedData::class, PageToken::class], version = 8)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun myPlaylistDao(): MyPlaylistDao
    abstract fun youtubeCashedDataDao(): YoutubeCashedDataDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getDatabase(context: Context): AppDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database-name"
                )
                    .addMigrations(MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

// Musics 테이블에 AudioEfects 필드 추가
val MIGRATION_7_8 = object: Migration(7,8){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Musics ADD COLUMN audioEffects TEXT")
    }
}

// nextPageToken null 가능하게 바꾸기
val MIGRATION_6_7= object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new temporary table with the same structure but with nextPageToken as nullable
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `PageToken_new` (
                `tokenId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `nextPageToken` TEXT, 
                `keyWord` TEXT NOT NULL, 
                FOREIGN KEY(`keyWord`) REFERENCES `CashedKeyword`(`searchKeyword`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )""".trimIndent())

        // Copy the data
        database.execSQL("""
            INSERT INTO PageToken_new (tokenId, nextPageToken, keyWord)
            SELECT tokenId, nextPageToken, keyWord FROM PageToken""".trimIndent())

        // Remove the old table
        database.execSQL("DROP TABLE PageToken")

        // Rename the new table to the old one
        database.execSQL("ALTER TABLE PageToken_new RENAME TO PageToken")
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

private val MIGRATION_4_6 = object : Migration(4, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `PageToken` (`tokenId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nextPageToken` TEXT, `keyWord` TEXT, " +
                    "FOREIGN KEY(`keyWord`) REFERENCES `CashedKeyword`(`searchKeyword`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
    }
}


