package com.example.sporttracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sporttracker.data.model.Exercise
import com.example.sporttracker.data.model.Supplement
import com.example.sporttracker.data.db.dao.ExerciseDao
import com.example.sporttracker.data.db.dao.SupplementDao

@Database(
    entities = [
        Exercise::class,
        Supplement::class
               ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun supplementDao(): SupplementDao

}
