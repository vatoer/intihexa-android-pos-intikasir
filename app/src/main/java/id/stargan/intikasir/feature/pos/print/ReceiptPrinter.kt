package id.stargan.intikasir.feature.pos.print

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PageRange
import android.print.PrintManager
import androidx.core.net.toUri
import androidx.core.content.FileProvider
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.util.BluetoothPermissionHelper
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

object ReceiptPrinter {

    data class Result(val pdfUri: Uri, val fileName: String)

    fun generateReceiptPdf(
        context: Context,
        settings: StoreSettings?,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ): Result {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            textSize = 10f
            color = Color.DKGRAY
        }
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
        }

        var y = 40f
        val xPadding = 40f
        val centerX = pageInfo.pageWidth / 2f

        // Logo (if enabled and available)
        if (settings?.printLogo == true && !settings.storeLogo.isNullOrBlank()) {
            try {
                val logoFile = File(settings.storeLogo)
                if (logoFile.exists()) {
                    val bmp = BitmapFactory.decodeFile(settings.storeLogo)
                    if (bmp != null) {
                        // Limit logo to reasonable size (max 80 pixels for receipt)
                        val maxW = 80
                        val maxH = 80
                        val ratio = bmp.width.toFloat() / bmp.height.toFloat()

                        var w = min(maxW, bmp.width)
                        var h = (w / ratio).toInt().coerceAtLeast(1)

                        // If height exceeds max, recalculate
                        if (h > maxH) {
                            h = maxH
                            w = (h * ratio).toInt().coerceAtLeast(1)
                        }

                        val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
                        val cx = (pageInfo.pageWidth - scaled.width) / 2f
                        canvas.drawBitmap(scaled, cx, y, paint)
                        y += scaled.height + 12 // Reduced spacing
                        bmp.recycle()
                        scaled.recycle()
                    }
                }
            } catch (e: Exception) {
                Log.w("ReceiptPrinter", "Failed to load logo for PDF", e)
            }
        }

        // Store name & info
        val storeName = settings?.storeName?.takeIf { it.isNotBlank() } ?: "Nama Toko"
        val storeAddr = settings?.storeAddress?.takeIf { it.isNotBlank() } ?: "Alamat toko"
        val storePhone = settings?.storePhone?.takeIf { it.isNotBlank() }
        
        drawCenteredText(canvas, storeName, centerX, y, titlePaint)
        y += 24
        drawCenteredText(canvas, storeAddr, centerX, y, smallPaint)
        y += 14
        storePhone?.let {
            drawCenteredText(canvas, "Telp: $it", centerX, y, smallPaint)
            y += 14
        }
        
        // Custom header
        settings?.receiptHeader?.let {
            if (it.isNotBlank()) {
                y += 6
                it.split("\n").take(3).forEach { line ->
                    if (line.isNotBlank()) {
                        drawCenteredText(canvas, line.trim(), centerX, y, smallPaint)
                        y += 14
                    }
                }
            }
        }
        
        y += 10

        // Separator
        y += 4f  // padding before divider
        paint.strokeWidth = 2f
        canvas.drawLine(xPadding, y, pageInfo.pageWidth - xPadding, y, paint)
        y += 22f  // padding after divider

        // Receipt Title
        drawCenteredText(canvas, "STRUK PEMBAYARAN", centerX, y, headerPaint)
        y += 24

        // Transaction header
        val dateStr = dateFormat.format(Date(transaction.updatedAt))
        canvas.drawText("No. Transaksi:", xPadding, y, boldPaint)
        canvas.drawText(transaction.transactionNumber, xPadding + 100f, y, normalPaint)
        y += 16
        canvas.drawText("Tanggal:", xPadding, y, boldPaint)
        canvas.drawText(dateStr, xPadding + 100f, y, normalPaint)
        y += 16
        canvas.drawText("Kasir:", xPadding, y, boldPaint)
        canvas.drawText(transaction.cashierName, xPadding + 100f, y, normalPaint)
        y += 20f

        // Separator
        y += 4f  // padding before divider
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        canvas.drawLine(xPadding, y, pageInfo.pageWidth - xPadding, y, paint)
        y += 18f  // padding after divider

        // Items table header
        val qtyColumnX = pageInfo.pageWidth - 250f  // Adjusted for better spacing
        val priceColumnX = pageInfo.pageWidth - 180f  // Adjusted for better spacing
        val subtotalColumnX = pageInfo.pageWidth - xPadding - boldPaint.measureText("00.000.000")  // Space for millions

        canvas.drawText("Item", xPadding, y, boldPaint)
        canvas.drawText("Qty", qtyColumnX, y, boldPaint)
        canvas.drawText("Harga", priceColumnX, y, boldPaint)
        canvas.drawText("Subtotal", pageInfo.pageWidth - xPadding - boldPaint.measureText("Subtotal"), y, boldPaint)
        y += 6f  // padding before divider
        canvas.drawLine(xPadding, y, pageInfo.pageWidth - xPadding, y, paint)
        y += 16f  // padding after divider

        // Items
        items.forEach { item ->
            val name = item.productName
            val qty = "${item.quantity}"
            val sub = nf.format(item.subtotal).replace("Rp", "Rp ")

            // If item has discount, show original price with strikethrough
            if (item.discount > 0) {
                val originalPrice = item.productPrice
                val discountedPrice = item.unitPrice

                // Name
                canvas.drawText(name, xPadding, y, normalPaint)
                canvas.drawText(qty, qtyColumnX, y, normalPaint)

                // Original price with strikethrough
                val origPriceStr = nf.format(originalPrice).replace("Rp", "Rp ")
                canvas.drawText(origPriceStr, priceColumnX, y, normalPaint)
                val textWidth = normalPaint.measureText(origPriceStr)
                val lineY = y - 4f
                canvas.drawLine(priceColumnX, lineY, priceColumnX + textWidth, lineY, normalPaint)

                canvas.drawText(sub, pageInfo.pageWidth - xPadding - normalPaint.measureText(sub), y, normalPaint)
                y += 18f

                // Discounted price line
                val discPriceStr = nf.format(discountedPrice).replace("Rp", "Rp ")
                canvas.drawText("Harga diskon: $discPriceStr", xPadding + 20f, y, smallPaint)
                y += 14f

                // Discount amount
                val discPart = "Diskon: -${nf.format(item.discount).replace("Rp", "Rp ")}"
                canvas.drawText(discPart, xPadding + 20f, y, smallPaint)
                y += 18f
            } else {
                // No discount - simple format
                val price = nf.format(item.unitPrice).replace("Rp", "Rp ")

                canvas.drawText(name, xPadding, y, normalPaint)
                canvas.drawText(qty, qtyColumnX, y, normalPaint)
                canvas.drawText(price, priceColumnX, y, normalPaint)
                canvas.drawText(sub, pageInfo.pageWidth - xPadding - normalPaint.measureText(sub), y, normalPaint)
                y += 18f
            }
        }

        y += 8f  // padding before divider
        // Separator
        canvas.drawLine(xPadding, y, pageInfo.pageWidth - xPadding, y, paint)
        y += 18f  // padding after divider

        // Totals - right aligned
        val rightX = pageInfo.pageWidth - xPadding
        val labelX = rightX - 150f
        
        val subtotalStr = nf.format(transaction.subtotal).replace("Rp", "Rp ")
        val taxStr = nf.format(transaction.tax).replace("Rp", "Rp ")
        val discountStr = nf.format(transaction.discount).replace("Rp", "Rp ")
        val totalStr = nf.format(transaction.total).replace("Rp", "Rp ")

        canvas.drawText("Subtotal:", labelX, y, normalPaint)
        canvas.drawText(subtotalStr, rightX - normalPaint.measureText(subtotalStr), y, normalPaint)
        y += 16

        if (transaction.tax > 0) {
            canvas.drawText("PPN:", labelX, y, normalPaint)
            canvas.drawText(taxStr, rightX - normalPaint.measureText(taxStr), y, normalPaint)
            y += 16
        }
        if (transaction.discount > 0) {
            canvas.drawText("Diskon:", labelX, y, normalPaint)
            canvas.drawText("-$discountStr", rightX - normalPaint.measureText("-$discountStr"), y, normalPaint)
            y += 16
        }

        y += 8f  // padding before divider
        paint.strokeWidth = 2f
        paint.color = Color.BLACK
        canvas.drawLine(labelX - 10f, y, rightX, y, paint)
        y += 20f  // padding after divider

        // Grand Total
        val grandTotalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("TOTAL:", labelX, y, grandTotalPaint)
        canvas.drawText(totalStr, rightX - grandTotalPaint.measureText(totalStr), y, grandTotalPaint)
        y += 24

        // Payment method
        val paymentMethodName = when (transaction.paymentMethod.name) {
            "CASH" -> "Tunai"
            "DEBIT" -> "Debit"
            "CREDIT" -> "Kredit"
            "QRIS" -> "QRIS"
            "TRANSFER" -> "Transfer"
            else -> transaction.paymentMethod.name
        }
        canvas.drawText("Metode Pembayaran:", labelX, y, normalPaint)
        canvas.drawText(paymentMethodName, rightX - normalPaint.measureText(paymentMethodName), y, normalPaint)
        y += 16

        // Payment info if cash
        val received = transaction.cashReceived
        if (received > 0) {
            val receivedStr = nf.format(received).replace("Rp", "Rp ")
            val change = (received - transaction.total).coerceAtLeast(0.0)
            val changeStr = nf.format(change).replace("Rp", "Rp ")
            
            canvas.drawText("Dibayar:", labelX, y, normalPaint)
            canvas.drawText(receivedStr, rightX - normalPaint.measureText(receivedStr), y, normalPaint)
            y += 16
            
            if (change > 0) {
                canvas.drawText("Kembalian:", labelX, y, boldPaint)
                canvas.drawText(changeStr, rightX - boldPaint.measureText(changeStr), y, boldPaint)
                y += 16
            }
        }

        // Footer
        y += 20f
        y += 4f  // padding before divider
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        canvas.drawLine(xPadding, y, pageInfo.pageWidth - xPadding, y, paint)
        y += 18f  // padding after divider

        drawCenteredText(canvas, "Terima kasih atas kunjungan Anda", centerX, y, normalPaint)
        y += 16
        
        settings?.receiptFooter?.let {
            if (it.isNotBlank()) {
                it.split("\n").take(3).forEach { line ->
                    if (line.isNotBlank()) {
                        drawCenteredText(canvas, line.trim(), centerX, y, smallPaint)
                        y += 14
                    }
                }
                y += 6
            }
        }
        
        drawCenteredText(canvas, "-- Struk ini sah tanpa tanda tangan --", centerX, y, smallPaint)

        doc.finishPage(page)

        val fileName = "Receipt-${transaction.transactionNumber}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out -> doc.writeTo(out) }
        doc.close()
        // Return a content:// URI via FileProvider (avoid file:// exposure)
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Result(contentUri, fileName)
    }

    fun generateThermalReceiptPdf(
        context: Context,
        settings: StoreSettings?,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ): Result {
        val paperWidthMm = settings?.paperWidthMm ?: 58
        val charsPerLine = settings?.paperCharPerLine ?: if (paperWidthMm >= 80) 48 else 32
        // Approximate pixel width for 58mm (~384px) and 80mm (~576px)
        val pageWidthPx = if (paperWidthMm >= 80) 576 else 384
        val pageHeightPx = 1400 // Increased height for better layout
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPx, pageHeightPx, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            textSize = 11f 
            color = Color.DKGRAY
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            strokeWidth = 1f
            color = Color.LTGRAY
        }
        val boldDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
            strokeWidth = 2f
            color = Color.BLACK
        }

        var y = 16f
        val left = 12f
        val right = pageWidthPx - 12f
        val centerX = pageWidthPx / 2f

        fun drawLine(bold: Boolean = false) {
            y += 4f  // padding before line
            canvas.drawLine(left, y, right, y, if (bold) boldDividerPaint else dividerPaint)
            y += if (bold) 12f else 10f  // padding after line
        }

        fun drawTextLine(line: String, paint: Paint = textPaint) {
            canvas.drawText(line.take(charsPerLine), left, y, paint)
            y += 18f
        }

        // Logo (if enabled and available)
        if (settings?.printLogo == true && !settings.storeLogo.isNullOrBlank()) {
            try {
                val logoFile = File(settings.storeLogo)
                if (logoFile.exists()) {
                    val bmp = BitmapFactory.decodeFile(settings.storeLogo)
                    if (bmp != null) {
                        // Limit logo to reasonable size for thermal receipt
                        val maxW = if (paperWidthMm >= 80) 80 else 60 // Reduced from 120/80
                        val maxH = 80 // Max height
                        val ratio = bmp.width.toFloat() / bmp.height.toFloat()

                        var w = min(maxW, bmp.width)
                        var h = (w / ratio).toInt().coerceAtLeast(1)

                        // If height exceeds max, recalculate
                        if (h > maxH) {
                            h = maxH
                            w = (h * ratio).toInt().coerceAtLeast(1)
                        }

                        val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
                        val cx = (pageWidthPx - scaled.width) / 2f
                        canvas.drawBitmap(scaled, cx, y, null)
                        y += scaled.height + 8
                        bmp.recycle()
                        scaled.recycle()
                    }
                }
            } catch (e: Exception) {
                Log.w("ReceiptPrinter", "Failed to load logo for thermal PDF", e)
            }
        }

        // Header - Store Name
        val name = (settings?.storeName ?: "Toko").uppercase()
        canvas.drawText(name, centerX, y, titlePaint)
        y += 22f
        
        // Address and phone
        val addr = settings?.storeAddress ?: "Alamat"
        if (addr.isNotBlank()) {
            drawCenteredText(canvas, addr.take(charsPerLine), centerX, y, smallPaint)
            y += 16f
        }
        val phone = settings?.storePhone ?: ""
        if (phone.isNotBlank()) {
            drawCenteredText(canvas, "Telp: $phone", centerX, y, smallPaint)
            y += 16f
        }
        
        // Custom header
        settings?.receiptHeader?.let {
            if (it.isNotBlank()) {
                it.split("\n").take(3).forEach { line ->
                    if (line.isNotBlank()) {
                        drawCenteredText(canvas, line.trim().take(charsPerLine), centerX, y, smallPaint)
                        y += 16f
                    }
                }
            }
        }
        
        drawLine(bold = true)
        
        // Transaction info
        val dateStr = dateFormat.format(Date(transaction.updatedAt))
        canvas.drawText("No: ${transaction.transactionNumber}", left, y, textPaint)
        y += 16f
        canvas.drawText("Tanggal: $dateStr", left, y, textPaint)
        y += 16f
        canvas.drawText("Kasir: ${transaction.cashierName}", left, y, textPaint)
        y += 4f
        drawLine()

        // Items header
        canvas.drawText("ITEM", left, y, boldPaint)
        // Move JUMLAH column more to the left to accommodate larger amounts
        val amountColumnX = right - 85f  // Adjusted from far right to support millions
        canvas.drawText("JUMLAH", amountColumnX, y, boldPaint)
        y += 4f
        drawLine()

        // Items
        items.forEach { item ->
            // Product name
            val name = item.productName.take(charsPerLine - 12)  // Leave space for amount
            canvas.drawText(name, left, y, textPaint)
            y += 16f
            
            // If item has discount, show original price with strikethrough
            if (item.discount > 0) {
                val originalPrice = item.productPrice
                val discountPerUnit = item.discount / item.quantity
                val discountedPricePerUnit = originalPrice - discountPerUnit

                val origPriceStr = "@${nf.format(originalPrice).replace("Rp", "Rp ")}/pcs"
                canvas.drawText(origPriceStr, left + 8f, y, smallPaint)

                // Draw strikethrough line
                val textWidth = smallPaint.measureText(origPriceStr)
                val lineY = y - 4f
                canvas.drawLine(left + 8f, lineY, left + 8f + textWidth, lineY, smallPaint)
                y += 14f

                // Quantity x discounted price per unit = subtotal
                val qtyPart = "${item.quantity} x ${nf.format(discountedPricePerUnit).replace("Rp", "Rp ")}"
                val subPart = nf.format(item.subtotal).replace("Rp", "Rp ")
                canvas.drawText(qtyPart, left + 8f, y, smallPaint)
                // Right align subtotal at the amount column
                canvas.drawText(subPart, right - smallPaint.measureText(subPart), y, textPaint)
                y += 18f

                // Total discount amount
                val discountPart = "Diskon: -${nf.format(item.discount).replace("Rp", "Rp ")}"
                canvas.drawText(discountPart, left + 8f, y, smallPaint)
                y += 16f
            } else {
                // No discount - simple format
                val qtyPart = "${item.quantity} x ${nf.format(item.unitPrice).replace("Rp", "Rp ")}"
                val subPart = nf.format(item.subtotal).replace("Rp", "Rp ")
                canvas.drawText(qtyPart, left + 8f, y, smallPaint)
                canvas.drawText(subPart, right - smallPaint.measureText(subPart), y, textPaint)
                y += 18f
            }
        }
        y += 4f
        drawLine(bold = true)

        // Totals
        fun drawTotal(label: String, value: Double, paint: Paint = textPaint, negative: Boolean = false) {
            val valueStr = nf.format(value).replace("Rp", "Rp ")
            canvas.drawText(label, left, y, paint)
            val display = if (negative) "-$valueStr" else valueStr
            canvas.drawText(display, right - paint.measureText(display), y, paint)
            y += 18f
        }
        
        drawTotal("Subtotal", transaction.subtotal, textPaint)
        if (transaction.tax > 0) drawTotal("PPN", transaction.tax, textPaint)
        if (transaction.discount > 0) drawTotal("Diskon", transaction.discount, textPaint, negative = true)
        
        y += 4f
        drawLine(bold = true)
        
        // Grand Total - larger and bold
        val grandTotalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val totalStr = nf.format(transaction.total).replace("Rp", "Rp ")
        canvas.drawText("TOTAL", left, y, grandTotalPaint)
        canvas.drawText(totalStr, right - grandTotalPaint.measureText(totalStr), y, grandTotalPaint)
        y += 22f
        
        drawLine()

        // Payment info
        val paymentMethodName = when (transaction.paymentMethod.name) {
            "CASH" -> "Tunai"
            "DEBIT" -> "Debit"
            "CREDIT" -> "Kredit"
            "QRIS" -> "QRIS"
            "TRANSFER" -> "Transfer"
            else -> transaction.paymentMethod.name
        }
        canvas.drawText("Metode: $paymentMethodName", left, y, textPaint)
        y += 18f
        
        val received = transaction.cashReceived
        if (received > 0) {
            val change = (received - transaction.total).coerceAtLeast(0.0)
            drawTotal("Dibayar", received, textPaint)
            if (change > 0) {
                drawTotal("Kembali", change, boldPaint)
            }
        }
        
        y += 8f
        drawLine(bold = true)

        // Footer
        y += 8f
        drawCenteredText(canvas, "Terima kasih atas kunjungan Anda", centerX, y, textPaint)
        y += 18f
        
        settings?.receiptFooter?.let {
            if (it.isNotBlank()) {
                it.split("\n").take(3).forEach { line ->
                    if (line.isNotBlank()) {
                        drawCenteredText(canvas, line.trim().take(charsPerLine), centerX, y, smallPaint)
                        y += 16f
                    }
                }
            }
        }
        
        y += 8f
        drawCenteredText(canvas, "-- Struk ini sah tanpa tanda tangan --", centerX, y, smallPaint)

        doc.finishPage(page)
        val fileName = "Receipt-${transaction.transactionNumber}-${paperWidthMm}mm.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out -> doc.writeTo(out) }
        doc.close()
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Result(contentUri, fileName)
    }

    /**
     * Generate queue number ticket (thermal)
     */
    fun generateQueueTicketPdf(
        context: Context,
        settings: StoreSettings?,
        transaction: TransactionEntity
    ): Result {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Antrian_${transaction.transactionNumber}_${dateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)

        val pageWidth = if (settings?.paperWidthMm?.let { it >= 80 } == true) 576f else 384f
        val pageHeight = 400f // Short ticket
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.DKGRAY
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val boldPaint = Paint(paint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 32f
        }
        val titlePaint = Paint(paint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 48f
            textAlign = Paint.Align.CENTER
        }

        var y = 40f
        val left = 20f
        val center = pageWidth / 2

        // Header
        canvas.drawText("NOMOR ANTRIAN", center, y, titlePaint)
        y += 60f

        // Queue Number (from transaction number - extract last 4 digits)
        val queueNumber = transaction.transactionNumber.substringAfterLast('-')
        titlePaint.textSize = 72f
        canvas.drawText(queueNumber, center, y, titlePaint)
        y += 80f

        titlePaint.textSize = 28f
        titlePaint.textAlign = Paint.Align.LEFT

        // Transaction summary
        val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        canvas.drawText("Transaksi: ${transaction.transactionNumber}", left, y, paint)
        y += 30f
        canvas.drawText("Waktu: ${dateFormatter.format(Date(transaction.transactionDate))}", left, y, paint)
        y += 30f

        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        canvas.drawText("Total: ${nf.format(transaction.total).replace("Rp", "Rp ")}", left, y, boldPaint)
        y += 40f

        // Footer
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Terima kasih", center, y, paint)

        document.finishPage(page)

        try {
            document.writeTo(FileOutputStream(file))
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            return Result(uri, fileName)
        } finally {
            document.close()
        }
    }

    fun printOrSave(
        context: Context,
        settings: StoreSettings?,
        pdfUri: Uri,
        jobName: String
    ) {
        if (settings?.printerConnected == true) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val adapter = PdfFilePrintAdapter(context, pdfUri, jobName)
            val attr = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER.asPortrait())
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .build()
            printManager.print(jobName, adapter, attr)
        } else {
            saveToDownloads(context, pdfUri, jobName)
        }
    }

    private fun saveToDownloads(context: Context, pdfUri: Uri, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val itemUri = resolver.insert(collection, values) ?: return
                resolver.openOutputStream(itemUri)?.use { out ->
                    resolver.openInputStream(pdfUri)?.use { input ->
                        input.copyTo(out)
                    }
                }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val outFile = File(dir, fileName)
                context.contentResolver.openInputStream(pdfUri)?.use { input ->
                    FileOutputStream(outFile).use { out -> input.copyTo(out) }
                }
            }
        } catch (_: Exception) { }
    }

    fun sharePdf(context: Context, pdfUri: Uri) {
        try {
            // Convert file:// URIs to content:// via FileProvider and validate
            val shareUri = when (pdfUri.scheme) {
                "file" -> {
                    val file = File(pdfUri.path ?: "")
                    if (!file.exists()) {
                        Toast.makeText(context, "File struk tidak ditemukan", Toast.LENGTH_SHORT).show()
                        Log.w("ReceiptPrinter", "sharePdf: file not found=$pdfUri")
                        return
                    }
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                }
                else -> pdfUri
            }

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, shareUri)
                // Provide ClipData which some apps require to read the stream
                clipData = android.content.ClipData.newUri(context.contentResolver, "Struk", shareUri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = android.content.Intent.createChooser(intent, "Bagikan Struk")

            // Explicitly grant URI permission to resolved activities (defensive)
            val resInfos = context.packageManager.queryIntentActivities(chooser, 0)
            resInfos.forEach { resolveInfo ->
                try {
                    context.grantUriPermission(
                        resolveInfo.activityInfo.packageName,
                        shareUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // ignore individual grant failures but log them
                    Log.w("ReceiptPrinter", "Failed to grant uri permission to ${resolveInfo.activityInfo.packageName}", e)
                }
            }

            context.startActivity(
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Log.w("ReceiptPrinter", "sharePdf failed: ${e.message}", e)
            Toast.makeText(context, "Gagal membagikan struk", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, cx: Float, y: Float, paint: Paint) {
        val w = paint.measureText(text)
        canvas.drawText(text, cx - w / 2f, y, paint)
    }

    fun printQueueOrPdf(
        context: Context,
        settings: StoreSettings?,
        transaction: TransactionEntity
    ): ESCPosPrinter.PrintResult? {
        return if (settings?.useEscPosDirect == true && !settings.printerAddress.isNullOrBlank()) {
            // Return PrintResult for ESC/POS direct print
            ESCPosPrinter.printQueueTicket(context, settings, transaction)
        } else {
            // For PDF, just generate and print/save, return null (no need for PrintResult)
            val result = generateQueueTicketPdf(context, settings, transaction)
            printOrSave(context, settings, result.pdfUri, result.fileName)
            null
        }
    }

    /**
     * Print receipt - auto-select ESC/POS or PDF based on settings
     */
    fun printReceiptOrPdf(
        context: Context,
        settings: StoreSettings?,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ): ESCPosPrinter.PrintResult? {
        val safeSettings = settings
        if (safeSettings == null) {
            Log.w("ReceiptPrinter", "printReceiptOrPdf: settings null, aborting")
            return ESCPosPrinter.PrintResult.Error("Pengaturan printer belum siap")
        }

        // Log for debugging (validation moved to UI layer to prevent race conditions)
        Log.d("ReceiptPrinter", "printReceiptOrPdf: items=${items.size} useEscPosDirect=${safeSettings.useEscPosDirect} printerAddress=${safeSettings.printerAddress} connected=${safeSettings.printerConnected}")

        val canBluetooth = !safeSettings.printerAddress.isNullOrBlank() && BluetoothPermissionHelper.hasBluetoothPermissions(context)
        val shouldEscPos = (safeSettings.useEscPosDirect || (safeSettings.printerConnected && canBluetooth))
        return if (shouldEscPos) {
            ESCPosPrinter.printReceipt(context, safeSettings, transaction, items)
        } else {
            val result = generateThermalReceiptPdf(context, safeSettings, transaction, items)
            printOrSave(context, safeSettings, result.pdfUri, result.fileName)
            null
        }
    }
}

private class PdfFilePrintAdapter(
    private val context: Context,
    private val pdfUri: Uri,
    private val jobName: String
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback,
        extras: android.os.Bundle?
    ) {
        val info = PrintDocumentInfo.Builder(jobName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal,
        callback: WriteResultCallback
    ) {
        try {
            context.contentResolver.openInputStream(pdfUri)?.use { input ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        }
    }
}
