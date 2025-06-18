package com.example.sporttracker.data.repository

import com.example.sporttracker.data.db.dao.ExerciseDao
import com.example.sporttracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao
) {
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun getExerciseById(id: Int): Flow<Exercise?> =
        exerciseDao.getExerciseById(id)

    suspend fun insertExercise(exercise: Exercise) = exerciseDao.insertExercise(exercise)

    suspend fun updateExercise(exercise: Exercise) = exerciseDao.updateExercise(exercise)

    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.deleteExercise(exercise)
}
