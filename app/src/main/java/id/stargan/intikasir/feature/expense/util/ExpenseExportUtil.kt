package id.stargan.intikasir.feature.expense.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.feature.expense.ui.components.getCategoryLabel
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ExpenseExportUtil {

    /**
     * Export expenses to CSV
     */
    fun exportToCSV(
        context: Context,
        expenses: List<ExpenseEntity>,
        startDate: Long,
        endDate: Long
    ): File {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        val fileName = "Pengeluaran_${dateFormat.format(Date(startDate))}_${dateFormat.format(Date(endDate))}.csv"
        val file = File(context.cacheDir, fileName)

        FileWriter(file).use { writer ->
            // Header
            writer.append("Tanggal,Kategori,Keterangan,Jumlah,Metode Pembayaran,Dicatat Oleh\n")

            // Data rows
            expenses.forEach { expense ->
                writer.append("${dateTimeFormat.format(Date(expense.date))},")
                writer.append("${getCategoryLabel(expense.category)},")
                writer.append("\"${expense.description.replace("\"", "\"\"")}\",")
                writer.append("${expense.amount},")
                writer.append("${getPaymentMethodLabel(expense.paymentMethod.name)},")
                writer.append("${expense.createdByName}\n")
            }

            // Summary
            writer.append("\n")
            writer.append("RINGKASAN\n")
            writer.append("Total Pengeluaran,${currency.format(expenses.sumOf { it.amount }).replace("Rp", "Rp ")}\n")
            writer.append("Jumlah Transaksi,${expenses.size}\n")
            writer.append("Periode,${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}\n")

            // Category summary
            writer.append("\n")
            writer.append("RINGKASAN PER KATEGORI\n")
            writer.append("Kategori,Jumlah\n")
            expenses.groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .forEach { (category, total) ->
                    writer.append("${getCategoryLabel(category)},${currency.format(total).replace("Rp", "Rp ")}\n")
                }
        }

        return file
    }

    /**
     * Export detailed expenses to CSV
     */
    fun exportDetailedToCSV(
        context: Context,
        expenses: List<ExpenseEntity>,
        startDate: Long,
        endDate: Long
    ): File {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
        val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        val fileName = "Pengeluaran_Detail_${dateFormat.format(Date(startDate))}_${dateFormat.format(Date(endDate))}.csv"
        val file = File(context.cacheDir, fileName)

        FileWriter(file).use { writer ->
            // Header
            writer.append("ID,Tanggal,Waktu Pencatatan,Kategori,Keterangan,Jumlah,Metode Pembayaran,Dicatat Oleh,Diperbarui\n")

            // Data rows
            expenses.forEach { expense ->
                writer.append("${expense.id},")
                writer.append("${dateTimeFormat.format(Date(expense.date))},")
                writer.append("${dateTimeFormat.format(Date(expense.createdAt))},")
                writer.append("${getCategoryLabel(expense.category)},")
                writer.append("\"${expense.description.replace("\"", "\"\"")}\",")
                writer.append("${expense.amount},")
                writer.append("${getPaymentMethodLabel(expense.paymentMethod.name)},")
                writer.append("${expense.createdByName},")
                writer.append("${dateTimeFormat.format(Date(expense.updatedAt))}\n")
            }

            // Summary
            writer.append("\n")
            writer.append("RINGKASAN DETAIL\n")
            writer.append("Total Pengeluaran,${currency.format(expenses.sumOf { it.amount }).replace("Rp", "Rp ")}\n")
            writer.append("Jumlah Transaksi,${expenses.size}\n")
            writer.append("Periode,${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}\n")
            writer.append("Tanggal Export,${dateTimeFormat.format(Date())}\n")

            // Payment method summary
            writer.append("\n")
            writer.append("RINGKASAN PER METODE PEMBAYARAN\n")
            writer.append("Metode,Jumlah Transaksi,Total\n")
            expenses.groupBy { it.paymentMethod }
                .forEach { (method, list) ->
                    writer.append("${getPaymentMethodLabel(method.name)},${list.size},${currency.format(list.sumOf { it.amount }).replace("Rp", "Rp ")}\n")
                }

            // Category summary
            writer.append("\n")
            writer.append("RINGKASAN PER KATEGORI\n")
            writer.append("Kategori,Jumlah Transaksi,Total\n")
            expenses.groupBy { it.category }
                .forEach { (category, list) ->
                    writer.append("${getCategoryLabel(category)},${list.size},${currency.format(list.sumOf { it.amount }).replace("Rp", "Rp ")}\n")
                }
        }

        return file
    }

    /**
     * Share CSV file
     */
    fun shareCSV(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
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
            Intent.createChooser(intent, "Bagikan Laporan Pengeluaran")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun getPaymentMethodLabel(method: String): String {
        return when (method) {
            "CASH" -> "Tunai"
            "QRIS" -> "QRIS"
            "CARD" -> "Kartu"
            "TRANSFER" -> "Transfer"
            else -> method
        }
    }
}

