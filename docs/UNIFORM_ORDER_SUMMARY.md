# Uniform Order Summary Implementation

## Tanggal: 18 November 2025

## Tujuan
Menyeragamkan tampilan ringkasan pesanan di semua screen menggunakan `OrderSummaryCard` component yang reusable.

---

## OrderSummaryCard Component

### Lokasi:
`/app/src/main/java/id/stargan/intikasir/feature/pos/ui/components/OrderSummaryCard.kt`

### Features:
- ✅ **Gross Subtotal** - Total sebelum diskon item
- ✅ **Item Discount** - Total diskon dari item
- ✅ **Net Subtotal** - Subtotal setelah diskon item
- ✅ **Tax (PPN)** - Pajak dengan persentase
- ✅ **Global Discount** - Diskon keseluruhan transaksi
- ✅ **Total** - Total akhir yang harus dibayar

### Design:
- Card dengan background `primaryContainer`
- Consistent typography dan spacing
- Clear hierarchy dengan dividers
- Bold total untuk emphasis
- Conditional rendering (hanya tampil jika nilai > 0)

---

## Implementation

### 1. ✅ Payment Screen
**File:** `PaymentScreenReactive.kt`

**Status:** ✅ Already implemented

**Usage:**
```kotlin
OrderSummaryCard(
    grossSubtotal = grossSubtotal,
    itemDiscount = itemDiscount,
    netSubtotal = state.subtotal,
    taxRate = taxRate,
    taxAmount = state.tax,
    globalDiscount = state.globalDiscount,
    total = state.total
)
```

**Location:** Displayed at top of payment screen before payment method selection

---

### 2. ✅ History Detail Screen
**File:** `HistoryScreens.kt`

**Status:** ✅ Implemented (NEW)

**Changes:**
- Replaced manual totals display with `OrderSummaryCard`
- Added calculation for gross subtotal and item discount
- Separated cash payment details into own card

**Before:**
```kotlin
// Manual text display
Column {
    Text("Subtotal: $subtotalStr")
    if (tx.tax > 0) Text("PPN: $taxStr")
    if (tx.discount > 0) Text("Diskon: -$discountStr")
    Text("Total: $totalStr", fontWeight = FontWeight.Bold)
    if (tx.cashReceived > 0) {
        Text("Dibayar: $receivedStr")
        Text("Kembalian: $changeStr")
    }
}
```

**After:**
```kotlin
// Using OrderSummaryCard
val grossSubtotal = uiState.items.sumOf { it.unitPrice * it.quantity }
val itemDiscount = uiState.items.sumOf { it.discount }
val netSubtotal = tx.subtotal
val taxRate = if (netSubtotal > 0 && tx.tax > 0) tx.tax / netSubtotal else 0.0

OrderSummaryCard(
    grossSubtotal = grossSubtotal,
    itemDiscount = itemDiscount,
    netSubtotal = netSubtotal,
    taxRate = taxRate,
    taxAmount = tx.tax,
    globalDiscount = tx.discount,
    total = tx.total
)

// Cash details in separate card (if applicable)
if (tx.cashReceived > 0) {
    Card {
        Column {
            Text("Detail Pembayaran Tunai")
            Row { Text("Tunai Diterima") Text(received) }
            Row { Text("Kembalian") Text(change) }
        }
    }
}
```

**Benefits:**
- ✅ Consistent visual design with Payment screen
- ✅ Clear breakdown of discounts and taxes
- ✅ Shows gross vs net subtotal
- ✅ Separated cash payment details for clarity

---

### 3. ℹ️ Receipt Screen
**File:** `ReceiptScreen.kt`

**Status:** ℹ️ Kept as is (intentional)

**Reason:**
- Receipt screen is a simple success confirmation
- Only shows essential info: payment method, total, cash details
- Full breakdown already shown in Payment screen before
- Keeping it simple for better UX (less information overload)

**Display:**
```
Detail Pembayaran
─────────────────
Metode Pembayaran: CASH
Total Belanja: Rp 100.000
Tunai Diterima: Rp 150.000
Kembalian: Rp 50.000
```

---

## Screen-by-Screen Comparison

### Payment Screen
```
┌─────────────────────────────┐
│ Ringkasan Pesanan           │
│ ─────────────────────────── │
│ Subtotal (bruto)  Rp 50.000 │
│ Diskon item      -Rp  5.000 │
│ Subtotal          Rp 45.000 │
│ PPN (11%)         Rp  4.950 │
│ Diskon global    -Rp  2.000 │
│ ─────────────────────────── │
│ Total             Rp 47.950 │ ✅
└─────────────────────────────┘
```

