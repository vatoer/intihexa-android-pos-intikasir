# Fitur Laporan - IntiKasir PoS

## 📊 Overview

Fitur laporan menyediakan insight komprehensif tentang performa bisnis dengan berbagai jenis laporan, visualisasi data, dan kemampuan export.

---

## 🎯 Fitur Utama

### 1. **Dashboard Laporan**
Ringkasan visual dengan metrik-metrik penting:

- **Summary Cards**
  - Total Pendapatan
  - Total Pengeluaran  
  - Laba Bersih
  - Jumlah Transaksi

- **Grafik Tren**
  - Tren pendapatan harian/mingguan/bulanan
  - Tren pengeluaran
  - Perbandingan visual

- **Top Products**
  - Produk terlaris berdasarkan jumlah terjual
  - Revenue per produk

- **Payment Method Breakdown**
  - Distribusi metode pembayaran
  - Persentase per metode
  - Jumlah transaksi per metode

- **Expense Category Breakdown**
  - Distribusi kategori pengeluaran
  - Persentase per kategori
  - Analisis pengeluaran

### 2. **Laporan Laba Rugi (Profit & Loss)**
Laporan keuangan komprehensif:

- **Rincian Pendapatan**
  - Penjualan Kotor
  - Diskon
  - Penjualan Bersih
  - PPN
  - Total Pendapatan

- **Rincian Pengeluaran**
  - Per kategori (Operasional, Inventori, Gaji, dll)
  - Total Pengeluaran

- **Perhitungan Laba**
  - Laba/Rugi Bersih
  - Margin Laba (%)

### 3. **Filter & Period Selection**
Fleksibilitas dalam memilih periode:

- **Preset Periods**
  - Hari Ini
  - Kemarin
  - Minggu Ini
  - Minggu Lalu
  - Bulan Ini
  - Bulan Lalu
  - Tahun Ini

- **Custom Period**
  - Pilih tanggal mulai dan akhir sendiri

### 4. **Export Functionality**
Export laporan ke berbagai format:

- **CSV/Excel Export**
  - Ringkasan Dashboard
  - Laporan Transaksi Detail
  - Laporan Pengeluaran
  - Laporan Laba Rugi
  - Compatible dengan Excel, Google Sheets

- **PDF Export** (Future)
  - Format profesional untuk print
  - Layout yang rapi

---

## 🏗️ Arsitektur

### Clean Architecture

```
presentation/
├── ui/
│   ├── ReportsScreen.kt          # Main screen
│   ├── ReportsViewModel.kt       # Business logic & state
│   └── components/
│       └── ReportComponents.kt   # Reusable UI components
│
domain/
├── model/
│   └── ReportModels.kt           # Data models
├── repository/
│   └── ReportsRepository.kt      # Repository interface
└── usecase/
    ├── GetDashboardDataUseCase.kt
    ├── GetProfitLossReportUseCase.kt
    └── ExportReportUseCase.kt
│
data/
└── repository/
    └── ReportsRepositoryImpl.kt  # Repository implementation
```

### Dependency Injection (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class ReportsModule {
    @Binds
    @Singleton
    abstract fun bindReportsRepository(
        impl: ReportsRepositoryImpl
    ): ReportsRepository
}
```

---

## 📋 Data Models

### ReportDashboard
```kotlin
data class ReportDashboard(
    val totalRevenue: Double,
    val totalExpense: Double,
    val netProfit: Double,
    val transactionCount: Int,
    val dailyRevenue: List<DailyData>,
    val dailyExpense: List<DailyData>,
    val topProducts: List<ProductSales>,
    val paymentMethodBreakdown: List<PaymentMethodData>,
    val expenseCategoryBreakdown: List<ExpenseCategoryData>,
    val periodStart: Long,
    val periodEnd: Long
)
```

### ProfitLossReport
```kotlin
data class ProfitLossReport(
    val revenue: RevenueBreakdown,
    val expenses: ExpenseBreakdown,
    val netProfit: Double,
    val profitMargin: Double,
    val periodStart: Long,
    val periodEnd: Long
)
```

---

## 💻 Usage

### Navigation
```kotlin
// Di HomeNavGraph.kt
composable(HomeRoutes.REPORTS) {
    ReportsScreen(
        onNavigateBack = { navController.navigateUp() }
    )
}
```

### ViewModel Events
```kotlin
// Select period
viewModel.onEvent(ReportsEvent.SelectPeriod(PeriodType.THIS_MONTH))

