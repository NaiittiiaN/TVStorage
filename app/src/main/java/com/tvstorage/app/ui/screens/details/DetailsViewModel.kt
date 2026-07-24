package com.tvstorage.app.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvstorage.app.data.entity.TelevisionEntity
import com.tvstorage.app.data.repository.TelevisionRepository
import com.tvstorage.app.utils.ThemeStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: TelevisionRepository
) : ViewModel() {

    private val _television = MutableStateFlow<TelevisionEntity?>(null)
    val television: StateFlow<TelevisionEntity?> = _television

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _navigateBack = MutableStateFlow(false)
    val navigateBack: StateFlow<Boolean> = _navigateBack

    fun loadTelevision(tvId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getByIdFlow(tvId).collect { tv ->
                _television.value = tv
                _isLoading.value = false
            }
        }
    }

    fun togglePause() {
        val tv = _television.value ?: return
        viewModelScope.launch {
            repository.update(tv.copy(isPaused = !tv.isPaused))
        }
    }

    fun archive() {
        val tv = _television.value ?: return
        viewModelScope.launch {
            repository.update(tv.copy(isArchived = true))
            _navigateBack.value = true
        }
    }

    fun delete() {
        val tv = _television.value ?: return
        viewModelScope.launch {
            repository.delete(tv)
            _navigateBack.value = true
        }
    }
}
