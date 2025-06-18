package com.example.sporttracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sporttracker.data.model.Exercise
import com.example.sporttracker.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository
) : ViewModel() {

    val exercises: StateFlow<List<Exercise>> =
        repository.getAllExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(exercise: Exercise) {
        viewModelScope.launch {
            repository.insertExercise(exercise)
        }
    }

    fun update(exercise: Exercise) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    fun delete(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }
}
