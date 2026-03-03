package com.jaywaa.receipts.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object AddReceipt

@Serializable
data class ReceiptDetail(val receiptId: Long)

@Serializable
object SendPreview

@Serializable
object Settings
