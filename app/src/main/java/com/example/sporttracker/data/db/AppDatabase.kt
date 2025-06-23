package com.example.sporttracker.data.db

import android.content.Context
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
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun supplementDao(): SupplementDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
