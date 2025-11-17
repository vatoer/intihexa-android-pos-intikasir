# Migration from Opsi A to Opsi B - Complete Implementation

## Tanggal: 18 November 2025

## Masalah yang Diperbaiki

### Inkonsistensi Tampilan Diskon - Opsi A vs Opsi B

**Sebelum:**
- Screen Kasir (POS): Menggunakan **Opsi A** (diskon flat)
- Screen History Detail: Menggunakan **Opsi A** (diskon flat)  
- Receipt/Struk Print: Menggunakan **Opsi B** (diskon per unit)

❌ **Masalah:**
- Tidak konsisten antara UI dan struk cetak
- User bingung melihat perhitungan berbeda
- Tidak sesuai dengan best practice retail

**Sesudah:**
- ✅ **Semua screen menggunakan Opsi B (diskon per unit)**
- ✅ Konsisten di seluruh aplikasi
- ✅ Sesuai dengan best practice retail

---

## Opsi B: Diskon Per Unit (Best Practice)

### Formula:
```
Total Diskon = Diskon per pcs × Quantity
Discounted Price per Unit = Original Price - (Total Discount / Quantity)
Subtotal = Quantity × Discounted Price per Unit
```

### Contoh:
```
Sabun @ Rp 1.000/pcs
Diskon: Rp 200/pcs
Quantity: 2 pcs

Calculation:
- Total Discount = 200 × 2 = Rp 400
- Discounted Price = 1000 - (400/2) = Rp 800/pcs
- Subtotal = 2 × 800 = Rp 1.600
```

---

## Files Modified

### 1. ✅ PosProductItemReactive.kt
**Location:** `feature/pos/ui/components/PosProductItemReactive.kt`

**Changes:**
- Display original price with @ prefix (strikethrough indicator)
- Calculate and display discounted price per unit
- Show discount per unit AND total discount
- Show calculation: `Qty × Discounted Price = Subtotal`

**Before (Opsi A):**
```
Sabun
Rp 1.000
Diskon: Rp 400
x 2 = Rp 1.600
```
❌ Tidak jelas harga per pcs setelah diskon

**After (Opsi B):**
```
Sabun
@Rp 1.000/pcs (original)
Rp 800/pcs (after discount)
Diskon: Rp 200/pcs (Total: Rp 400)
2 x Rp 800 = Rp 1.600
```
✅ Jelas per unit price!

---

### 2. ✅ HistoryScreens.kt - ItemDetailRow
**Location:** `feature/history/ui/HistoryScreens.kt`

**Changes:**
- Added `productPrice` parameter to ItemDetailRow
- Display original price with @ prefix
- Calculate discounted price per unit
- Show discount per unit AND total discount

**Before (Opsi A):**
```kotlin
Text("$quantity x $unitStr = $subStr")
if (discount > 0) {
    Text("Diskon item: -$discountStr")
}
```

**After (Opsi B):**
```kotlin
if (discount > 0) {
    // Original price
    Text("@$origPriceStr/pcs")
    
    // Discounted price per unit
    val discountPerUnit = discount / quantity
    val discountedPricePerUnit = productPrice - discountPerUnit
    Text("$quantity x $discountedStr = $subStr")
    
    // Discount info
    Text("Diskon: $discountPerUnitStr/pcs (Total: -$discountStr)")
}
```

---

## Visual Examples

### 1. Screen Kasir (POS) - Product Item Card

**Opsi A (Before):**
```
┌─────────────────────────────┐
│ Sabun Mandi                 │
│ Rp 1.000                    │
│ Diskon: Rp 400              │ ← Total diskon aja
│ x 2 = Rp 1.600              │ ← Tidak jelas
└─────────────────────────────┘
```

