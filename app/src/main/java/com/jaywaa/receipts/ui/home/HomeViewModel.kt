package com.jaywaa.receipts.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaywaa.receipts.data.db.Receipt
import com.jaywaa.receipts.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReceiptRepository(application)

    val unsentReceipts: StateFlow<List<Receipt>> = repository.getUnsentReceipts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sentReceipts: StateFlow<List<Receipt>> = repository.getSentReceipts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val isSelecting: Boolean get() = _selectedIds.value.isNotEmpty()

    private val _expandedBatches = MutableStateFlow<Set<Long>>(emptySet())
    val expandedBatches: StateFlow<Set<Long>> = _expandedBatches.asStateFlow()

    fun toggleBatchExpanded(sentAt: Long) {
        _expandedBatches.value = _expandedBatches.value.toMutableSet().apply {
            if (contains(sentAt)) remove(sentAt) else add(sentAt)
        }
    }

    fun toggleSelection(id: Long) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun toggleSelectAll(ids: List<Long>) {
        _selectedIds.value = if (_selectedIds.value.containsAll(ids)) emptySet() else ids.toSet()
    }

    fun markSelectedAsSent() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.markAsSent(ids)
            _selectedIds.value = emptySet()
        }
    }

    fun markSelectedAsUnsent() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.markAsUnsent(ids)
            _selectedIds.value = emptySet()
        }
    }

    private val _pendingDelete = MutableStateFlow<Receipt?>(null)
    val pendingDelete: StateFlow<Receipt?> = _pendingDelete.asStateFlow()

    fun deleteReceipt(receipt: Receipt) {
        _pendingDelete.value = receipt
    }

    fun confirmDelete() {
        val receipt = _pendingDelete.value ?: return
        viewModelScope.launch {
            repository.delete(receipt)
            _pendingDelete.value = null
        }
    }

    fun undoDelete() {
        _pendingDelete.value = null
    }
}
