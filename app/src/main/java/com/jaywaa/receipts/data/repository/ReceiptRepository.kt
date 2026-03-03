package com.jaywaa.receipts.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.jaywaa.receipts.data.db.AppDatabase
import com.jaywaa.receipts.data.db.Receipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ReceiptRepository(private val context: Context) {
    private val dao = AppDatabase.getInstance(context).receiptDao()

    fun getUnsentReceipts(): Flow<List<Receipt>> = dao.getUnsentReceipts()

    fun getSentReceipts(): Flow<List<Receipt>> = dao.getSentReceipts()

    fun getUnsentCount(): Flow<Int> = dao.getUnsentCount()

    suspend fun getById(id: Long): Receipt? = dao.getById(id)

    suspend fun insert(receipt: Receipt): Long = dao.insert(receipt)

    suspend fun update(receipt: Receipt) = dao.update(receipt)

    suspend fun delete(receipt: Receipt) {
        File(receipt.photoPath).delete()
        dao.delete(receipt)
    }

    suspend fun markAsSent(ids: List<Long>) = dao.markAsSent(ids)

    suspend fun markAsUnsent(ids: List<Long>) = dao.markAsUnsent(ids)

    suspend fun savePhoto(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        saveBitmap(bitmap)
    }

    suspend fun saveBitmap(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val filename = "receipt_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    }
}
