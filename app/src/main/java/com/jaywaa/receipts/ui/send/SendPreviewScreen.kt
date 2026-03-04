package com.jaywaa.receipts.ui.send

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jaywaa.receipts.data.db.Receipt
import com.jaywaa.receipts.ui.home.formatDate
import com.jaywaa.receipts.ui.home.formatZar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPreviewScreen(
    context: Context,
    onNavigateBack: () -> Unit,
    viewModel: SendViewModel = viewModel()
) {
    val unsent by viewModel.unsentReceipts.collectAsState()
    val excludedIds by viewModel.excludedIds.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val selected = unsent.filter { it.id !in excludedIds }
    val totalAmount = selected.sumOf { it.amount }

    val emailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onEmailSent()
    }

    LaunchedEffect(uiState.emailIntent) {
        uiState.emailIntent?.let { intent ->
            try {
                emailLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                viewModel.clearEmailIntent()
            }
        }
    }

    LaunchedEffect(uiState.markedSent) {
        if (uiState.markedSent) onNavigateBack()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SummaryCard(
                count = selected.size,
                total = totalAmount,
                receipts = selected
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(unsent, key = { it.id }) { receipt ->
                    ReceiptCheckItem(
                        receipt = receipt,
                        checked = receipt.id !in excludedIds,
                        onCheckedChange = { viewModel.toggleReceipt(receipt.id) }
                    )
                }
            }

            Button(
                onClick = { viewModel.generateAndSend() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selected.isNotEmpty() && !uiState.isGenerating
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating PDF...")
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Send")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(count: Int, total: Double, receipts: List<Receipt>) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val dateRange = if (receipts.isNotEmpty()) {
        val dates = receipts.map { it.date }
        "${dateFormat.format(Date(dates.min()))} - ${dateFormat.format(Date(dates.max()))}"
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatZar(total),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count receipt${if (count != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (dateRange.isNotEmpty()) {
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ReceiptCheckItem(
    receipt: Receipt,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = { onCheckedChange() })
            AsyncImage(
                model = File(receipt.photoPath),
                contentDescription = "Receipt",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(receipt.date),
                    style = MaterialTheme.typography.bodyMedium
                )
                receipt.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = formatZar(receipt.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
