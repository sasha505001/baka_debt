package com.example.sporttracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sporttracker.data.model.Exercise
import com.example.sporttracker.data.db.dao.ExerciseDao

@Database(
    entities = [Exercise::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}
