package com.example.sporttracker.data.db.di

import android.content.Context
import androidx.room.Room
import com.example.sporttracker.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.sporttracker.data.db.dao.SupplementDao
import com.example.sporttracker.data.db.dao.ExerciseDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "sporttracker.db"
        ).build()
    }

    @Provides
    fun provideExerciseDao(db: AppDatabase): ExerciseDao {
        return db.exerciseDao()
    }

    @Provides
    fun provideSupplementDao(database: AppDatabase): SupplementDao {
        return database.supplementDao()
    }
}
