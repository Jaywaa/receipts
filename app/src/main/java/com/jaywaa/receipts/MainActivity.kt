package com.jaywaa.receipts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jaywaa.receipts.data.preferences.SettingsDataStore
import com.jaywaa.receipts.ui.theme.ReceiptsTheme
import com.jaywaa.receipts.worker.ReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsIfNeeded()
        scheduleReminderIfNeeded()

        val sharedImageUri = extractSharedImageUri(intent)
        val startAtAddReceipt = intent?.action == ACTION_QUICK_ADD

        setContent {
            ReceiptsTheme {
                ReceiptsApp(
                    context = applicationContext,
                    sharedImageUri = sharedImageUri,
                    startAtAddReceipt = startAtAddReceipt
                )
            }
        }
    }

    companion object {
        const val ACTION_QUICK_ADD = "com.jaywaa.receipts.ACTION_QUICK_ADD"
    }

    private fun extractSharedImageUri(intent: Intent?): Uri? {
        if (intent?.action != Intent.ACTION_SEND || intent.type?.startsWith("image/") != true) {
            return null
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun scheduleReminderIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = SettingsDataStore(applicationContext).settings.first()
            if (settings.fridayReminderEnabled) {
                ReminderWorker.schedule(
                    applicationContext,
                    settings.reminderHour,
                    settings.reminderMinute
                )
            }
        }
    }
}
