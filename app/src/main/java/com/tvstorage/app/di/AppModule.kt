package com.tvstorage.app.di

import android.content.Context
import com.tvstorage.app.utils.ThemeStore
import com.tvstorage.app.utils.UpdateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideThemeStore(@ApplicationContext context: Context): ThemeStore {
        return ThemeStore(context)
    }

    @Provides
    @Singleton
    fun provideUpdateManager(@ApplicationContext context: Context): UpdateManager {
        return UpdateManager(context)
    }
}
