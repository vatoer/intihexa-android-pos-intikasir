# Discount Input Dialog Fix - Opsi B Per Unit

## Tanggal: 18 November 2025

## Masalah yang Diperbaiki

### Input Diskon Membingungkan (Opsi A)

**Scenario:**
```
Produk: Buku Tulis @ Rp 5.000
User input diskon: 300
```

**Sebelum (Opsi A - Total Discount):**
```
Beli 1 pcs:
- Input: 300
- Total diskon: Rp 300
- Diskon per pcs: Rp 300/1 = Rp 300
- Harga jadi: 5.000 - 300 = Rp 4.700 ✅

Beli 2 pcs:
- Input: 300 (SAME!)
- Total diskon: Rp 300
- Diskon per pcs: Rp 300/2 = Rp 150
- Harga per pcs: 5.000 - 150 = Rp 4.850
- Total: 2 × 4.850 = Rp 9.700 ❌
```

❌ **Masalah:**
- Input 300 tapi hasil berbeda tergantung quantity
- Beli 1 = Rp 4.700, beli 2 = Rp 9.700 (Rp 4.850/pcs)
- Harga per pcs tidak konsisten
- User bingung!

**Sesudah (Opsi B - Per Unit Discount):**
```
Beli 1 pcs:
- Input: 300/pcs
- Total diskon: 300 × 1 = Rp 300
- Harga jadi: 5.000 - 300 = Rp 4.700 ✅

Beli 2 pcs:
- Input: 300/pcs (SAME!)
- Total diskon: 300 × 2 = Rp 600
- Harga per pcs: 5.000 - 300 = Rp 4.700
- Total: 2 × 4.700 = Rp 9.400 ✅
```

✅ **Solusi:**
- Input 300/pcs → selalu konsisten
- Beli 1 = Rp 4.700, beli 2 = Rp 9.400 (Rp 4.700/pcs)
- Harga per pcs KONSISTEN
- Clear & predictable!

---

## Implementation

### Dialog Baru - Features:

1. **Input Diskon per Pcs**
   - Label: "Diskon per Pcs"
   - Suffix: "/pcs"
   - Clear indication unit pricing

2. **Product Info Card**
   - Nama produk
   - Harga asli per pcs
   - Quantity saat ini

3. **Real-time Preview**
   - Harga asli
   - Diskon per pcs
   - Harga setelah diskon per pcs
   - Total diskon (auto-calculated)
   - Subtotal final

4. **Validation**
   - Max diskon = harga produk
   - Error indicator jika melebihi
   - Disable save button jika invalid

5. **Auto-conversion**
   - User input: diskon per pcs
   - System save: total diskon (per pcs × qty)
   - Database tetap konsisten

---

## Visual Design

### Dialog Layout:
```
╔════════════════════════════════════════╗
║         Diskon per Pcs                 ║
╠════════════════════════════════════════╣
║ ┌────────────────────────────────────┐ ║
║ │ Buku Tulis                         │ ║
║ │ Harga: Rp 5.000/pcs                │ ║
║ │ Quantity: 2 pcs                    │ ║
║ └────────────────────────────────────┘ ║
║                                        ║
║ ┌─ Diskon per Pcs ──────────────────┐ ║
║ │ Rp [300]/pcs                      │ ║
║ │ Maks: Rp 5.000/pcs                │ ║
║ └───────────────────────────────────┘ ║
║                                        ║
║ ┌─ Preview: ────────────────────────┐ ║
║ │ Harga asli:      Rp 5.000/pcs     │ ║
║ │ Diskon per pcs:  -Rp 300          │ ║
║ │ Harga jadi:      Rp 4.700/pcs     │ ║
║ │ ─────────────────────────────────  │ ║
║ │ Total diskon:    -Rp 600          │ ║
║ │ Subtotal:        Rp 9.400         │ ║
║ └───────────────────────────────────┘ ║
║                                        ║
║           [Batal]  [Simpan]            ║
╚════════════════════════════════════════╝
```

---

## Code Implementation

### Discount Dialog Logic:

```kotlin
// Calculate current discount per unit (for editing)
val currentDiscountPerUnit = if (transactionItem.discount > 0) {
    (transactionItem.discount / transactionItem.quantity).toInt()
} else 0

var discountPerUnitText by remember { mutableStateOf(currentDiscountPerUnit.toString()) }

// Real-time calculation
val discountPerUnit = discountPerUnitText.toIntOrNull() ?: 0
val totalDiscount = discountPerUnit * transactionItem.quantity
val priceAfterDiscount = product.price - discountPerUnit
val subtotal = priceAfterDiscount * transactionItem.quantity

// Validation
val maxDiscountPerUnit = product.price.toInt()
isError = discountPerUnit > maxDiscountPerUnit

// On save - convert to total discount
onSetDiscount(discountPerUnit.toDouble() * transactionItem.quantity)
```

### Key Points:

1. **Input:** Diskon per pcs (user-friendly)
2. **Storage:** Total diskon (database format)
3. **Display:** Preview both per pcs dan total
4. **Validation:** Max per pcs = harga produk

---

## User Flow

### Scenario 1: Input Diskon Baru

```
1. User klik icon "⋮" (More) di item
2. Dialog muncul dengan info produk
3. User lihat:
   - Buku Tulis
   - Harga: Rp 5.000/pcs
   - Quantity: 2 pcs
   
4. User input diskon: 300
   Label jelas: "Diskon per Pcs" + suffix "/pcs"
   
5. Preview update real-time:
   - Harga asli: Rp 5.000/pcs
   - Diskon per pcs: -Rp 300
   - Harga jadi: Rp 4.700/pcs
   - Total diskon: -Rp 600
   - Subtotal: Rp 9.400
   
6. User klik "Simpan"
7. System save total: 600 (300 × 2)
8. Display update: @Rp 5.000 → Rp 4.700/pcs
```

