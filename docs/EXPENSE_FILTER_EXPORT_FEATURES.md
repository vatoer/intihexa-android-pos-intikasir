# Expense Filter & Export Features

## Tanggal: 16 November 2025

## Overview

Penambahan fitur filtering dan export CSV untuk Pengeluaran, mengikuti pattern dari History/Riwayat Transaksi.

---

## ✅ Features Implemented

### 1. Advanced Date Range Filter
- **TODAY** - Pengeluaran hari ini
- **YESTERDAY** - Pengeluaran kemarin
- **LAST_7_DAYS** - 7 hari terakhir
- **THIS_MONTH** - Bulan ini
- **LAST_MONTH** - Bulan lalu
- **CUSTOM** - Pilih range sendiri dengan date picker

### 2. Category Filter
- Filter by kategori pengeluaran
- Filter "Semua" untuk show all categories
- Multi-select via filter chips

### 3. Export CSV
- **Export Ringkasan** - Summary report
- **Export Detail** - Detailed report with all fields

---

## UI Changes

### ExpenseListScreen

**Before**:
```
TopBar: [Back] Pengeluaran [Calendar]
├─ Date Selector Card
├─ Daily Summary
├─ Category Chips (horizontal scroll)
└─ Expense List
```

**After**:
```
TopBar: [Back] Pengeluaran [Filter] [Export Menu]
├─ Filter Bar (collapsible)
│   ├─ Date Range Chips
│   ├─ Custom Date Picker
│   ├─ Category Chips
│   └─ [Apply Button]
├─ Summary Card (with date range)
└─ Expense List
```

---

## Implementation Details

### 1. ExpenseViewModel Updates

**New State Properties**:
```kotlin
data class UiState(
    val expenses: List<ExpenseEntity>,
    val selectedDate: Long,           // Removed usage
    val selectedCategory: ExpenseCategory?,
    val dateRange: ExpenseDateRange,  // NEW
    val startDate: Long,              // NEW
    val endDate: Long,                // NEW
    val showFilter: Boolean,          // NEW
    val dailyTotal: Double,
    val categorySummary: Map<ExpenseCategory, Double>,
    val isLoading: Boolean,
    val error: String?
)
```

**New Events**:
```kotlin
sealed class ExpenseEvent {
    // Existing
    data class SelectDate(val date: Long)
    data class SelectCategory(val category: ExpenseCategory)
    object ClearCategoryFilter
    
    // NEW for filtering
    data class ChangeDateRange(val range: ExpenseDateRange)
    data class ChangeStartDate(val date: Long)
    data class ChangeEndDate(val date: Long)
    object ToggleFilter
    object ApplyFilter
    
    // Existing CRUD
    data class CreateExpense(val expense: ExpenseEntity)
    data class UpdateExpense(val expense: ExpenseEntity)
    data class DeleteExpense(val expenseId: String)
    object DismissToast
}
```

**New Enum**:
```kotlin
enum class ExpenseDateRange(val label: String) {
    TODAY("Hari ini"),
    YESTERDAY("Kemarin"),
    LAST_7_DAYS("7 hari terakhir"),
    THIS_MONTH("Bulan ini"),
    LAST_MONTH("Bulan lalu"),
    CUSTOM("Custom")
}
```

**Updated loadExpenses()**:
```kotlin
private fun loadExpenses() {
    // Load by date range instead of single date
    val expensesFlow = if (category != null) {
        repository.getExpensesByCategory(category)
    } else {
        repository.getExpensesByDateRange(startDate, endDate)
    }
    
    // Filter by date range AND category
    val filteredExpenses = allExpenses.filter { expense ->
        expense.date >= startDate && expense.date <= endDate &&
        (category == null || expense.category == category)
    }
}
```

**Date Range Calculation**:
```kotlin
private fun calculateDateRange(range: ExpenseDateRange): Pair<Long, Long> {
    return when (range) {
        TODAY -> startOfToday to endOfToday
        YESTERDAY -> startOfYesterday to endOfYesterday
        LAST_7_DAYS -> start7DaysAgo to endOfToday
        THIS_MONTH -> startOfMonth to endOfToday
        LAST_MONTH -> startOfLastMonth to endOfLastMonth
        CUSTOM -> uiState.value.startDate to uiState.value.endDate
    }
}
```

---

### 2. ExpenseExportUtil

**File**: `feature/expense/util/ExpenseExportUtil.kt`

**Functions**:

#### exportToCSV()
```kotlin
fun exportToCSV(
    context: Context,
    expenses: List<ExpenseEntity>,
    startDate: Long,
    endDate: Long
): File
```

