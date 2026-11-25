package com.cleanesia.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cleanesia.CleanesiaApp
import com.cleanesia.data.local.CellEntity
import com.cleanesia.data.repository.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PetaViewModel(repository: ReportRepository) : ViewModel() {

    val cells: StateFlow<List<CellEntity>> = repository.cells
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PetaViewModel((this[APPLICATION_KEY] as CleanesiaApp).reportRepository)
            }
        }
    }
}
