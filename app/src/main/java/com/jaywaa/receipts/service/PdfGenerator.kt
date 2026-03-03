package com.jaywaa.receipts.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.jaywaa.receipts.data.db.Receipt
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {

    companion object {
        private const val A4_WIDTH = 595
        private const val A4_HEIGHT = 842
        private const val MARGIN = 40
        private const val MAX_PHOTO_PX = 1200
    }

    fun generate(receipts: List<Receipt>): File {
        val document = PdfDocument()
        val totalAmount = receipts.sumOf { it.amount }
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dates = receipts.map { it.date }
        val dateRange = "${dateFormat.format(Date(dates.min()))} - ${dateFormat.format(Date(dates.max()))}"

        val headerPaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val subHeaderPaint = Paint().apply {
            textSize = 16f
            isAntiAlias = true
        }
        val captionPaint = Paint().apply {
            textSize = 14f
            isAntiAlias = true
        }
        val captionBoldPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val summaryPage = document.startPage(
            PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        )
        drawSummaryPage(summaryPage.canvas, receipts, totalAmount, dateRange, headerPaint, subHeaderPaint, captionPaint)
        document.finishPage(summaryPage)

        receipts.forEachIndexed { index, receipt ->
            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, index + 2).create()
            val page = document.startPage(pageInfo)
            drawReceiptPage(page.canvas, receipt, index + 1, receipts.size, dateFormat, captionPaint, captionBoldPaint)
            document.finishPage(page)
        }

        val outputFile = File(context.cacheDir, "parking_receipts.pdf")
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        return outputFile
    }

    private fun drawSummaryPage(
        canvas: Canvas,
        receipts: List<Receipt>,
        totalAmount: Double,
        dateRange: String,
        headerPaint: Paint,
        subHeaderPaint: Paint,
        captionPaint: Paint
    ) {
        var y = MARGIN + 30f

        canvas.drawText("Parking Receipts", MARGIN.toFloat(), y, headerPaint)
        y += 30f
        canvas.drawText(dateRange, MARGIN.toFloat(), y, subHeaderPaint)
        y += 40f

        val linePaint = Paint().apply {
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN.toFloat(), y, (A4_WIDTH - MARGIN).toFloat(), y, linePaint)
        y += 25f

        canvas.drawText("Total: R${"%.2f".format(totalAmount)}", MARGIN.toFloat(), y, headerPaint)
        y += 30f
        canvas.drawText("${receipts.size} receipt${if (receipts.size != 1) "s" else ""}", MARGIN.toFloat(), y, subHeaderPaint)
        y += 40f

        canvas.drawLine(MARGIN.toFloat(), y, (A4_WIDTH - MARGIN).toFloat(), y, linePaint)
        y += 25f

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        for (receipt in receipts) {
            if (y > A4_HEIGHT - MARGIN) break
            val dateStr = dateFormat.format(Date(receipt.date))
            val amountStr = "R${"%.2f".format(receipt.amount)}"
            val noteStr = receipt.note?.let { " - $it" } ?: ""
            canvas.drawText("$dateStr: $amountStr$noteStr", MARGIN.toFloat(), y, captionPaint)
            y += 20f
        }
    }

    private fun drawReceiptPage(
        canvas: Canvas,
        receipt: Receipt,
        pageNum: Int,
        totalPages: Int,
        dateFormat: SimpleDateFormat,
        captionPaint: Paint,
        captionBoldPaint: Paint
    ) {
        val dateStr = dateFormat.format(Date(receipt.date))
        val amountStr = "R${"%.2f".format(receipt.amount)}"

        var y = MARGIN.toFloat()
        canvas.drawText("Receipt $pageNum of $totalPages", MARGIN.toFloat(), y + 14f, captionPaint)
        canvas.drawText(amountStr, (A4_WIDTH - MARGIN).toFloat() - captionBoldPaint.measureText(amountStr), y + 14f, captionBoldPaint)
        y += 20f
        canvas.drawText(dateStr, MARGIN.toFloat(), y + 14f, captionPaint)
        receipt.note?.let {
            canvas.drawText(it, (A4_WIDTH - MARGIN).toFloat() - captionPaint.measureText(it), y + 14f, captionPaint)
        }
        y += 30f

        val bitmap = decodeScaledPhoto(receipt.photoPath) ?: return
        val availableWidth = A4_WIDTH - 2 * MARGIN
        val availableHeight = A4_HEIGHT - y.toInt() - MARGIN
        val scale = minOf(
            availableWidth.toFloat() / bitmap.width,
            availableHeight.toFloat() / bitmap.height
        )
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledHeight = (bitmap.height * scale).toInt()
        val left = MARGIN + (availableWidth - scaledWidth) / 2

        val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        canvas.drawBitmap(scaled, left.toFloat(), y, null)
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
    }

    private fun decodeScaledPhoto(path: String): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null

        var sampleSize = 1
        val longestSide = maxOf(opts.outWidth, opts.outHeight)
        while (longestSide / sampleSize > MAX_PHOTO_PX * 2) {
            sampleSize *= 2
        }

        val decoded = BitmapFactory.Options().run {
            inSampleSize = sampleSize
            BitmapFactory.decodeFile(path, this)
        } ?: return null

        val maxSide = maxOf(decoded.width, decoded.height)
        if (maxSide <= MAX_PHOTO_PX) return decoded

        val ratio = MAX_PHOTO_PX.toFloat() / maxSide
        val result = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * ratio).toInt(),
            (decoded.height * ratio).toInt(),
            true
        )
        if (result !== decoded) decoded.recycle()
        return result
    }
}