**Opsi B (After):**
```
┌─────────────────────────────┐
│ Sabun Mandi                 │
│ @Rp 1.000/pcs               │ ← Harga asli
│ Rp 800/pcs                  │ ← Harga diskon per pcs
│ Diskon: Rp 200/pcs          │ ← Diskon per pcs
│   (Total: Rp 400)           │ ← Total diskon
│ 2 x Rp 800 = Rp 1.600       │ ← Clear calculation
└─────────────────────────────┘
```

---

### 2. History Detail - Item Row

**Opsi A (Before):**
```
┌─────────────────────────────┐
│ Sabun Mandi                 │
│ 2 x Rp 1.000 = Rp 1.600     │ ← Wrong! (harga asli)
│ Diskon item: -Rp 400        │
└─────────────────────────────┘
```

**Opsi B (After):**
```
┌─────────────────────────────┐
│ Sabun Mandi                 │
│ @Rp 1.000/pcs               │ ← Harga asli
│ 2 x Rp 800 = Rp 1.600       │ ← Harga setelah diskon
│ Diskon: Rp 200/pcs          │
│   (Total: -Rp 400)          │
└─────────────────────────────┘
```

---

## Implementation Details

### PosProductItemReactive.kt Logic:

```kotlin
if (transactionItem != null && transactionItem.discount > 0) {
    // Show original price with strikethrough indicator
    Text("@${nf.format(product.price)}/pcs")
    
    // Calculate and show discounted price per unit
    val discountPerUnit = transactionItem.discount / transactionItem.quantity
    val discountedPricePerUnit = product.price - discountPerUnit
    Text("${nf.format(discountedPricePerUnit)}/pcs", color = primary)
    
    // Show discount per unit and total
    Text("Diskon: ${nf.format(discountPerUnit)}/pcs (Total: ${nf.format(transactionItem.discount)})")
    
    // Show calculation
    Text("${transactionItem.quantity} x ${nf.format(priceAfterDiscount)} = ${nf.format(transactionItem.subtotal)}")
}
```

### ItemDetailRow Logic:

```kotlin
if (discount > 0) {
    // Original price
    Text("@${currency.format(productPrice)}/pcs")
    
    // Calculate discounted price per unit
    val discountPerUnit = discount / quantity
    val discountedPricePerUnit = productPrice - discountPerUnit
    
    // Show calculation
    Text("$quantity x ${currency.format(discountedPricePerUnit)} = ${currency.format(subtotal)}")
    
    // Show discount
    Text("Diskon: ${currency.format(discountPerUnit)}/pcs (Total: -${currency.format(discount)})")
}
```

---

## Consistency Across All Screens

### ✅ Kasir/POS Screen:
```
Item: Sabun @ Rp 1.000, disc Rp 200/pcs, qty 2

Display:
@Rp 1.000/pcs
Rp 800/pcs
Diskon: Rp 200/pcs (Total: Rp 400)
2 x Rp 800 = Rp 1.600
```

### ✅ Cart Screen:
```
Same as POS (uses same PosProductItemReactive component)
```

### ✅ Payment Screen:
```
OrderSummaryCard shows:
Gross Subtotal: Rp 2.000 (2 × 1.000)
Diskon item: -Rp 400
Net Subtotal: Rp 1.600
```

### ✅ History Detail Screen:
```
Item Card:
Sabun
@Rp 1.000/pcs
2 x Rp 800 = Rp 1.600
Diskon: Rp 200/pcs (Total: -Rp 400)
```

### ✅ Receipt/Struk Print:
```
Sabun
  @Rp 1.000/pcs
2 x Rp 800         Rp 1.600
  Diskon: -Rp 400
```

**ALL CONSISTENT!** ✅

---

## Benefits

### 1. **Konsistensi Total**
- ✅ UI dan struk cetak sama
- ✅ Tidak ada kebingungan
- ✅ Professional

### 2. **Clarity (Kejelasan)**
- ✅ Harga asli terlihat jelas
- ✅ Diskon per unit jelas
- ✅ Perhitungan mudah diverifikasi