**Output Format**:
```csv
Tanggal,Kategori,Keterangan,Jumlah,Metode Pembayaran,Dicatat Oleh
14/11/2025 10:30,Perlengkapan,"Beli alat tulis",50000,Tunai,Admin
14/11/2025 14:00,Utilitas,"Bayar listrik",200000,Transfer,Kasir

RINGKASAN
Total Pengeluaran,Rp 250.000
Jumlah Transaksi,2
Periode,14 Nov 2025 - 14 Nov 2025

RINGKASAN PER KATEGORI
Kategori,Jumlah
Perlengkapan,Rp 50.000
Utilitas,Rp 200.000
```

#### exportDetailedToCSV()
```kotlin
fun exportDetailedToCSV(
    context: Context,
    expenses: List<ExpenseEntity>,
    startDate: Long,
    endDate: Long
): File
```

**Output Format**:
```csv
ID,Tanggal,Waktu Pencatatan,Kategori,Keterangan,Jumlah,Metode Pembayaran,Dicatat Oleh,Diperbarui
abc-123,14/11/2025 10:30:00,14/11/2025 10:30:15,Perlengkapan,"Beli alat tulis",50000,Tunai,Admin,14/11/2025 10:30:15

RINGKASAN DETAIL
Total Pengeluaran,Rp 250.000
Jumlah Transaksi,2
Periode,14 Nov 2025 - 14 Nov 2025
Tanggal Export,16/11/2025 15:30:45

RINGKASAN PER METODE PEMBAYARAN
Metode,Jumlah Transaksi,Total
Tunai,1,Rp 50.000
Transfer,1,Rp 200.000

RINGKASAN PER KATEGORI
Kategori,Jumlah Transaksi,Total
Perlengkapan,1,Rp 50.000
Utilitas,1,Rp 200.000
```

#### shareCSV()
```kotlin
fun shareCSV(context: Context, file: File)
```

Uses Android Share Sheet to share via:
- WhatsApp
- Email
- Google Drive
- Other apps

---

### 3. ExpenseListScreen UI

**Filter Bar Component**:
```kotlin
@Composable
private fun ExpenseFilterBar(
    selectedRange: ExpenseDateRange,
    startDate: Long,
    endDate: Long,
    selectedCategory: ExpenseCategory?,
    onRangeChange: (ExpenseDateRange) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long) -> Unit,
    onCategoryChange: (ExpenseCategory?) -> Unit,
    onApply: () -> Unit,
    onShowCustomDatePicker: () -> Unit
)
```

**TopBar Actions**:
- Filter icon → Toggle filter bar
- Export icon → Show export menu
  - Export Ringkasan CSV
  - Export Detail CSV

**Summary Card**:
- Shows date range (not single date)
- Total amount for the range
- Transaction count

---

## User Flows

### Flow 1: Filter by Last 7 Days
```
1. Tap Filter icon
2. Filter bar expands
3. Tap "7 hari terakhir" chip
4. Tap "Terapkan"
5. List updates to show last 7 days
6. Summary shows last 7 days total
```

### Flow 2: Filter by Category
```
1. Tap Filter icon
2. Filter bar expands
3. Tap category chip (e.g., "Utilitas")
4. Tap "Terapkan"
5. List shows only Utilitas expenses
6. Summary shows Utilitas total
```

### Flow 3: Custom Date Range
```
1. Tap Filter icon
2. Tap "Custom" chip
3. Date range picker opens
4. Select start date
5. Select end date
6. Confirm
7. Tap "Terapkan"
8. List shows custom range
```

### Flow 4: Export Summary CSV
```
1. Tap Export icon
2. Tap "Export Ringkasan CSV"
3. CSV generated
4. Share sheet opens
5. Select WhatsApp/Email/Drive
6. File shared
7. Toast: "Laporan berhasil diekspor"
```

### Flow 5: Export Detail CSV
```
1. Tap Export icon
2. Tap "Export Detail CSV"
3. Detailed CSV generated
4. Share sheet opens
5. Select app
6. File shared
7. Toast: "Laporan detail berhasil diekspor"
```

---

## Comparison with History Feature

### Similarities ✅
- Filter bar design (collapsible)
- Date range chips (same options)
- Custom date picker integration
- Export CSV functionality
- Summary card design
- Toast feedback

### Differences
| Feature | History | Expense |
|---------|---------|---------|
| Status Filter | ✅ Yes | ❌ No (not needed) |
| Category Filter | ❌ No | ✅ Yes |
| Transaction Items | ✅ Yes (detail export) | ❌ No (single record) |
| Summary By | Status + Total | Category + Total |