### History Detail Screen
```
┌─────────────────────────────┐
│ Ringkasan Pesanan           │
│ ─────────────────────────── │
│ Subtotal (bruto)  Rp 50.000 │
│ Diskon item      -Rp  5.000 │
│ Subtotal          Rp 45.000 │
│ PPN (11%)         Rp  4.950 │
│ Diskon global    -Rp  2.000 │
│ ─────────────────────────── │
│ Total             Rp 47.950 │ ✅
└─────────────────────────────┘

┌─────────────────────────────┐
│ Detail Pembayaran Tunai     │
│ ─────────────────────────── │
│ Tunai Diterima   Rp 50.000  │
│ Kembalian        Rp  2.050  │
└─────────────────────────────┘
```

### Receipt Screen (Simple)
```
┌─────────────────────────────┐
│ Detail Pembayaran           │
│ ─────────────────────────── │
│ Metode Pembayaran     CASH  │
│ Total Belanja     Rp 47.950 │
│ Tunai Diterima    Rp 50.000 │
│ Kembalian         Rp  2.050 │
└─────────────────────────────┘
```

---

## Benefits

### 1. **Consistency**
- ✅ Same visual design across screens
- ✅ Same information hierarchy
- ✅ Same styling and spacing
- ✅ Reduces cognitive load

### 2. **Maintainability**
- ✅ Single source of truth for order summary UI
- ✅ Changes to OrderSummaryCard affect all screens
- ✅ Less code duplication
- ✅ Easier to test

### 3. **User Experience**
- ✅ Users recognize the pattern
- ✅ Easier to understand breakdown
- ✅ Clear visibility of discounts
- ✅ Professional appearance

### 4. **Flexibility**
- ✅ Component handles conditional rendering
- ✅ Shows only relevant fields
- ✅ Adapts to different transaction types
- ✅ Reusable in future screens

---

## Calculation Logic

### Gross Subtotal
```kotlin
val grossSubtotal = items.sumOf { it.unitPrice * it.quantity }
```
Total sebelum diskon apapun.

### Item Discount
```kotlin
val itemDiscount = items.sumOf { it.discount }
```
Total diskon dari masing-masing item.

### Net Subtotal
```kotlin
val netSubtotal = grossSubtotal - itemDiscount
// or from transaction.subtotal
```
Subtotal setelah diskon item, sebelum tax.

### Tax Rate
```kotlin
val taxRate = if (netSubtotal > 0 && tax > 0) {
    tax / netSubtotal
} else {
    0.0
}
```
Persentase PPN (biasanya 0.11 untuk 11%).

### Total
```kotlin
val total = netSubtotal + tax - globalDiscount
```
Total akhir yang harus dibayar.

---

## Files Modified

1. ✅ `feature/history/ui/HistoryScreens.kt`
   - Added import for OrderSummaryCard
   - Replaced totals section with OrderSummaryCard
   - Added calculation logic
   - Separated cash payment details

---

## Testing Checklist

### Payment Screen:
- [x] OrderSummaryCard displays correctly
- [x] All fields show correct values
- [x] Conditional fields (discount, tax) work
- [x] Styling consistent

### History Detail Screen:
- [x] OrderSummaryCard displays correctly
- [x] Calculations accurate (gross, item discount, net)
- [x] Tax rate calculated correctly
- [x] Cash payment details in separate card
- [x] Same visual design as Payment screen

### Receipt Screen:
- [x] Simple display maintained
- [x] No confusion with different format
- [x] Essential info only

---

## Build Status

✅ **BUILD SUCCESSFUL**
```
42 actionable tasks: 10 executed, 32 up-to-date
```

---

## Summary

**Goal:** Menyeragamkan tampilan ringkasan pesanan

**Implementation:**
- ✅ Payment Screen - Already using OrderSummaryCard
- ✅ History Detail Screen - Now using OrderSummaryCard
- ℹ️ Receipt Screen - Kept simple (intentional)

**Result:**
- ✅ Consistent UI/UX
- ✅ Reusable component
- ✅ Maintainable code
- ✅ Professional appearance
- ✅ Clear information hierarchy

---

**Status: ✅ COMPLETE**

Semua screen yang menampilkan detail ringkasan pesanan sekarang menggunakan `OrderSummaryCard` component yang sama untuk konsistensi visual dan kode yang lebih maintainable.

