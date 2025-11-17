package id.stargan.intikasir.feature.reports.domain.usecase

import android.content.Context
import id.stargan.intikasir.feature.history.util.ExportUtil
import id.stargan.intikasir.feature.reports.domain.model.*
import id.stargan.intikasir.feature.reports.domain.repository.ReportsRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportReportUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    /**
     * Export transaction report to CSV
     */
    suspend fun exportTransactionReport(
        context: Context,
        filter: ReportFilter
    ): File {
        val report = repository.getTransactionReport(filter)
        val transactions = repository.getTransactions(filter.startDate, filter.endDate).first()
        val itemsMap = repository.getTransactionItems(transactions.map { it.id })

        return ExportUtil.exportToCSV(context, transactions, itemsMap)
    }

    /**
     * Export expense report to CSV
     */
    suspend fun exportExpenseReport(
        context: Context,
        filter: ReportFilter
    ): File {
        val report = repository.getExpenseReport(filter)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Laporan_Pengeluaran_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

        file.bufferedWriter().use { writer ->
            // Header
            writer.write("Tanggal,Kategori,Keterangan,Jumlah,Dibuat Oleh\n")

            // Data
            report.expenses.forEach { exp ->
                writer.write("${dateFormatter.format(Date(exp.date))},")
                writer.write("${getCategoryName(exp.category)},")
                writer.write("\"${exp.description.replace("\"", "\"\"")}\",")
                writer.write("${exp.amount},")
                writer.write("${exp.createdBy}\n")
            }

            // Summary
            writer.write("\n")
            writer.write("RINGKASAN\n")
            writer.write("Total Pengeluaran,${report.summary.totalExpense}\n")
            writer.write("Jumlah Transaksi,${report.summary.totalCount}\n")
            writer.write("Rata-rata,${report.summary.averageExpense}\n")
            writer.write("\n")
            writer.write("PER KATEGORI\n")
            report.summary.byCategory.forEach { (cat, amount) ->
                writer.write("${getCategoryName(cat)},${amount}\n")
            }
        }

        return file
    }

    /**
     * Export profit & loss report to CSV
     */
    suspend fun exportProfitLossReport(
        context: Context,
        startDate: Long,
        endDate: Long
    ): File {
        val report = repository.getProfitLossReport(startDate, endDate)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Laporan_Laba_Rugi_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

        file.bufferedWriter().use { writer ->
            writer.write("LAPORAN LABA RUGI\n")
            writer.write("Periode: ${dateFormatter.format(Date(startDate))} - ${dateFormatter.format(Date(endDate))}\n")
            writer.write("\n")

            writer.write("PENDAPATAN\n")
            writer.write("Penjualan Kotor,${report.revenue.grossSales}\n")
            writer.write("Diskon,(${report.revenue.discounts})\n")
            writer.write("Penjualan Bersih,${report.revenue.netSales}\n")
            writer.write("PPN,${report.revenue.tax}\n")
            writer.write("Total Pendapatan,${report.revenue.netSales + report.revenue.tax}\n")
            writer.write("\n")

            writer.write("PENGELUARAN\n")
            writer.write("Operasional,${report.expenses.operational}\n")
            writer.write("Inventori,${report.expenses.inventory}\n")
            writer.write("Gaji,${report.expenses.salary}\n")
            writer.write("Utilitas,${report.expenses.utilities}\n")
            writer.write("Perawatan,${report.expenses.maintenance}\n")
            writer.write("Marketing,${report.expenses.marketing}\n")
            writer.write("Lain-lain,${report.expenses.other}\n")
            writer.write("Total Pengeluaran,${report.expenses.total}\n")
            writer.write("\n")

            writer.write("LABA BERSIH,${report.netProfit}\n")
            writer.write("Margin Laba,${String.format("%.2f", report.profitMargin)}%\n")
        }

        return file
    }

    /**
     * Export dashboard summary to CSV
     */
    suspend fun exportDashboardSummary(
        context: Context,
        dashboard: ReportDashboard
    ): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Ringkasan_Dashboard_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

        file.bufferedWriter().use { writer ->
            writer.write("RINGKASAN DASHBOARD\n")
            writer.write("Periode: ${dateFormatter.format(Date(dashboard.periodStart))} - ${dateFormatter.format(Date(dashboard.periodEnd))}\n")
            writer.write("\n")

            writer.write("METRIK UTAMA\n")
            writer.write("Total Pendapatan,${dashboard.totalRevenue}\n")
            writer.write("Total Pengeluaran,${dashboard.totalExpense}\n")
            writer.write("Laba Bersih,${dashboard.netProfit}\n")
            writer.write("Jumlah Transaksi,${dashboard.transactionCount}\n")
            writer.write("\n")

            writer.write("PRODUK TERLARIS\n")
            writer.write("Produk,Jumlah Terjual,Pendapatan\n")
            dashboard.topProducts.forEach { product ->
                writer.write("${product.productName},${product.quantitySold},${product.revenue}\n")
            }
            writer.write("\n")

            writer.write("METODE PEMBAYARAN\n")
            writer.write("Metode,Jumlah,Transaksi,Persentase\n")
            dashboard.paymentMethodBreakdown.forEach { pm ->
                writer.write("${pm.method.name},${pm.amount},${pm.count},${String.format("%.2f", pm.percentage)}%\n")
            }
            writer.write("\n")

            writer.write("KATEGORI PENGELUARAN\n")
            writer.write("Kategori,Jumlah,Transaksi,Persentase\n")
            dashboard.expenseCategoryBreakdown.forEach { ec ->
                writer.write("${getCategoryName(ec.category)},${ec.amount},${ec.count},${String.format("%.2f", ec.percentage)}%\n")
            }
        }

        return file
    }

    private fun getCategoryName(category: id.stargan.intikasir.data.local.entity.ExpenseCategory): String {
        return when (category) {
            id.stargan.intikasir.data.local.entity.ExpenseCategory.SUPPLIES -> "Perlengkapan"
            id.stargan.intikasir.data.local.entity.ExpenseCategory.UTILITIES -> "Utilitas"
            id.stargan.intikasir.data.local.entity.ExpenseCategory.RENT -> "Sewa"
            id.stargan.intikasir.data.local.entity.ExpenseCategory.SALARY -> "Gaji"
            id.stargan.intikasir.data.local.entity.ExpenseCategory.MARKETING -> "Marketing"
            id.stargan.intikasir.data.local.entity.ExpenseCategory.MAINTENANCE -> "Perawatan"
            id.stargan.intikasir.data.local.entity.ExpenseCategory.TRANSPORT -> "Transportasi"
            id.stargan.intikasir.data.local.entity.ExpenseCategory.MISC -> "Lain-lain"
        }
    }
}

