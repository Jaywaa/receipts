package com.jaywaa.receipts.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaywaa.receipts.data.preferences.AppSettings
import com.jaywaa.receipts.data.preferences.SettingsDataStore
import com.jaywaa.receipts.worker.ReminderWorker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)
    private val app = application

    val settings: StateFlow<AppSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _toEmail = MutableStateFlow("")
    val toEmail: StateFlow<String> = _toEmail.asStateFlow()

    private val _ccEmail = MutableStateFlow("")
    val ccEmail: StateFlow<String> = _ccEmail.asStateFlow()

    private val _subjectTemplate = MutableStateFlow("")
    val subjectTemplate: StateFlow<String> = _subjectTemplate.asStateFlow()

    private val _pdfFilenameTemplate = MutableStateFlow("")
    val pdfFilenameTemplate: StateFlow<String> = _pdfFilenameTemplate.asStateFlow()

    private var seeded = false

    init {
        viewModelScope.launch {
            val initial = settingsDataStore.settings.first()
            _toEmail.value = initial.toEmail
            _ccEmail.value = initial.ccEmail
            _subjectTemplate.value = initial.subjectTemplate
            _pdfFilenameTemplate.value = initial.pdfFilenameTemplate
            seeded = true
        }

        _toEmail.drop(1).debounce(500).onEach { if (seeded) settingsDataStore.updateToEmail(it) }.launchIn(viewModelScope)
        _ccEmail.drop(1).debounce(500).onEach { if (seeded) settingsDataStore.updateCcEmail(it) }.launchIn(viewModelScope)
        _subjectTemplate.drop(1).debounce(500).onEach { if (seeded) settingsDataStore.updateSubjectTemplate(it) }.launchIn(viewModelScope)
        _pdfFilenameTemplate.drop(1).debounce(500).onEach { if (seeded) settingsDataStore.updatePdfFilenameTemplate(it) }.launchIn(viewModelScope)
    }

    fun updateToEmail(email: String) {
        _toEmail.value = email
    }

    fun updateCcEmail(email: String) {
        _ccEmail.value = email
    }

    fun updateSubjectTemplate(template: String) {
        _subjectTemplate.value = template
    }

    fun updatePdfFilenameTemplate(template: String) {
        _pdfFilenameTemplate.value = template
    }

    fun updateFridayReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateFridayReminder(enabled)
            if (enabled) {
                val s = settings.value
                ReminderWorker.schedule(app, s.reminderHour, s.reminderMinute)
            } else {
                ReminderWorker.cancel(app)
            }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsDataStore.updateReminderTime(hour, minute)
            if (settings.value.fridayReminderEnabled) {
                ReminderWorker.schedule(app, hour, minute)
            }
        }
    }

    fun updateAutoMarkAsSent(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateAutoMarkAsSent(enabled)
        }
    }
}
