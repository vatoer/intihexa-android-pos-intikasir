package id.stargan.intikasir.feature.pos.print

import android.content.ContentValues
import android.content.Context
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
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.domain.model.StoreSettings
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
        }

        var y = 40f
        val xPadding = 24f

        // Logo (optional)
        if (settings?.printLogo == true && !settings.storeLogo.isNullOrBlank()) {
            try {
                val bmp = BitmapFactory.decodeFile(settings.storeLogo)
                if (bmp != null) {
                    val maxW = 120
                    val ratio = bmp.width.toFloat() / bmp.height.toFloat()
                    val w = maxW
                    val h = (maxW / ratio).toInt().coerceAtLeast(1)
                    val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
                    val cx = (pageInfo.pageWidth - scaled.width) / 2f
                    canvas.drawBitmap(scaled, cx, y, paint)
                    y += scaled.height + 12
                }
            } catch (_: Exception) { }
        }

        // Store name & address
        val storeName = settings?.storeName?.takeIf { it.isNotBlank() } ?: "Nama Toko"
        val storeAddr = settings?.storeAddress?.takeIf { it.isNotBlank() } ?: "Alamat toko"
        drawCenteredText(canvas, storeName, pageInfo.pageWidth / 2f, y, titlePaint)
        y += 20
        drawCenteredText(canvas, storeAddr, pageInfo.pageWidth / 2f, y, smallPaint)
        y += 16

        // Separator
        paint.strokeWidth = 1f
        canvas.drawLine(xPadding, y, pageInfo.pageWidth - xPadding, y, paint)
        y += 12

        // Transaction header
        val dateStr = dateFormat.format(Date(transaction.updatedAt))
        canvas.drawText("No: ${transaction.transactionNumber}", xPadding, y, normalPaint)
        canvas.drawText(dateStr, pageInfo.pageWidth - xPadding - normalPaint.measureText(dateStr), y, normalPaint)
        y += 16

        // Items
        items.forEach { item ->
            val name = item.productName
            val qtyPrice = "${item.quantity} x ${nf.format(item.unitPrice).replace("Rp", "Rp ")}"
            val sub = nf.format(item.subtotal).replace("Rp", "Rp ")

            // Name
            canvas.drawText(name, xPadding, y, normalPaint)
            y += 14
            // qty and subtotal
            canvas.drawText(qtyPrice, xPadding, y, smallPaint)
            canvas.drawText(sub, pageInfo.pageWidth - xPadding - smallPaint.measureText(sub), y, smallPaint)
            y += 18
        }

        // Separator
        canvas.drawLine(xPadding, y, pageInfo.pageWidth - xPadding, y, paint)
        y += 12

        // Totals
        val subtotalStr = nf.format(transaction.subtotal).replace("Rp", "Rp ")
        val taxStr = nf.format(transaction.tax).replace("Rp", "Rp ")
        val discountStr = nf.format(transaction.discount).replace("Rp", "Rp ")
        val totalStr = nf.format(transaction.total).replace("Rp", "Rp ")

        canvas.drawText("Subtotal", xPadding, y, normalPaint)
        canvas.drawText(subtotalStr, pageInfo.pageWidth - xPadding - normalPaint.measureText(subtotalStr), y, normalPaint)
        y += 14

        if (transaction.tax > 0) {
            canvas.drawText("PPN", xPadding, y, normalPaint)
            canvas.drawText(taxStr, pageInfo.pageWidth - xPadding - normalPaint.measureText(taxStr), y, normalPaint)
            y += 14
        }
        if (transaction.discount > 0) {
            canvas.drawText("Diskon", xPadding, y, normalPaint)
            canvas.drawText("-$discountStr", pageInfo.pageWidth - xPadding - normalPaint.measureText("-$discountStr"), y, normalPaint)
            y += 14
        }

        canvas.drawText("Total", xPadding, y, boldPaint)
        canvas.drawText(totalStr, pageInfo.pageWidth - xPadding - boldPaint.measureText(totalStr), y, boldPaint)
        y += 18

        // Payment info if cash
        val received = transaction.cashReceived // already non-null in entity but safe if nullable
        if (received > 0) {
            val receivedStr = nf.format(received).replace("Rp", "Rp ")
            val change = (received - transaction.total).coerceAtLeast(0.0)
            val changeStr = nf.format(change).replace("Rp", "Rp ")
            canvas.drawText("Tunai diterima", xPadding, y, normalPaint)
            canvas.drawText(receivedStr, pageInfo.pageWidth - xPadding - normalPaint.measureText(receivedStr), y, normalPaint)
            y += 14
            canvas.drawText("Kembalian", xPadding, y, normalPaint)
            canvas.drawText(changeStr, pageInfo.pageWidth - xPadding - normalPaint.measureText(changeStr), y, normalPaint)
            y += 14
        }

        // Footer
        y += 10
        drawCenteredText(canvas, "Terima kasih telah berbelanja", pageInfo.pageWidth / 2f, y, smallPaint)

        doc.finishPage(page)

        val fileName = "Receipt-${transaction.transactionNumber}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out -> doc.writeTo(out) }
        doc.close()
        return Result(file.toUri(), fileName)
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
        val pageHeightPx = 800 // will grow if needed (simple single page assumption)
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPx, pageHeightPx, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale("id", "ID"))

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 14f }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 1f }

        var y = 20f
        val left = 10f
        val right = pageWidthPx - 10f

        fun drawLine() {
            canvas.drawLine(left, y, right, y, dividerPaint)
            y += 8
        }

        fun drawTextLine(line: String, paint: Paint = textPaint) {
            canvas.drawText(line.take(charsPerLine), left, y, paint)
            y += 18
        }

        // Header
        val name = (settings?.storeName ?: "Toko").uppercase()
        val addr = settings?.storeAddress ?: "Alamat"
        drawCenteredText(canvas, name, pageWidthPx / 2f, y, boldPaint); y += 20
        drawCenteredText(canvas, addr.take(charsPerLine), pageWidthPx / 2f, y, smallPaint); y += 18
        settings?.receiptHeader?.let {
            if (it.isNotBlank()) {
                drawCenteredText(canvas, it.take(charsPerLine), pageWidthPx / 2f, y, smallPaint); y += 18
            }
        }
        drawLine()

        // Transaction info
        drawTextLine("No: ${transaction.transactionNumber}")
        drawTextLine(dateFormat.format(Date(transaction.updatedAt)))
        drawLine()

        // Items
        items.forEach { item ->
            drawTextLine(item.productName)
            val qtyPart = "${item.quantity} x ${nf.format(item.unitPrice).replace("Rp", "Rp ")}"
            val subPart = nf.format(item.subtotal).replace("Rp", "Rp ")
            // Right align subtotal
            canvas.drawText(qtyPart.take(charsPerLine), left, y, smallPaint)
            canvas.drawText(subPart, right - smallPaint.measureText(subPart), y, smallPaint)
            y += 18
        }
        drawLine()

        // Totals
        fun drawTotal(label: String, value: Double, paint: Paint = textPaint, negative: Boolean = false) {
            val valueStr = nf.format(value).replace("Rp", "Rp ")
            canvas.drawText(label, left, y, paint)
            val display = if (negative) "-$valueStr" else valueStr
            canvas.drawText(display, right - paint.measureText(display), y, paint)
            y += 18
        }
        drawTotal("Subtotal", transaction.subtotal)
        if (transaction.tax > 0) drawTotal("PPN", transaction.tax)
        if (transaction.discount > 0) drawTotal("Diskon", transaction.discount, textPaint, negative = true)
        drawTotal("TOTAL", transaction.total, boldPaint)

        val received = transaction.cashReceived // already non-null in entity but safe if nullable
        if (received > 0) {
            val change = (received - transaction.total).coerceAtLeast(0.0)
            drawTotal("Tunai", received)
            drawTotal("Kembali", change)
        }
        drawLine()

        settings?.receiptFooter?.let {
            if (it.isNotBlank()) {
                drawCenteredText(canvas, it.take(charsPerLine), pageWidthPx / 2f, y, smallPaint); y += 18
            }
        }
        drawCenteredText(canvas, "Terima kasih", pageWidthPx / 2f, y, smallPaint)

        doc.finishPage(page)
        val fileName = "Receipt-${transaction.transactionNumber}-${paperWidthMm}mm.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out -> doc.writeTo(out) }
        doc.close()
        return Result(file.toUri(), fileName)
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
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, pdfUri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            android.content.Intent.createChooser(intent, "Bagikan Struk")
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun drawCenteredText(canvas: Canvas, text: String, cx: Float, y: Float, paint: Paint) {
        val w = paint.measureText(text)
        canvas.drawText(text, cx - w / 2f, y, paint)
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
