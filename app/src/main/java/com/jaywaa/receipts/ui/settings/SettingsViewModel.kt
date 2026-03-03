package com.jaywaa.receipts.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaywaa.receipts.data.preferences.AppSettings
import com.jaywaa.receipts.data.preferences.SettingsDataStore
import com.jaywaa.receipts.worker.ReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)
    private val app = application

    val settings: StateFlow<AppSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun updateToEmail(email: String) {
        viewModelScope.launch { settingsDataStore.updateToEmail(email) }
    }

    fun updateCcEmail(email: String) {
        viewModelScope.launch { settingsDataStore.updateCcEmail(email) }
    }

    fun updateSubjectTemplate(template: String) {
        viewModelScope.launch { settingsDataStore.updateSubjectTemplate(template) }
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
}
