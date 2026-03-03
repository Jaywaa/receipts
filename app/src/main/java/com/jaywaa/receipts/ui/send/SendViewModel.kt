package com.jaywaa.receipts.ui.send

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaywaa.receipts.data.db.Receipt
import com.jaywaa.receipts.data.preferences.AppSettings
import com.jaywaa.receipts.data.preferences.SettingsDataStore
import com.jaywaa.receipts.data.repository.ReceiptRepository
import com.jaywaa.receipts.service.EmailIntentBuilder
import com.jaywaa.receipts.service.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SendUiState(
    val isGenerating: Boolean = false,
    val emailIntent: Intent? = null,
    val error: String? = null,
    val markedSent: Boolean = false
)

class SendViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReceiptRepository(application)
    private val settingsDataStore = SettingsDataStore(application)
    private val pdfGenerator = PdfGenerator(application)
    private val emailIntentBuilder = EmailIntentBuilder(application)

    val unsentReceipts: StateFlow<List<Receipt>> = repository.getUnsentReceipts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _excludedIds = MutableStateFlow<Set<Long>>(emptySet())
    val excludedIds: StateFlow<Set<Long>> = _excludedIds.asStateFlow()

    private val _uiState = MutableStateFlow(SendUiState())
    val uiState: StateFlow<SendUiState> = _uiState.asStateFlow()

    fun toggleReceipt(id: Long) {
        val current = _excludedIds.value
        _excludedIds.value = if (id in current) current - id else current + id
    }

    fun getSelectedReceipts(): List<Receipt> {
        return unsentReceipts.value.filter { it.id !in _excludedIds.value }
    }

    fun generateAndSend() {
        val selected = getSelectedReceipts()
        if (selected.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "No receipts selected")
            return
        }

        _uiState.value = _uiState.value.copy(isGenerating = true, error = null)
        viewModelScope.launch {
            try {
                val settings: AppSettings = settingsDataStore.settings.first()
                val pdfFile = pdfGenerator.generate(selected)
                val intent = emailIntentBuilder.buildEmailIntent(
                    pdfFile = pdfFile,
                    receipts = selected,
                    toEmail = settings.toEmail,
                    ccEmail = settings.ccEmail,
                    subjectTemplate = settings.subjectTemplate
                )
                _uiState.value = _uiState.value.copy(isGenerating = false, emailIntent = intent)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = "Failed to generate PDF: ${e.message}"
                )
            }
        }
    }

    fun onEmailSent() {
        val selected = getSelectedReceipts()
        viewModelScope.launch {
            repository.markAsSent(selected.map { it.id })
            _uiState.value = _uiState.value.copy(emailIntent = null, markedSent = true)
        }
    }

    fun clearEmailIntent() {
        _uiState.value = _uiState.value.copy(emailIntent = null)
    }
}