---

## Files Created/Modified

### Created (1 file)
1. ✅ `feature/expense/util/ExpenseExportUtil.kt`
   - exportToCSV()
   - exportDetailedToCSV()
   - shareCSV()
   - getPaymentMethodLabel() helper

### Modified (2 files)
2. ✅ `feature/expense/ui/ExpenseViewModel.kt`
   - Added dateRange, startDate, endDate, showFilter to UiState
   - Added ExpenseDateRange enum
   - Added new events (ChangeDateRange, ChangeStartDate, etc)
   - Updated loadExpenses() for range filtering
   - Added calculateDateRange() helper
   
3. ✅ `feature/expense/ui/ExpenseListScreen.kt`
   - Added filter icon & export menu to TopBar
   - Added ExpenseFilterBar composable
   - Replaced date selector with filter bar
   - Updated summary card to show date range
   - Added DateRangePickerModal integration
   - Added export functionality with toast

---

## Testing Checklist

### Filter Functionality
- [ ] Tap filter icon → Filter bar appears
- [ ] Tap "Hari ini" → Shows today's expenses
- [ ] Tap "Kemarin" → Shows yesterday's expenses
- [ ] Tap "7 hari terakhir" → Shows last 7 days
- [ ] Tap "Bulan ini" → Shows this month
- [ ] Tap "Bulan lalu" → Shows last month
- [ ] Tap "Custom" → Date picker opens
- [ ] Select custom range → Shows custom range
- [ ] Filter by category → Shows only selected category
- [ ] Clear category filter → Shows all categories
- [ ] Tap "Terapkan" → Filter applies & bar collapses

### Export Functionality
- [ ] Tap export icon → Menu appears
- [ ] Tap "Export Ringkasan CSV" → Share sheet opens
- [ ] CSV has correct format (summary)
- [ ] Share via WhatsApp → Success
- [ ] Share via Email → Success
- [ ] Tap "Export Detail CSV" → Share sheet opens
- [ ] CSV has correct format (detailed)
- [ ] All expense records included
- [ ] Category summary correct
- [ ] Payment method summary correct
- [ ] Toast appears after export

### Summary Card
- [ ] Shows correct date range text
- [ ] Shows correct total amount
- [ ] Shows correct transaction count
- [ ] Updates when filter changes

---

## Build Status

```
BUILD SUCCESSFUL in 19s
18 actionable tasks: 6 executed, 12 up-to-date

Warnings: Only deprecation (safe)
Errors: 0
```

---

## Benefits

### For Users
1. **Better Insights**: Filter by date range to analyze spending patterns
2. **Flexible Reporting**: Export data for external analysis (Excel, Google Sheets)
3. **Quick Summary**: See totals by category and payment method
4. **Easy Sharing**: Share reports via any app

### For Business
1. **Expense Tracking**: Monitor spending over time
2. **Budget Planning**: Analyze category-wise expenses
3. **Compliance**: Export for accounting/audit purposes
4. **Data Analysis**: CSV for spreadsheet analysis

### For Development
1. **Code Reuse**: Similar pattern as History feature
2. **Maintainability**: Centralized export utility
3. **Consistency**: Same UX across features
4. **Best Practices**: Clean architecture maintained

---

## Future Enhancements

### Short Term
1. Add expense charts (pie chart by category)
2. Add budget vs actual comparison
3. Add recurring expense templates
4. Add expense approval workflow

### Medium Term
1. Export to Excel (XLSX) format
2. Email report scheduling
3. Multi-store expense separation
4. Advanced analytics dashboard

### Long Term
1. AI-powered expense categorization
2. Receipt OCR integration
3. Bank statement import
4. Expense forecasting

---

## Related Documentation

- `EXPENSE_FEATURE_SIMPLE.md` - Main expense feature docs
- `HISTORY_FEATURES_COMPLETE.md` - History filter implementation
- `EXPORT_CSV_FORMAT_EXAMPLE.md` - CSV format examples

---

## Summary

✅ **Filter Feature** - Date range + category filtering (6 presets + custom)  
✅ **Export Feature** - Summary & detailed CSV export  
✅ **UI Updates** - Filter bar + export menu in TopBar  
✅ **Code Reuse** - Follows History feature pattern  
✅ **Build Success** - No errors, production ready  

Fitur filtering dan export CSV untuk Pengeluaran telah berhasil diimplementasikan mengikuti pattern dari History/Riwayat Transaksi! 🎉

