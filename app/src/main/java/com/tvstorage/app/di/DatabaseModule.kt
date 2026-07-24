package com.tvstorage.app.di

import android.content.Context
import androidx.room.Room
import com.tvstorage.app.data.dao.TelevisionDao
import com.tvstorage.app.data.database.TVStorageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TVStorageDatabase {
        return Room.databaseBuilder(
            context,
            TVStorageDatabase::class.java,
            "tvstorage_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTelevisionDao(database: TVStorageDatabase): TelevisionDao {
        return database.televisionDao()
    }
}