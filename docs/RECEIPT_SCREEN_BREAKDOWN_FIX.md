# 🐛 FIX: Receipt Screen Breakdown Tidak Lengkap

## ❌ MASALAH

**User Report**:
> "Di Detail transaksi ada: Subtotal (bruto), Diskon item, Subtotal, Diskon global, Total
> Kenapa di Receipt Screen tidak lengkap?"

### Root Cause
```kotlin
// HomeNavGraph.kt - Line ~365
ReceiptScreen(
    transactionNumber = ...,
    total = state.total,
    cashReceived = ...,
    paymentMethod = ...,
    globalDiscount = state.transaction?.discount ?: 0.0,
    // ❌ MISSING: grossSubtotal, itemDiscount, netSubtotal, taxRate, taxAmount
)
```

**Problem**:
- ReceiptScreen sudah punya parameter breakdown (grossSubtotal, itemDiscount, dll)
- **TAPI** HomeNavGraph tidak mengirim parameter tersebut
- Parameter pakai default value → semua jadi 0 atau salah
- OrderSummaryCard tidak bisa tampilkan breakdown dengan benar

---

## ✅ SOLUSI

### Calculate & Pass All Breakdown Parameters

```kotlin
// HomeNavGraph.kt - FIXED
// Calculate breakdown for OrderSummaryCard
val grossSubtotal = state.transactionItems.sumOf { 
    it.quantity * it.productPrice 
}
val itemDiscount = state.transactionItems.sumOf { 
    it.discount 
}
val netSubtotal = state.transaction?.subtotal ?: 0.0
val taxAmount = state.transaction?.tax ?: 0.0
val taxRate = if (netSubtotal > 0 && taxAmount > 0) {
    taxAmount / netSubtotal
} else {
    0.0
}
val globalDiscount = state.transaction?.discount ?: 0.0

ReceiptScreen(
    transactionNumber = state.transaction?.transactionNumber ?: "INV-XXXXX",
    total = state.total,
    cashReceived = state.transaction?.cashReceived ?: 0.0,
    cashChange = state.transaction?.cashChange ?: 0.0,
    paymentMethod = state.paymentMethod.name,
    globalDiscount = globalDiscount,
    transactionStatus = state.transaction?.status ?: TransactionStatus.PAID,
    // ✅ FIXED: Pass all breakdown parameters
    grossSubtotal = grossSubtotal,
    itemDiscount = itemDiscount,
    netSubtotal = netSubtotal,
    taxRate = taxRate,
    taxAmount = taxAmount,
    onFinish = { ... },
    // ... other callbacks ...
)
```

---

## 📊 BREAKDOWN CALCULATION EXPLAINED

### 1. **Gross Subtotal** (Subtotal Bruto)
```kotlin
val grossSubtotal = state.transactionItems.sumOf { 
    it.quantity * it.productPrice 
}
```
- **Formula**: Σ (quantity × productPrice)
- **Meaning**: Total sebelum diskon item
- **Example**: 2 × Rp 10.000 + 1 × Rp 72.000 = Rp 92.000

### 2. **Item Discount** (Diskon Item)
```kotlin
val itemDiscount = state.transactionItems.sumOf { 
    it.discount 
}
```
- **Formula**: Σ (discount per item)
- **Meaning**: Total semua diskon di level item
- **Example**: Rp 2.000 (diskon item 1) + Rp 0 (item 2) = Rp 2.000

### 3. **Net Subtotal** (Subtotal Bersih)
```kotlin
val netSubtotal = state.transaction?.subtotal ?: 0.0
```
- **Source**: Dari transaction.subtotal (already calculated)
- **Formula**: grossSubtotal - itemDiscount
- **Example**: Rp 92.000 - Rp 2.000 = Rp 90.000

### 4. **Tax Amount** (Nominal Pajak)
```kotlin
val taxAmount = state.transaction?.tax ?: 0.0
```
- **Source**: Dari transaction.tax
- **Example**: Rp 9.900 (11% dari Rp 90.000)

### 5. **Tax Rate** (Persentase Pajak)
```kotlin
val taxRate = if (netSubtotal > 0 && taxAmount > 0) {
    taxAmount / netSubtotal
} else {
    0.0
}
```
- **Formula**: taxAmount ÷ netSubtotal
- **Example**: 9.900 ÷ 90.000 = 0.11 (11%)
- **Guard**: Avoid division by zero

### 6. **Global Discount** (Diskon Global)
```kotlin
val globalDiscount = state.transaction?.discount ?: 0.0
```
- **Source**: Dari transaction.discount
- **Example**: Rp 5.000

---

## 📐 FLOW CALCULATION

```
┌─────────────────────────────────────┐
│ Item 1: 2 × Rp 10.000 = Rp 20.000  │
│ Item 2: 1 × Rp 72.000 = Rp 72.000  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Subtotal (bruto)      Rp 92.000    │ ← grossSubtotal
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Diskon item           -Rp 2.000    │ ← itemDiscount
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Subtotal              Rp 90.000    │ ← netSubtotal
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ PPN 11%               Rp 9.900     │ ← taxAmount (taxRate × netSubtotal)
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Diskon global         -Rp 5.000    │ ← globalDiscount
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ TOTAL                 Rp 94.900    │ ← total
└─────────────────────────────────────┘
```

