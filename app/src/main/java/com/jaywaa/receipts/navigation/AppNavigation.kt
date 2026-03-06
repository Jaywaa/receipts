package com.jaywaa.receipts.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class AddReceipt(val sharedImageUri: String? = null)

@Serializable
data class ReceiptDetail(val receiptId: Long)

@Serializable
object SendPreview

@Serializable
object Settings
