package com.jaywaa.receipts.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts WHERE sent = 0 ORDER BY date DESC")
    fun getUnsentReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE sent = 1 ORDER BY sentAt DESC")
    fun getSentReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getById(id: Long): Receipt?

    @Query("SELECT COUNT(*) FROM receipts WHERE sent = 0")
    fun getUnsentCount(): Flow<Int>

    @Insert
    suspend fun insert(receipt: Receipt): Long

    @Update
    suspend fun update(receipt: Receipt)

    @Delete
    suspend fun delete(receipt: Receipt)

    @Query("UPDATE receipts SET sent = 1, sentAt = :sentAt WHERE id IN (:ids)")
    suspend fun markAsSent(ids: List<Long>, sentAt: Long = System.currentTimeMillis())
}
