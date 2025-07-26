package com.cutwa.videoeditor.di

import android.content.Context
import androidx.room.Room
import com.cutwa.videoeditor.data.database.ProjectDao
import com.cutwa.videoeditor.data.database.ProjectDatabase
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
    fun provideProjectDatabase(@ApplicationContext context: Context): ProjectDatabase {
        return Room.databaseBuilder(
            context,
            ProjectDatabase::class.java,
            "cutwa_database"
        ).build()
    }
    
    @Provides
    fun provideProjectDao(database: ProjectDatabase): ProjectDao {
        return database.projectDao()
    }
}