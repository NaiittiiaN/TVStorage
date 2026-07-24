package com.tvstorage.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvstorage.app.data.entity.TelevisionEntity
import com.tvstorage.app.data.repository.TelevisionRepository
import com.tvstorage.app.utils.ThemeStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tvstorage.app.utils.DateUtils

enum class SortOrder {
    DATE_DESC, DATE_ASC, COST_DESC, COST_ASC
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TelevisionRepository,
    private val themeStore: ThemeStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _showWhatsNew = MutableStateFlow(false)
    val showWhatsNew: StateFlow<Boolean> = _showWhatsNew

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    val televisions: StateFlow<List<TelevisionEntity>> = combine(
        repository.getAllActive(),
        _searchQuery,
        _sortOrder
    ) { list, query, sort ->
        val filtered = if (query.isBlank()) list
        else list.filter {
            it.orderNumber.contains(query, ignoreCase = true) ||
                    it.brand.contains(query, ignoreCase = true) ||
                    it.clientName.contains(query, ignoreCase = true)
        }
        
        when (sort) {
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.receivedDate }
            SortOrder.DATE_ASC -> filtered.sortedBy { it.receivedDate }
            SortOrder.COST_DESC -> filtered.sortedByDescending { 
                DateUtils.calculateTotalCost(it.dailyCost, DateUtils.getDaysSince(it.receivedDate)) 
            }
            SortOrder.COST_ASC -> filtered.sortedBy { 
                DateUtils.calculateTotalCost(it.dailyCost, DateUtils.getDaysSince(it.receivedDate)) 
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = repository.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        checkVersion()
    }

    private fun checkVersion() {
        viewModelScope.launch {
            val currentVersion = "1.2.5"
            val lastVersion = themeStore.lastSeenVersion.first()
            if (lastVersion != currentVersion) {
                _showWhatsNew.value = true
            }
        }
    }

    fun dismissWhatsNew() {
        viewModelScope.launch {
            themeStore.setLastSeenVersion("1.2.5")
            _showWhatsNew.value = false
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }

    fun toggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            _searchQuery.value = ""
        }
    }

    fun deleteTelevision(television: TelevisionEntity) {
        viewModelScope.launch {
            repository.delete(television)
        }
    }

    fun togglePause(television: TelevisionEntity) {
        viewModelScope.launch {
            repository.update(television.copy(isPaused = !television.isPaused))
        }
    }

    fun archiveTelevision(television: TelevisionEntity) {
        viewModelScope.launch {
            repository.update(television.copy(isArchived = true))
        }
    }
}
