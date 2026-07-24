package com.tvstorage.app.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvstorage.app.data.entity.TelevisionEntity
import com.tvstorage.app.data.repository.TelevisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: TelevisionRepository
) : ViewModel() {

    val archivedTelevisions: StateFlow<List<TelevisionEntity>> = repository.getAllArchived()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restore(tv: TelevisionEntity) {
        viewModelScope.launch {
            repository.update(tv.copy(isArchived = false))
        }
    }

    fun delete(tv: TelevisionEntity) {
        viewModelScope.launch {
            repository.delete(tv)
        }
    }
}