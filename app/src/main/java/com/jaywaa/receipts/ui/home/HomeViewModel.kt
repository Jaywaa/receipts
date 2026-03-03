package com.jaywaa.receipts.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaywaa.receipts.data.db.Receipt
import com.jaywaa.receipts.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReceiptRepository(application)

    val unsentReceipts: StateFlow<List<Receipt>> = repository.getUnsentReceipts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sentReceipts: StateFlow<List<Receipt>> = repository.getSentReceipts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            repository.delete(receipt)
        }
    }
}
