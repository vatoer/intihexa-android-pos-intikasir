package id.stargan.intikasir.feature.reports.domain.usecase

import android.content.Context
import id.stargan.intikasir.feature.history.util.ExportUtil
import id.stargan.intikasir.feature.reports.domain.model.*
import id.stargan.intikasir.feature.reports.domain.repository.ReportsRepository
import id.stargan.intikasir.util.DateFormatUtils
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Locale
import javax.inject.Inject
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream

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
        val fileName = "Laporan_Pengeluaran_${DateFormatUtils.fileTimestamp()}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatterPattern = "dd/MM/yyyy"

        file.bufferedWriter().use { writer ->
            // Header
            writer.write("Tanggal,Kategori,Keterangan,Jumlah,Dibuat Oleh\n")

            // Data
            report.expenses.forEach { exp ->
                writer.write("${DateFormatUtils.formatEpochMillis(exp.date, dateFormatterPattern)},")
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
        val fileName = "Laporan_Laba_Rugi_${DateFormatUtils.fileTimestamp()}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatterPattern = "dd/MM/yyyy"

        file.bufferedWriter().use { writer ->
            writer.write("LAPORAN LABA RUGI\n")
            writer.write("Periode: ${DateFormatUtils.formatEpochMillis(startDate, dateFormatterPattern)} - ${DateFormatUtils.formatEpochMillis(endDate, dateFormatterPattern)}\n")
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
            writer.write("Margin Laba,${String.format(Locale.getDefault(), "%.2f", report.profitMargin)}%\n")
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
        val fileName = "Ringkasan_Dashboard_${DateFormatUtils.fileTimestamp()}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatterPattern = "dd/MM/yyyy"

        file.bufferedWriter().use { writer ->
            writer.write("RINGKASAN DASHBOARD\n")
            writer.write("Periode: ${DateFormatUtils.formatEpochMillis(dashboard.periodStart, dateFormatterPattern)} - ${DateFormatUtils.formatEpochMillis(dashboard.periodEnd, dateFormatterPattern)}\n")
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
                writer.write("${pm.method.name},${pm.amount},${pm.count},${String.format(Locale.getDefault(), "%.2f", pm.percentage)}%\n")
            }
            writer.write("\n")

            writer.write("KATEGORI PENGELUARAN\n")
            writer.write("Kategori,Jumlah,Transaksi,Persentase\n")
            dashboard.expenseCategoryBreakdown.forEach { ec ->
                writer.write("${getCategoryName(ec.category)},${ec.amount},${ec.count},${String.format(Locale.getDefault(), "%.2f", ec.percentage)}%\n")
            }
        }

        return file
    }

    /**
     * Export dashboard summary to XLSX
     */
    suspend fun exportDashboardSummaryXlsx(
        context: Context,
        dashboard: ReportDashboard
    ): File {
        val fileName = "Ringkasan_Dashboard_${DateFormatUtils.fileTimestamp()}.xlsx"
        val file = File(context.cacheDir, fileName)

        XSSFWorkbook().use { workbook ->
            val dateFormatterPattern = "dd/MM/yyyy"

            // Summary sheet
            val summarySheet = workbook.createSheet("Summary")
            var rowIndex = 0
            var row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Periode")
            row.createCell(1).setCellValue("${DateFormatUtils.formatEpochMillis(dashboard.periodStart, dateFormatterPattern)} - ${DateFormatUtils.formatEpochMillis(dashboard.periodEnd, dateFormatterPattern)}")

            row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Total Pendapatan")
            row.createCell(1).setCellValue(dashboard.totalRevenue)

            row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Total Pengeluaran")
            row.createCell(1).setCellValue(dashboard.totalExpense)

            row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Laba Bersih")
            row.createCell(1).setCellValue(dashboard.netProfit)

            row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Jumlah Transaksi")
            row.createCell(1).setCellValue(dashboard.transactionCount.toDouble())

            // Top Products sheet
            val topSheet = workbook.createSheet("Top Products")
            var r = topSheet.createRow(0)
            r.createCell(0).setCellValue("Produk")
            r.createCell(1).setCellValue("Jumlah Terjual")
            r.createCell(2).setCellValue("Pendapatan")
            dashboard.topProducts.forEachIndexed { idx, p ->
                val rr = topSheet.createRow(idx + 1)
                rr.createCell(0).setCellValue(p.productName)
                rr.createCell(1).setCellValue(p.quantitySold.toDouble())
                rr.createCell(2).setCellValue(p.revenue)
            }

            // Payment Methods sheet
            val pmSheet = workbook.createSheet("Payment Methods")
            var pr = pmSheet.createRow(0)
            pr.createCell(0).setCellValue("Metode")
            pr.createCell(1).setCellValue("Jumlah")
            pr.createCell(2).setCellValue("Transaksi")
            pr.createCell(3).setCellValue("Persentase")
            dashboard.paymentMethodBreakdown.forEachIndexed { idx, pm ->
                val rr = pmSheet.createRow(idx + 1)
                rr.createCell(0).setCellValue(pm.method.name)
                rr.createCell(1).setCellValue(pm.amount)
                rr.createCell(2).setCellValue(pm.count.toDouble())
                rr.createCell(3).setCellValue(pm.percentage)
            }

            // Expense categories sheet
            val ecSheet = workbook.createSheet("Expense Categories")
            var er = ecSheet.createRow(0)
            er.createCell(0).setCellValue("Kategori")
            er.createCell(1).setCellValue("Jumlah")
            er.createCell(2).setCellValue("Transaksi")
            er.createCell(3).setCellValue("Persentase")
            dashboard.expenseCategoryBreakdown.forEachIndexed { idx, ec ->
                val rr = ecSheet.createRow(idx + 1)
                rr.createCell(0).setCellValue(getCategoryName(ec.category))
                rr.createCell(1).setCellValue(ec.amount)
                rr.createCell(2).setCellValue(ec.count.toDouble())
                rr.createCell(3).setCellValue(ec.percentage)
            }

            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
        }

        return file
    }

    /**
     * Export worst products report to CSV
     */
    suspend fun exportWorstProducts(
        context: Context,
        worstReport: WorstProductsReport,
        periodStart: Long,
        periodEnd: Long
    ): File {
        val fileName = "Worst_Products_${DateFormatUtils.fileTimestamp()}.csv"
        val file = File(context.cacheDir, fileName)

        val dateFormatterPattern = "dd/MM/yyyy"

        file.bufferedWriter().use { writer ->
            writer.write("LAPORAN WORST PRODUCTS\n")
            writer.write("Periode: ${DateFormatUtils.formatEpochMillis(periodStart, dateFormatterPattern)} - ${DateFormatUtils.formatEpochMillis(periodEnd, dateFormatterPattern)}\n")
            writer.write("\n")

            writer.write("WORST PRODUCTS (terurut dari jumlah terjual paling sedikit)\n")
            writer.write("Produk,Jumlah Terjual,Pendapatan\n")
            worstReport.worstProducts.forEach { p ->
                writer.write("${p.productName},${p.quantitySold},${p.revenue}\n")
            }

            writer.write("\nNOT SOLD (produk dengan penjualan 0)\n")
            writer.write("Produk,Stok\n")
            worstReport.notSold.forEach { p ->
                writer.write("${p.productName},${p.stock}\n")
            }
        }

        return file
    }

    /**
     * Export worst products report to XLSX
     */
    suspend fun exportWorstProductsXlsx(
        context: Context,
        worstReport: WorstProductsReport,
        periodStart: Long,
        periodEnd: Long
    ): File {
        val fileName = "Worst_Products_${DateFormatUtils.fileTimestamp()}.xlsx"
        val file = File(context.cacheDir, fileName)

        XSSFWorkbook().use { workbook ->
            val dateFormatterPattern = "dd/MM/yyyy"

            // Worst products sheet
            val worstSheet = workbook.createSheet("Worst Products")
            var r = worstSheet.createRow(0)
            r.createCell(0).setCellValue("Produk")
            r.createCell(1).setCellValue("Jumlah Terjual")
            r.createCell(2).setCellValue("Pendapatan")
            worstReport.worstProducts.forEachIndexed { idx, p ->
                val rr = worstSheet.createRow(idx + 1)
                rr.createCell(0).setCellValue(p.productName)
                rr.createCell(1).setCellValue(p.quantitySold.toDouble())
                rr.createCell(2).setCellValue(p.revenue)
            }

            // Not sold sheet
            val notSoldSheet = workbook.createSheet("Not Sold")
            var nr = notSoldSheet.createRow(0)
            nr.createCell(0).setCellValue("Produk")
            nr.createCell(1).setCellValue("Stok")
            worstReport.notSold.forEachIndexed { idx, p ->
                val rr = notSoldSheet.createRow(idx + 1)
                rr.createCell(0).setCellValue(p.productName)
                rr.createCell(1).setCellValue(p.stock.toDouble())
            }

            FileOutputStream(file).use { fos ->
                workbook.write(fos)
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
