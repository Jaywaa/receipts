package com.jaywaa.receipts.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val toEmail: String = "",
    val ccEmail: String = "",
    val subjectTemplate: String = "Parking Receipts {date_range}",
    val fridayReminderEnabled: Boolean = true,
    val reminderHour: Int = 17,
    val reminderMinute: Int = 0
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val TO_EMAIL = stringPreferencesKey("to_email")
        val CC_EMAIL = stringPreferencesKey("cc_email")
        val SUBJECT_TEMPLATE = stringPreferencesKey("subject_template")
        val FRIDAY_REMINDER = booleanPreferencesKey("friday_reminder")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            toEmail = prefs[Keys.TO_EMAIL] ?: "",
            ccEmail = prefs[Keys.CC_EMAIL] ?: "",
            subjectTemplate = prefs[Keys.SUBJECT_TEMPLATE] ?: "Parking Receipts {date_range}",
            fridayReminderEnabled = prefs[Keys.FRIDAY_REMINDER] ?: true,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 17,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun updateToEmail(email: String) {
        context.dataStore.edit { it[Keys.TO_EMAIL] = email }
    }

    suspend fun updateCcEmail(email: String) {
        context.dataStore.edit { it[Keys.CC_EMAIL] = email }
    }

    suspend fun updateSubjectTemplate(template: String) {
        context.dataStore.edit { it[Keys.SUBJECT_TEMPLATE] = template }
    }

    suspend fun updateFridayReminder(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FRIDAY_REMINDER] = enabled }
    }

    suspend fun updateReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }
}