### 3. **Best Practice**
- ✅ Sesuai standar retail modern
- ✅ Sama dengan Shopify, Square, dll
- ✅ User-friendly

### 4. **Transparency**
- ✅ Customer tahu harga asli
- ✅ Customer tahu diskon yang didapat
- ✅ Builds trust

---

## Testing Checklist

### POS Screen:
- [x] Item tanpa diskon: Show price/pcs
- [x] Item dengan diskon: Show original price @ format
- [x] Item dengan diskon: Show discounted price/pcs
- [x] Item dengan diskon: Show discount per unit
- [x] Item dengan diskon: Show total discount
- [x] Item dengan diskon: Show calculation qty × price = subtotal

### Cart Screen:
- [x] Same as POS (reuses component)
- [x] OrderSummaryCard shows correct values

### History Detail:
- [x] ItemDetailRow shows original price
- [x] ItemDetailRow shows discounted price per unit
- [x] ItemDetailRow shows discount per unit
- [x] ItemDetailRow shows total discount
- [x] Calculation correct

### Receipt Print:
- [x] Already implemented Opsi B
- [x] Consistent with UI screens

---

## Migration Summary

### What Changed:
1. **PosProductItemReactive.kt** - Display logic updated to Opsi B
2. **HistoryScreens.kt** - ItemDetailRow updated to Opsi B

### What Stayed Same:
1. **Database structure** - Already supports Opsi B
2. **Calculation logic** - Already correct in ViewModel
3. **OrderSummaryCard** - Already correct
4. **Receipt printing** - Already using Opsi B

### Impact:
- ✅ **UI now matches database logic**
- ✅ **UI now matches receipt print**
- ✅ **Consistent user experience**
- ✅ **Best practice implementation**

---

## Build Status

✅ **BUILD SUCCESSFUL**
```
42 actionable tasks: 13 executed, 29 up-to-date
```

---

## Example Scenarios

### Scenario 1: Single Item with Discount
```
Product: Sabun @ Rp 1.000
Discount: Rp 200/pcs
Quantity: 2

POS Display:
- @Rp 1.000/pcs (original)
- Rp 800/pcs (discounted)
- Diskon: Rp 200/pcs (Total: Rp 400)
- 2 x Rp 800 = Rp 1.600

Receipt Print:
Sabun
  @Rp 1.000/pcs
2 x Rp 800         Rp 1.600
  Diskon: -Rp 400

✅ CONSISTENT!
```

### Scenario 2: Multiple Items
```
Item 1: Sabun @ Rp 1.000, disc Rp 200/pcs, qty 2
Item 2: Shampoo @ Rp 5.000, disc Rp 1.000/pcs, qty 1
Item 3: Pasta @ Rp 2.000, no disc, qty 3

POS & Cart Display:
- Sabun: @Rp 1.000 → Rp 800/pcs, 2 × Rp 800 = Rp 1.600
- Shampoo: @Rp 5.000 → Rp 4.000/pcs, 1 × Rp 4.000 = Rp 4.000
- Pasta: Rp 2.000/pcs, 3 × Rp 2.000 = Rp 6.000

Summary (OrderSummaryCard):
- Gross Subtotal: Rp 11.000 (2×1k + 1×5k + 3×2k)
- Diskon item: -Rp 1.400 (400 + 1000)
- Net Subtotal: Rp 9.600

Receipt Print: (same format)

✅ ALL CONSISTENT!
```

---

## Conclusion

### ✅ Achievement:
- Migrated **ALL screens** from Opsi A to Opsi B
- **100% consistency** across UI and print
- **Best practice** implementation
- **User-friendly** display

### ✅ Files Updated:
1. PosProductItemReactive.kt
2. HistoryScreens.kt (ItemDetailRow)

### ✅ Impact:
- Clear pricing information
- Transparent discount display
- Professional appearance
- Better user experience

---

**Status: ✅ COMPLETE - All screens now use Opsi B (Discount Per Unit)**

