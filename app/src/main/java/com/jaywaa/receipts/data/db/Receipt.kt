package com.jaywaa.receipts.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val amount: Double,
    val date: Long,
    val note: String? = null,
    val sent: Boolean = false,
    val sentAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
