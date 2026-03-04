package com.jaywaa.receipts.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.jaywaa.receipts.data.db.Receipt
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmailIntentBuilder(private val context: Context) {

    fun buildEmailIntent(
        pdfFile: File,
        receipts: List<Receipt>,
        toEmail: String,
        ccEmail: String,
        subjectTemplate: String
    ): Intent {
        val totalAmount = receipts.sumOf { it.amount }
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dates = receipts.map { it.date }
        val dateRange = "${dateFormat.format(Date(dates.min()))} - ${dateFormat.format(Date(dates.max()))}"

        val subject = subjectTemplate
            .replace("{date_range}", dateRange)
            .replace("{total}", "R${"%.2f".format(totalAmount)}")
            .replace("{count}", receipts.size.toString())

        val body = buildString {
            appendLine("Parking Receipts Summary")
            appendLine("========================")
            appendLine()
            appendLine("Period: $dateRange")
            appendLine("Total: R${"%.2f".format(totalAmount)}")
            appendLine("Receipts: ${receipts.size}")
            appendLine()
            appendLine("Breakdown:")
            for (receipt in receipts.sortedBy { it.date }) {
                val date = dateFormat.format(Date(receipt.date))
                val note = receipt.note?.let { " ($it)" } ?: ""
                appendLine("  $date: R${"%.2f".format(receipt.amount)}$note")
            }
            appendLine()
            appendLine("PDF with receipt photos is attached.")
        }

        val pdfUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            if (ccEmail.isNotBlank()) {
                putExtra(Intent.EXTRA_CC, arrayOf(ccEmail))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
