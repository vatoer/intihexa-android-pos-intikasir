# Reports Feature - Component Structure

Struktur komponen Reports Screen telah direfactor menjadi komponen-komponen kecil yang reusable dan mudah di-maintain.

## Struktur Direktori

```
feature/reports/ui/
├── ReportsScreen.kt           # Main screen orchestrator
├── ReportsViewModel.kt         # ViewModel
├── ReportsEvent.kt            # UI Events
├── ReportsUiState.kt          # UI State
├── ReportTab.kt               # Tab enum
├── components/                # UI Components
│   ├── ReportsTopBar.kt       # Top app bar dengan actions
│   ├── ReportsTabRow.kt       # Tab row (Dashboard/Laba Rugi)
│   ├── DashboardContent.kt    # Dashboard tab content
│   ├── ProfitLossContent.kt   # Profit/Loss tab content
│   ├── SummaryCards.kt        # Summary cards component
│   ├── RevenueExpenseTrendLineChart.kt
│   ├── PaymentMethodPieChart.kt
│   ├── ExpenseCategoryBarChart.kt
│   ├── TopProductsCard.kt
│   ├── PeriodInfoCard.kt
│   ├── NetProfitCard.kt
│   ├── RevenueBreakdownCard.kt
│   └── ExpenseBreakdownCard.kt
├── dialogs/                   # Dialog components
│   ├── PeriodPickerDialog.kt  # Dialog pilih periode
│   └── ExportDialog.kt        # Dialog export CSV/PDF
└── utils/                     # Utility functions
    └── PeriodUtils.kt         # Helper untuk period label
```

## Component Responsibilities

### ReportsScreen.kt
- Main orchestrator
- Mengelola state dan events
- Menampilkan dialog-dialog
- Koordinasi antara komponen

### ReportsTopBar.kt
- TopAppBar dengan navigasi back
- Period selector button
- Refresh action
- Export action

### ReportsTabRow.kt
- Tab switcher antara Dashboard dan Laba Rugi
- Menggunakan Material 3 PrimaryTabRow

### DashboardContent.kt
- Menampilkan konten dashboard
- Summary cards
- Charts (line, pie, bar)
- Top products

### ProfitLossContent.kt
- Menampilkan laporan laba rugi
- Period info
- Net profit summary
- Revenue & expense breakdown

### Dialogs
- **PeriodPickerDialog**: Dialog untuk memilih periode waktu
- **ExportDialog**: Dialog untuk memilih format export (CSV/PDF)

## Design Principles Applied

1. **Single Responsibility**: Setiap komponen punya satu tanggung jawab
2. **Composability**: Komponen kecil yang bisa digabungkan
3. **Reusability**: Komponen bisa digunakan kembali
4. **Testability**: Mudah untuk di-test secara terpisah
5. **Maintainability**: Mudah untuk di-maintain dan di-update

## Usage Example

```kotlin
// Main screen
ReportsScreen(
    onNavigateBack = { navController.popBackStack() }
)

// Individual components can be used separately
ReportsTopBar(
    selectedPeriod = PeriodType.TODAY,
    onNavigateBack = { },
    onPeriodClick = { },
    onRefreshClick = { },
    onExportClick = { }
)
```

## Benefits

✅ **Modularity**: Komponen terpisah dan independen  
✅ **Readability**: Kode lebih mudah dibaca  
✅ **Reusability**: Komponen bisa digunakan di tempat lain  
✅ **Testability**: Mudah membuat unit test per komponen  
✅ **Maintainability**: Perubahan lebih mudah dan terisolasi  
✅ **Scalability**: Mudah menambah fitur baru  

## Future Improvements

- [ ] Add unit tests untuk setiap komponen
- [ ] Implement export CSV/PDF functionality
- [ ] Add custom date range picker
- [ ] Add animation transitions
- [ ] Add loading skeleton screens
- [ ] Add error states per component

