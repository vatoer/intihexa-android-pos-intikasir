package id.stargan.intikasir.feature.pos.print

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.domain.model.StoreSettings
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generate receipt as PNG image (thermal printer style)
 * Optimized for sharing via WhatsApp, social media
 */
object ReceiptImageGenerator {
    private const val TAG = "ReceiptImageGenerator"

    // Thermal receipt styling
    private const val PAPER_WIDTH_PX = 576 // 80mm thermal
    private const val PADDING_PX = 24
    private const val LINE_HEIGHT_PX = 32
    private const val SPACING_SMALL_PX = 8
    private const val SPACING_MEDIUM_PX = 16

    data class ImageResult(
        val imageUri: Uri,
        val imagePath: String,
        val fileName: String
    )

    /**
     * Generate receipt image as PNG
     */
    fun generateReceiptImage(
        context: Context,
        settings: StoreSettings,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ): ImageResult {
        Log.d(TAG, "Generating receipt image for ${transaction.transactionNumber}")

        val nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))

        // Calculate height needed
        var yPosition = PADDING_PX

        // Logo height if enabled
        if (settings.printLogo) {
            yPosition += estimateLogoHeight(context, settings) + SPACING_MEDIUM_PX + SPACING_SMALL_PX // Extra space after logo
        }

        // Header (store name + address + divider with spacing)
        yPosition += LINE_HEIGHT_PX * 3 + SPACING_MEDIUM_PX * 2 + SPACING_SMALL_PX // Space before + line + space after

        // Transaction info (no, date, cashier + divider with spacing)
        yPosition += LINE_HEIGHT_PX * 3 + SPACING_MEDIUM_PX * 2 + SPACING_SMALL_PX // Space before + line + space after

        // Items
        items.forEach { item ->
            yPosition += LINE_HEIGHT_PX // Product name
            if (item.discount > 0) {
                yPosition += LINE_HEIGHT_PX * 3 // Original price, discounted price, discount
            } else {
                yPosition += LINE_HEIGHT_PX // Qty x price
            }
        }

        // Totals (divider with spacing + subtotal, tax, discount, total)
        yPosition += LINE_HEIGHT_PX + SPACING_MEDIUM_PX * 2 + SPACING_SMALL_PX // Space before + line + space after
        yPosition += LINE_HEIGHT_PX * 4 // Subtotal, tax, discount, total

        if (transaction.cashReceived > 0) {
            yPosition += LINE_HEIGHT_PX * 2 // Cash received, change
        }

        // Footer (divider with spacing + thank you)
        yPosition += LINE_HEIGHT_PX * 3 + SPACING_MEDIUM_PX * 2 + SPACING_SMALL_PX // Space before + line + space after + footer

        val totalHeight = yPosition + PADDING_PX

        // Create bitmap
        val bitmap = Bitmap.createBitmap(PAPER_WIDTH_PX, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // White background
        canvas.drawColor(Color.WHITE)

        // Draw receipt content
        drawReceipt(canvas, context, settings, transaction, items, nf, dateFormat)

        // Save to file
        val fileName = "receipt_${transaction.transactionNumber}_${System.currentTimeMillis()}.png"
        val cacheDir = File(context.cacheDir, "receipts")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val imageFile = File(cacheDir, fileName)
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        Log.d(TAG, "Receipt image saved: ${imageFile.absolutePath}, size: ${imageFile.length()} bytes")

        // Get shareable URI
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        return ImageResult(imageUri, imageFile.absolutePath, fileName)
    }

    private fun drawReceipt(
        canvas: Canvas,
        context: Context,
        settings: StoreSettings,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>,
        nf: NumberFormat,
        dateFormat: SimpleDateFormat
    ) {
        var y = PADDING_PX.toFloat()

        // Paint configurations
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.MONOSPACE
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 32f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val dividerPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }

        val centerX = PAPER_WIDTH_PX / 2f

        // Logo
        if (settings.printLogo) {
            val logoHeight = drawLogo(canvas, context, settings, y)
            if (logoHeight > 0) {
                y += logoHeight + SPACING_MEDIUM_PX + SPACING_SMALL_PX // Extra spacing after logo
            }
        }

        // Store name
        val storeName = settings.storeName.ifBlank { "TOKO" }
        canvas.drawText(storeName, centerX, y, titlePaint)
        y += LINE_HEIGHT_PX

        // Store address
        if (settings.storeAddress.isNotBlank()) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(settings.storeAddress, centerX, y, textPaint)
            y += LINE_HEIGHT_PX
        }

        // Divider - occupies its own space
        y += SPACING_MEDIUM_PX // Space before divider
        canvas.drawLine(PADDING_PX.toFloat(), y, (PAPER_WIDTH_PX - PADDING_PX).toFloat(), y, dividerPaint)
        y += SPACING_SMALL_PX // Divider line space
        y += SPACING_MEDIUM_PX // Space after divider

        // Transaction info (left aligned)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("No: ${transaction.transactionNumber}", PADDING_PX.toFloat(), y, textPaint)
        y += LINE_HEIGHT_PX

        canvas.drawText("Tgl: ${dateFormat.format(Date(transaction.transactionDate))}", PADDING_PX.toFloat(), y, textPaint)
        y += LINE_HEIGHT_PX

        canvas.drawText("Kasir: ${transaction.cashierName}", PADDING_PX.toFloat(), y, textPaint)
        y += LINE_HEIGHT_PX

        // Divider - occupies its own space
        y += SPACING_MEDIUM_PX // Space before divider
        canvas.drawLine(PADDING_PX.toFloat(), y, (PAPER_WIDTH_PX - PADDING_PX).toFloat(), y, dividerPaint)
        y += SPACING_SMALL_PX // Divider line space
        y += SPACING_MEDIUM_PX // Space after divider

        // Items
        items.forEach { item ->
            // Product name
            canvas.drawText(item.productName, PADDING_PX.toFloat(), y, textPaint)
            y += LINE_HEIGHT_PX

            if (item.discount > 0) {
                // Original price with @ prefix
                val originalPrice = item.productPrice
                val origPriceStr = "@${nf.format(originalPrice).replace("Rp", "Rp ")}/pcs"
                canvas.drawText("  $origPriceStr", PADDING_PX.toFloat(), y, textPaint)
                y += LINE_HEIGHT_PX

                // Quantity x discounted price = subtotal
                val discountPerUnit = item.discount / item.quantity
                val discountedPricePerUnit = originalPrice - discountPerUnit
                val qtyStr = "${item.quantity} x ${nf.format(discountedPricePerUnit).replace("Rp", "Rp ")}"
                val subStr = nf.format(item.subtotal).replace("Rp", "Rp ")

                // Draw qty on left, subtotal on right
                canvas.drawText(qtyStr, PADDING_PX.toFloat(), y, textPaint)
                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(subStr, (PAPER_WIDTH_PX - PADDING_PX).toFloat(), y, textPaint)
                textPaint.textAlign = Paint.Align.LEFT
                y += LINE_HEIGHT_PX

                // Discount
                val discountStr = "  Diskon: -${nf.format(item.discount).replace("Rp", "Rp ")}"
                canvas.drawText(discountStr, PADDING_PX.toFloat(), y, textPaint)
                y += LINE_HEIGHT_PX
            } else {
                // No discount - simple format
                val qtyStr = "${item.quantity} x ${nf.format(item.unitPrice).replace("Rp", "Rp ")}"
                val subStr = nf.format(item.subtotal).replace("Rp", "Rp ")

                canvas.drawText(qtyStr, PADDING_PX.toFloat(), y, textPaint)
                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(subStr, (PAPER_WIDTH_PX - PADDING_PX).toFloat(), y, textPaint)
                textPaint.textAlign = Paint.Align.LEFT
                y += LINE_HEIGHT_PX
            }
        }

        // Divider - occupies its own space
        y += SPACING_MEDIUM_PX // Space before divider
        canvas.drawLine(PADDING_PX.toFloat(), y, (PAPER_WIDTH_PX - PADDING_PX).toFloat(), y, dividerPaint)
        y += SPACING_SMALL_PX // Divider line space
        y += SPACING_MEDIUM_PX // Space after divider

        // Totals
        fun drawTotalLine(label: String, value: String, bold: Boolean = false) {
            val paint = if (bold) boldPaint else textPaint
            canvas.drawText(label, PADDING_PX.toFloat(), y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, (PAPER_WIDTH_PX - PADDING_PX).toFloat(), y, paint)
            paint.textAlign = Paint.Align.LEFT
            y += LINE_HEIGHT_PX
        }

        drawTotalLine("Subtotal", nf.format(transaction.subtotal).replace("Rp", "Rp "))

        if (transaction.tax > 0) {
            drawTotalLine("PPN", nf.format(transaction.tax).replace("Rp", "Rp "))
        }

        if (transaction.discount > 0) {
            drawTotalLine("Diskon", "-${nf.format(transaction.discount).replace("Rp", "Rp ")}")
        }

        drawTotalLine("TOTAL", nf.format(transaction.total).replace("Rp", "Rp "), bold = true)

        if (transaction.cashReceived > 0) {
            drawTotalLine("Tunai", nf.format(transaction.cashReceived).replace("Rp", "Rp "))
            val change = (transaction.cashReceived - transaction.total).coerceAtLeast(0.0)
            drawTotalLine("Kembali", nf.format(change).replace("Rp", "Rp "))
        }

        // Divider - occupies its own space
        y += SPACING_MEDIUM_PX // Space before divider
        canvas.drawLine(PADDING_PX.toFloat(), y, (PAPER_WIDTH_PX - PADDING_PX).toFloat(), y, dividerPaint)
        y += SPACING_SMALL_PX // Divider line space
        y += SPACING_MEDIUM_PX // Space after divider

        // Thank you message
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Terima kasih", centerX, y, textPaint)
    }

    private fun drawLogo(
        canvas: Canvas,
        context: Context,
        settings: StoreSettings,
        startY: Float
    ): Float {
        if (settings.storeLogo.isNullOrBlank()) return 0f

        val logoFile = File(settings.storeLogo)
        if (!logoFile.exists()) return 0f

        return try {
            val originalBitmap = BitmapFactory.decodeFile(logoFile.absolutePath) ?: return 0f

            // Scale logo to 40% of paper width
            val targetWidth = (PAPER_WIDTH_PX * 0.40).toInt()
            val aspectRatio = originalBitmap.height.toFloat() / originalBitmap.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt()

            val scaledLogo = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

            // Center horizontally
            val left = (PAPER_WIDTH_PX - targetWidth) / 2f

            canvas.drawBitmap(scaledLogo, left, startY, null)

            originalBitmap.recycle()
            scaledLogo.recycle()

            targetHeight.toFloat()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to draw logo", e)
            0f
        }
    }

    private fun estimateLogoHeight(context: Context, settings: StoreSettings): Int {
        if (settings.storeLogo.isNullOrBlank()) return 0

        val logoFile = File(settings.storeLogo)
        if (!logoFile.exists()) return 0

        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(logoFile.absolutePath, options)

            val targetWidth = (PAPER_WIDTH_PX * 0.40).toInt()
            val aspectRatio = options.outHeight.toFloat() / options.outWidth.toFloat()
            (targetWidth * aspectRatio).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Share receipt image via Android Share Sheet
     */
    fun shareImage(context: Context, imageUri: Uri) {
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(android.content.Intent.EXTRA_STREAM, imageUri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = android.content.Intent.createChooser(shareIntent, "Bagikan Struk")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

