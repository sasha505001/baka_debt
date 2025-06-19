package com.example.sporttracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sporttracker.data.model.Supplement
import com.example.sporttracker.data.repository.SupplementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplementViewModel @Inject constructor(
    private val repository: SupplementRepository
) : ViewModel() {

    val supplements: StateFlow<List<Supplement>> = repository
        .getAllSupplements()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getSupplementById(id: Int): Flow<Supplement?> {
        return repository.getSupplementById(id)
    }

    fun insert(supplement: Supplement) {
        viewModelScope.launch {
            repository.insert(supplement)
        }
    }

    fun update(supplement: Supplement) {
        viewModelScope.launch {
            repository.update(supplement)
        }
    }

    fun delete(supplement: Supplement) {
        viewModelScope.launch {
            repository.delete(supplement)
        }
    }
}
