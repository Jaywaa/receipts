package com.jaywaa.receipts.ui.addreceipt

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaywaa.receipts.data.db.Receipt
import com.jaywaa.receipts.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddReceiptUiState(
    val photoPath: String? = null,
    val amount: String = "",
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class AddReceiptViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReceiptRepository(application)

    private val _uiState = MutableStateFlow(AddReceiptUiState())
    val uiState: StateFlow<AddReceiptUiState> = _uiState.asStateFlow()

    fun onPhotoFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val path = repository.savePhoto(uri)
                _uiState.value = _uiState.value.copy(photoPath = path, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save photo")
            }
        }
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val path = repository.saveBitmap(bitmap)
                _uiState.value = _uiState.value.copy(photoPath = path, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save photo")
            }
        }
    }

    fun clearPhoto() {
        _uiState.value = _uiState.value.copy(photoPath = null)
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onDateChanged(date: Long) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onNoteChanged(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun save() {
        val state = _uiState.value
        val photoPath = state.photoPath
        val amount = state.amount.toDoubleOrNull()

        if (photoPath == null) {
            _uiState.value = state.copy(error = "Please take or select a photo")
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(error = "Please enter a valid amount")
            return
        }

        _uiState.value = state.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                repository.insert(
                    Receipt(
                        photoPath = photoPath,
                        amount = amount,
                        date = state.date,
                        note = state.note.ifBlank { null }
                    )
                )
                _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = "Failed to save receipt")
            }
        }
    }
}
