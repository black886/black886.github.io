package com.cutwa.videoeditor.di

import android.content.Context
import com.cutwa.videoeditor.data.database.AppDatabase
import com.cutwa.videoeditor.data.database.VideoProjectDao
import com.cutwa.videoeditor.util.VideoUtils
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideVideoProjectDao(database: AppDatabase): VideoProjectDao {
        return database.videoProjectDao()
    }

    @Provides
    @Singleton
    fun provideVideoUtils(@ApplicationContext context: Context): VideoUtils {
        return VideoUtils(context)
    }
}