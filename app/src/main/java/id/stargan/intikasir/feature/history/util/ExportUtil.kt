package id.stargan.intikasir.feature.history.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ExportUtil {

    fun exportToCSV(
        context: Context,
        transactions: List<TransactionEntity>,
        itemsMap: Map<String, List<TransactionItemEntity>>
    ): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Transaksi_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))

        file.bufferedWriter().use { writer ->
            // Header
            writer.write("No Transaksi,Tanggal,Kasir,Status,Metode Bayar,Subtotal,PPN,Diskon,Total,Dibayar,Kembalian,Jumlah Item\n")

            // Data
            transactions.forEach { tx ->
                val itemCount = itemsMap[tx.id]?.size ?: 0
                writer.write("${tx.transactionNumber},")
                writer.write("${dateFormatter.format(Date(tx.transactionDate))},")
                writer.write("${tx.cashierName},")
                writer.write("${tx.status.name},")
                writer.write("${tx.paymentMethod.name},")
                writer.write("${tx.subtotal},")
                writer.write("${tx.tax},")
                writer.write("${tx.discount},")
                writer.write("${tx.total},")
                writer.write("${tx.cashReceived},")
                writer.write("${tx.cashChange},")
                writer.write("$itemCount\n")
            }
        }

        return file
    }

    fun exportDetailedToCSV(
        context: Context,
        transactions: List<TransactionEntity>,
        itemsMap: Map<String, List<TransactionItemEntity>>
    ): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Transaksi_Detail_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))

        file.bufferedWriter().use { writer ->
            // Header - Format: No Transaksi → Detail per item
            writer.write("No Transaksi,Tanggal,Kasir,Status,Metode Bayar,Produk,Qty,Harga Satuan,Diskon Item,Subtotal Item,Total Transaksi\n")

            // Data - Satu baris per item
            transactions.forEach { tx ->
                val items = itemsMap[tx.id] ?: emptyList()

                if (items.isEmpty()) {
                    // Jika tidak ada item, tetap tampilkan transaksi
                    writer.write("${tx.transactionNumber},")
                    writer.write("${dateFormatter.format(Date(tx.transactionDate))},")
                    writer.write("${tx.cashierName},")
                    writer.write("${tx.status.name},")
                    writer.write("${tx.paymentMethod.name},")
                    writer.write("-,0,0,0,0,")
                    writer.write("${tx.total}\n")
                } else {
                    // Setiap item mendapat baris tersendiri dengan nomor transaksi yang sama
                    items.forEach { item ->
                        writer.write("${tx.transactionNumber},")
                        writer.write("${dateFormatter.format(Date(tx.transactionDate))},")
                        writer.write("${tx.cashierName},")
                        writer.write("${tx.status.name},")
                        writer.write("${tx.paymentMethod.name},")
                        writer.write("\"${item.productName.replace("\"", "\"\"")}\",") // Escape double quotes
                        writer.write("${item.quantity},")
                        writer.write("${item.unitPrice},")
                        writer.write("${item.discount},")
                        writer.write("${item.subtotal},")
                        writer.write("${tx.total}\n")
                    }
                }
            }
        }

        return file
    }

    fun shareCSV(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Bagikan Laporan")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

