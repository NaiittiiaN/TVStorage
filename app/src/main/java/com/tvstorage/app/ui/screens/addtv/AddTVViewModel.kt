package com.tvstorage.app.ui.screens.addtv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvstorage.app.data.entity.TelevisionEntity
import com.tvstorage.app.data.repository.TelevisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTVUiState(
    val brand: String = "",
    val model: String = "",
    val clientName: String = "",
    val orderNumber: String = "",
    val phoneNumber: String = "",
    val notes: String = "",
    val dailyCost: String = "100",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val orderNumberError: String? = null,
    val customReceivedDate: Long? = null
)

@HiltViewModel
class AddTVViewModel @Inject constructor(
    private val repository: TelevisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTVUiState())
    val uiState: StateFlow<AddTVUiState> = _uiState

    private var editingTvId: Long? = null

    fun loadTelevision(tvId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val tv = repository.getById(tvId)
            if (tv != null) {
                editingTvId = tv.id
                _uiState.value = AddTVUiState(
                    brand = tv.brand,
                    model = tv.model,
                    clientName = tv.clientName,
                    orderNumber = tv.orderNumber,
                    phoneNumber = tv.phoneNumber,
                    notes = tv.notes,
                    dailyCost = tv.dailyCost.toLong().toString(),
                    isEditing = true,
                    isLoading = false,
                    customReceivedDate = tv.receivedDate
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onBrandChange(value: String) { _uiState.value = _uiState.value.copy(brand = value) }
    fun onModelChange(value: String) { _uiState.value = _uiState.value.copy(model = value) }
    fun onClientNameChange(value: String) { _uiState.value = _uiState.value.copy(clientName = value) }
    fun onOrderNumberChange(value: String) { 
        _uiState.value = _uiState.value.copy(orderNumber = value, orderNumberError = null) 
    }
    fun onPhoneNumberChange(value: String) { _uiState.value = _uiState.value.copy(phoneNumber = value) }
    fun onNotesChange(value: String) { _uiState.value = _uiState.value.copy(notes = value) }
    fun onDailyCostChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() || it == '.' }) {
            _uiState.value = _uiState.value.copy(dailyCost = value)
        }
    }
    
    fun onDateChange(timestamp: Long?) {
        _uiState.value = _uiState.value.copy(customReceivedDate = timestamp)
    }

    fun save() {
        val state = _uiState.value
        if (state.orderNumber.isBlank()) return

        viewModelScope.launch {
            val existingWithNumber = repository.getByOrderNumber(state.orderNumber)
            if (existingWithNumber != null && (!state.isEditing || existingWithNumber.id != editingTvId)) {
                _uiState.value = state.copy(orderNumberError = "Устройство с таким S/N уже существует")
                return@launch
            }

            _uiState.value = state.copy(isSaving = true)
            val dailyCost = state.dailyCost.toDoubleOrNull() ?: 100.0
            
            val receivedDate = if (state.customReceivedDate != null) {
                state.customReceivedDate
            } else if (state.isEditing && editingTvId != null) {
                repository.getById(editingTvId!!)?.receivedDate ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }

            if (state.isEditing && editingTvId != null) {
                val existing = repository.getById(editingTvId!!)
                if (existing != null) {
                    repository.update(
                        existing.copy(
                            brand = state.brand,
                            model = state.model,
                            clientName = state.clientName,
                            orderNumber = state.orderNumber,
                            phoneNumber = state.phoneNumber,
                            notes = state.notes,
                            dailyCost = dailyCost,
                            receivedDate = receivedDate
                        )
                    )
                }
            } else {
                repository.insert(
                    TelevisionEntity(
                        brand = state.brand,
                        model = state.model,
                        clientName = state.clientName,
                        orderNumber = state.orderNumber,
                        phoneNumber = state.phoneNumber,
                        notes = state.notes,
                        dailyCost = dailyCost,
                        receivedDate = receivedDate
                    )
                )
            }
            _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
        }
    }
}