// Switch tab
viewModel.onEvent(ReportsEvent.SelectTab(ReportTab.PROFIT_LOSS))

// Refresh data
viewModel.onEvent(ReportsEvent.Refresh)

// Show export dialog
viewModel.onEvent(ReportsEvent.ShowExportDialog)
```

### Export Reports
```kotlin
// Export transaction report
val file = exportReportUseCase.exportTransactionReport(
    context = context,
    filter = ReportFilter(
        startDate = startDate,
        endDate = endDate
    )
)
ExportUtil.shareCSV(context, file)

// Export dashboard summary
val file = exportReportUseCase.exportDashboardSummary(
    context = context,
    dashboard = dashboard
)
ExportUtil.shareCSV(context, file)
```

---

## 🎨 UI Components

### Summary Cards
Kartu metrik dengan warna semantic:
- Revenue → Green (Income color)
- Expense → Red (Expense color)
- Profit → Blue/Red (based on value)
- Transaction Count → Primary color

### Charts (Future Enhancement)
Untuk visualisasi yang lebih baik, pertimbangkan integrasi library:
- **MPAndroidChart** - Mature, feature-rich
- **Vico** - Modern, Jetpack Compose native
- **Compose Charts** - Lightweight alternative

### Export Dialog
Simple dialog dengan pilihan:
- Export CSV (Excel)
- Export PDF

---

## 📊 Database Queries

### Revenue Calculation
```sql
SELECT SUM(total) FROM transactions 
WHERE transactionDate BETWEEN :startDate AND :endDate 
AND status = 'COMPLETED'
AND isDeleted = 0
```

### Expense Calculation
```sql
SELECT SUM(amount) FROM expenses 
WHERE date >= :startDate AND date <= :endDate 
AND isDeleted = 0
```

### Top Selling Products
```sql
SELECT transaction_items.*, SUM(quantity) as totalQuantity 
FROM transaction_items 
INNER JOIN transactions ON transaction_items.transactionId = transactions.id
WHERE transactions.transactionDate BETWEEN :startDate AND :endDate
AND transactions.status = 'COMPLETED'
AND transactions.isDeleted = 0
GROUP BY productId
ORDER BY totalQuantity DESC
LIMIT :limit
```

---

## 📤 Export Format

### CSV Format (Transaction Report)
```csv
No Transaksi,Tanggal,Kasir,Status,Metode Bayar,Subtotal,PPN,Diskon,Total,Dibayar,Kembalian,Jumlah Item
INV-001,01/11/2025 10:30,Admin,COMPLETED,CASH,100000,10000,5000,105000,110000,5000,3
```

### CSV Format (Expense Report)
```csv
Tanggal,Kategori,Keterangan,Jumlah,Dibuat Oleh
01/11/2025,Operasional,Beli ATK,50000,Admin
```

### CSV Format (Profit & Loss)
```csv
LAPORAN LABA RUGI
Periode: 01 November 2025 - 30 November 2025

PENDAPATAN
Penjualan Kotor,5000000
Diskon,(250000)
Penjualan Bersih,4750000
PPN,475000
Total Pendapatan,5225000

PENGELUARAN
Operasional,500000
Inventori,1000000
...
Total Pengeluaran,2000000

