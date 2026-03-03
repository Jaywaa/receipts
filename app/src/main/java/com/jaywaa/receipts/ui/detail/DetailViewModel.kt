package com.jaywaa.receipts.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaywaa.receipts.data.db.Receipt
import com.jaywaa.receipts.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val receipt: Receipt? = null,
    val isEditing: Boolean = false,
    val editAmount: String = "",
    val editNote: String = "",
    val editDate: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReceiptRepository(application)

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadReceipt(id: Long) {
        viewModelScope.launch {
            val receipt = repository.getById(id)
            receipt?.let {
                _uiState.value = DetailUiState(
                    receipt = it,
                    editAmount = it.amount.toString(),
                    editNote = it.note ?: "",
                    editDate = it.date
                )
            }
        }
    }

    fun startEditing() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    fun cancelEditing() {
        val receipt = _uiState.value.receipt ?: return
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            editAmount = receipt.amount.toString(),
            editNote = receipt.note ?: "",
            editDate = receipt.date
        )
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(editAmount = amount)
    }

    fun onNoteChanged(note: String) {
        _uiState.value = _uiState.value.copy(editNote = note)
    }

    fun onDateChanged(date: Long) {
        _uiState.value = _uiState.value.copy(editDate = date)
    }

    fun saveEdit() {
        val state = _uiState.value
        val receipt = state.receipt ?: return
        val amount = state.editAmount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            val updated = receipt.copy(
                amount = amount,
                note = state.editNote.ifBlank { null },
                date = state.editDate
            )
            repository.update(updated)
            _uiState.value = state.copy(receipt = updated, isEditing = false)
        }
    }

    fun delete() {
        val receipt = _uiState.value.receipt ?: return
        viewModelScope.launch {
            repository.delete(receipt)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }

    fun toggleSentStatus() {
        val receipt = _uiState.value.receipt ?: return
        viewModelScope.launch {
            if (receipt.sent) {
                repository.markAsUnsent(listOf(receipt.id))
                _uiState.value = _uiState.value.copy(
                    receipt = receipt.copy(sent = false, sentAt = null)
                )
            } else {
                repository.markAsSent(listOf(receipt.id))
                _uiState.value = _uiState.value.copy(
                    receipt = receipt.copy(sent = true, sentAt = System.currentTimeMillis())
                )
            }
        }
    }
}