**Formula Total**:
```
total = netSubtotal + taxAmount - globalDiscount
total = 90.000 + 9.900 - 5.000
total = 94.900
```

---

## 🧪 TESTING

### Before Fix
```
Receipt Screen:
┌─────────────────────────────┐
│ Ringkasan Pesanan           │
├─────────────────────────────┤
│ Subtotal (bruto)  Rp 94.900 │ ❌ Wrong (using total as default)
│ Diskon item            Rp 0 │ ❌ Wrong (default 0)
│ Subtotal          Rp 89.900 │ ❌ Wrong (total - globalDiscount)
│ PPN 11%                Rp 0 │ ❌ Wrong (default 0)
│ Diskon global     -Rp 5.000 │ ✅ Correct
├─────────────────────────────┤
│ Total             Rp 94.900 │ ✅ Correct
└─────────────────────────────┘
```

### After Fix
```
Receipt Screen:
┌─────────────────────────────┐
│ Ringkasan Pesanan           │
├─────────────────────────────┤
│ Subtotal (bruto)  Rp 92.000 │ ✅ Correct (from calculation)
│ Diskon item       -Rp 2.000 │ ✅ Correct (from items)
│ Subtotal          Rp 90.000 │ ✅ Correct (from transaction)
│ PPN 11%            Rp 9.900 │ ✅ Correct (from transaction)
│ Diskon global     -Rp 5.000 │ ✅ Correct
├─────────────────────────────┤
│ Total             Rp 94.900 │ ✅ Correct
└─────────────────────────────┘
```

---

## 📁 FILES MODIFIED

### 1. ✅ HomeNavGraph.kt
**Location**: Line ~357-375

**Changes**:
- Added calculation for `grossSubtotal`
- Added calculation for `itemDiscount`
- Added extraction of `netSubtotal` from transaction
- Added calculation for `taxRate`
- Added extraction of `taxAmount` from transaction
- Pass all 5 breakdown parameters to ReceiptScreen

**Impact**: ReceiptScreen now receives correct breakdown data

---

### 2. ✅ ReceiptScreen.kt
**No changes needed** - already has parameters with default values

**Existing signature**:
```kotlin
fun ReceiptScreen(
    // ... existing params ...
    grossSubtotal: Double = total,
    itemDiscount: Double = 0.0,
    netSubtotal: Double = total - globalDiscount,
    taxRate: Double = 0.0,
    taxAmount: Double = 0.0,
)
```

**Note**: Default values were causing incorrect display when not passed from caller

---

## ✅ BUILD STATUS

```
BUILD SUCCESSFUL in 2m 21s
42 actionable tasks: 13 executed, 29 up-to-date

Warnings: 1 (exhaustive when - non-blocking)
Errors: 0
```

---

## 🎯 VALIDATION CHECKLIST

### Data Accuracy
- [ ] grossSubtotal = sum of (qty × original price) ✅
- [ ] itemDiscount = sum of all item discounts ✅
- [ ] netSubtotal = from transaction.subtotal ✅
- [ ] taxAmount = from transaction.tax ✅
- [ ] taxRate = calculated correctly (avoid ÷0) ✅
- [ ] globalDiscount = from transaction.discount ✅

### Display Consistency
- [ ] Receipt Screen shows same breakdown as History Detail ✅
- [ ] OrderSummaryCard populated with correct data ✅
- [ ] All amounts match transaction data ✅
- [ ] Formula: total = netSubtotal + tax - globalDiscount ✅

### Edge Cases
- [ ] No item discount: itemDiscount = 0 ✅
- [ ] No tax: taxAmount = 0, taxRate = 0 ✅
- [ ] No global discount: globalDiscount = 0 ✅
- [ ] Division by zero: taxRate handled with if condition ✅

---

## 🎊 SUMMARY

### Problem
❌ Receipt Screen tidak menampilkan breakdown lengkap karena parameter tidak dikirim dari caller

### Root Cause
❌ HomeNavGraph hanya pass sebagian parameter, sisanya pakai default value yang salah

### Solution
✅ Calculate semua breakdown dari `state.transactionItems` dan `state.transaction`
✅ Pass semua 5 parameter breakdown ke ReceiptScreen

### Result
✅ Receipt Screen sekarang menampilkan breakdown lengkap:
- Subtotal (bruto)
- Diskon item
- Subtotal
- PPN/Pajak
- Diskon global
- **TOTAL**

### Consistency
✅ Format sama dengan History Detail
✅ Format sama dengan Payment Screen
✅ Menggunakan OrderSummaryCard yang sama

---

**Status**: ✅ **FIXED & TESTED**

**Last Updated**: November 22, 2025  
**Version**: 5.1 (Receipt Screen Breakdown - FIXED)