LABA BERSIH,3225000
Margin Laba,61.72%
```

---

## 🔧 Configuration

### Date Format
```kotlin
private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
```

### Currency Format
```kotlin
private val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 0
}
```

---

## ✅ Best Practices

1. **Performance**
   - Use Flow untuk reactive updates
   - Implement pagination untuk large datasets
   - Cache calculated values

2. **UX**
   - Loading states untuk async operations
   - Error handling dengan snackbar
   - Pull-to-refresh untuk manual reload

3. **Data Accuracy**
   - Filter COMPLETED transactions only
   - Exclude soft-deleted records
   - Proper date range calculations

4. **Export**
   - Generate files in cache directory
   - Use FileProvider untuk sharing
   - Clean up old export files

---

## 🚀 Future Enhancements

### Phase 1 (Current) ✅
- [x] Dashboard dengan summary metrics
- [x] Profit & Loss report
- [x] Period selection
- [x] CSV export

### Phase 2 (Planned)
- [ ] Chart visualization (MPAndroidChart/Vico)
- [ ] PDF export dengan custom template
- [ ] Email report directly
- [ ] Scheduled reports

### Phase 3 (Advanced)
- [ ] Trend analysis & forecasting
- [ ] Comparative reports (vs previous period)
- [ ] Custom report builder
- [ ] Real-time dashboard
- [ ] Advanced filters (by product, cashier, etc)

### Phase 4 (Analytics)
- [ ] Sales analytics dashboard
- [ ] Customer behavior insights
- [ ] Inventory turnover analysis
- [ ] Cashier performance report

---

## 📱 Screenshots Mock

### Dashboard Tab
```
┌─────────────────────────────────────┐
│  Laporan          [Bulan Ini ▼] ⟳ ⬇ │
├─────────────────────────────────────┤
│ [Dashboard] [Laba Rugi]             │
├─────────────────────────────────────┤
│                                     │
│  ┌──────────┐ ┌──────────┐         │
│  │Pendapatan│ │Pengeluaran│        │
│  │Rp 5.2 jt │ │Rp 2.0 jt │         │
│  └──────────┘ └──────────┘         │
│  ┌──────────┐ ┌──────────┐         │
│  │Laba Bers │ │ Transaksi│         │
│  │Rp 3.2 jt │ │   125    │         │
│  └──────────┘ └──────────┘         │
│                                     │
│  ┌────────────────────────────────┐ │
│  │ Tren Pendapatan & Pengeluaran  │ │
│  │  [Chart Placeholder]           │ │
│  └───────────────���────────────────┘ │
│                                     │
│  ┌────────────────────────────────┐ │
│  │ Produk Terlaris                │ │
│  │  1. Kopi Susu   150x  Rp 750k  │ │
│  │  2. Nasi Goreng  80x  Rp 640k  │ │
│  └────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### Profit & Loss Tab
```
┌─────────────────────────────────────┐
│  Laporan          [Bulan Ini ▼] ⟳ ⬇ │
├─────────────────────────────────────┤
│ [Dashboard] [Laba Rugi]             │
├─────────────────────────────────────┤
│                                     │
│  ┌────────────────────────────────┐ │
│  │   Periode: 1-30 Nov 2025       │ │
│  └────────────────────────────────┘ │
│                                     │
│  ┌────────────────────────────────┐ │
│  │      Laba Bersih                │ │
│  │      Rp 3,225,000               │ │
│  │      Margin: 61.72%             │ │
│  └────────────────────────────────┘ │
│                                     │
│  ┌────────────────────────────────┐ │
│  │  Rincian Pendapatan             │ │
│  │  Penjualan Kotor   Rp 5,000,000│ │
│  │  Diskon           (Rp   250,000)│ │
│  │  ────────────────────────────── │ │
│  │  Penjualan Bersih  Rp 4,750,000│ │
│  │  PPN               Rp   475,000│ │
│  │  ══════════════════════════════ │ │
│  │  Total Pendapatan  Rp 5,225,000│ │
│  └────────────────────────────────┘ │
│                                     │
│  ┌────────────────────────────────┐ │
│  │  Rincian Pengeluaran            │ │
│  │  Operasional       Rp   500,000│ │
│  │  Inventori         Rp 1,000,000│ │
│  │  ...                            │ │
│  │  ══════════════════════════════ │ │
│  │  Total Pengeluaran Rp 2,000,000│ │
│  └────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

---

## 📚 Related Documentation

- `/docs/IMPLEMENTATION_SUMMARY.md` - Overall project summary
- `/docs/POS_REACTIVE_IMPLEMENTATION_SUMMARY.md` - POS feature
- `/docs/EXPENSE_FEATURE_SIMPLE.md` - Expense feature
- `/docs/HISTORY_FEATURES_COMPLETE.md` - Transaction history

---

**Feature Status**: ✅ **COMPLETE & READY TO USE**

- Dashboard laporan dengan metrik utama
- Laporan laba rugi
- Period selection (preset + custom)
- Export CSV untuk semua report types
- Clean architecture dengan Hilt DI
- Material 3 UI dengan Fresh Commerce color scheme

**Next Steps**:
1. Test dengan data real
2. Add chart visualization library
3. Implement PDF export
4. Add more advanced filters

---

**Dibuat**: 17 November 2025  
**Version**: 1.0.0  
**Author**: AI Assistant for IntiKasir PoS