### Scenario 2: Edit Quantity setelah Diskon

```
Current state:
- Buku Tulis @ Rp 5.000
- Diskon: Rp 300/pcs
- Qty: 2
- Total diskon: Rp 600

User ubah qty 2 → 3:
1. System auto-calculate:
   - Current total discount: Rp 600
   - Discount per unit: 600 / 2 = Rp 300
   - New total discount: 300 × 3 = Rp 900
   
2. Display update:
   - @Rp 5.000/pcs → Rp 4.700/pcs
   - Diskon: Rp 300/pcs (Total: Rp 900)
   - 3 × Rp 4.700 = Rp 14.100
   
✅ Harga per pcs tetap Rp 4.700!
```

---

## Comparison: Before vs After

### Input Diskon 300:

**Before (Opsi A - Total):**
```
Dialog:
┌─ Diskon Item ─────────┐
│ Jumlah Diskon:        │
│ Rp [300]              │ ← Total? Per pcs? Unclear!
└───────────────────────┘

Result:
Qty 1: Total Rp 300 → Rp 4.700/pcs
Qty 2: Total Rp 300 → Rp 4.850/pcs ❌ BERBEDA!
```

**After (Opsi B - Per Pcs):**
```
Dialog:
┌─ Diskon per Pcs ──────────────┐
│ Rp [300]/pcs                  │ ← JELAS per pcs!
│ Maks: Rp 5.000/pcs            │
│                               │
│ Preview:                      │
│ Harga jadi: Rp 4.700/pcs      │
│ Total diskon: -Rp 600         │
│ Subtotal: Rp 9.400            │
└───────────────────────────────┘

Result:
Qty 1: Total Rp 300 → Rp 4.700/pcs ✅
Qty 2: Total Rp 600 → Rp 4.700/pcs ✅ KONSISTEN!
```

---

## Benefits

### 1. **Clarity (Kejelasan)**
- ✅ Label jelas: "Diskon per Pcs"
- ✅ Suffix "/pcs" pada input
- ✅ Preview real-time
- ✅ No ambiguity

### 2. **Consistency (Konsistensi)**
- ✅ Harga per pcs selalu sama
- ✅ Predictable pricing
- ✅ Sesuai Opsi B di semua tempat

### 3. **User Experience**
- ✅ Easy to understand
- ✅ Visual preview
- ✅ Instant feedback
- ✅ Error prevention (validation)

### 4. **Professional**
- ✅ Best practice retail
- ✅ Clear pricing policy
- ✅ Transparent to customer

---

## Testing Scenarios

### Test 1: Single Item
```
Input: Buku @ Rp 5.000, disc Rp 300/pcs, qty 1
Expected:
- Total discount: Rp 300
- Price per pcs: Rp 4.700
- Subtotal: Rp 4.700
✅ PASS
```

### Test 2: Multiple Items
```
Input: Buku @ Rp 5.000, disc Rp 300/pcs, qty 2
Expected:
- Total discount: Rp 600
- Price per pcs: Rp 4.700
- Subtotal: Rp 9.400
✅ PASS
```

### Test 3: Change Quantity After Discount
```
Step 1: Set discount Rp 300/pcs, qty 2
Step 2: Change qty to 3
Expected:
- Discount per pcs stays: Rp 300
- Total discount updates: Rp 900
- Price per pcs stays: Rp 4.700
- Subtotal: Rp 14.100
✅ PASS
```

### Test 4: Max Discount Validation
```
Input: Buku @ Rp 5.000, try disc Rp 6.000/pcs
Expected:
- Show error indicator
- Disable save button
- Show max: Rp 5.000/pcs
✅ PASS
```

### Test 5: Zero Discount
```
Input: Clear discount (0)
Expected:
- No discount shown
- Price = original price
- Preview shows correct total
✅ PASS
```

---

## Files Modified

1. ✅ `PosProductItemReactive.kt`
   - Updated discount dialog
   - Changed from total input to per-unit input
   - Added product info card
   - Added real-time preview
   - Added validation
   - Added max discount indicator

---

## Build Status

✅ **BUILD SUCCESSFUL**
```
42 actionable tasks: 13 executed, 29 up-to-date
```

---

## Summary

### Problem:
❌ Discount input was **total discount** (Opsi A)
- Inconsistent price per unit when qty changes
- Confusing for users
- Not aligned with Opsi B

### Solution:
✅ Discount input is now **per unit** (Opsi B)
- Consistent price per unit
- Clear labeling "/pcs"
- Real-time preview
- Validation
- User-friendly

### Example:
```
Before: Input 300 → unclear (total? per pcs?)
Qty 1: Rp 4.700/pcs
Qty 2: Rp 4.850/pcs ❌

After: Input 300/pcs → clear (per pcs!)
Qty 1: Rp 4.700/pcs
Qty 2: Rp 4.700/pcs ✅
```

### Impact:
- ✅ Consistent pricing
- ✅ Clear user input
- ✅ Professional UX
- ✅ Aligned with Opsi B everywhere

---

**Status: ✅ COMPLETE**

Dialog input diskon sekarang konsisten dengan Opsi B - input per pcs, harga per pcs selalu sama, tidak membingungkan! 🎉

