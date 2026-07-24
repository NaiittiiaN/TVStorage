package com.tvstorage.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tvstorage.app.data.dao.TelevisionDao
import com.tvstorage.app.data.entity.TelevisionEntity

@Database(entities = [TelevisionEntity::class], version = 1, exportSchema = false)
abstract class TVStorageDatabase : RoomDatabase() {

    abstract fun televisionDao(): TelevisionDao

    companion object {
        @Volatile
        private var INSTANCE: TVStorageDatabase? = null

        fun getDatabase(context: Context): TVStorageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TVStorageDatabase::class.java,
                    "tvstorage_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}