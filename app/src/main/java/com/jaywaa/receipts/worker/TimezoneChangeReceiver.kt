package com.jaywaa.receipts.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jaywaa.receipts.data.preferences.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TimezoneChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_TIMEZONE_CHANGED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsDataStore(context).settings.first()
                if (settings.fridayReminderEnabled) {
                    ReminderWorker.schedule(context, settings.reminderHour, settings.reminderMinute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
