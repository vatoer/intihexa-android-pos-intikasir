# Discount Logic & Receipt Display - Final Documentation

## Tanggal: 18 November 2025

---

## 📌 Logika Diskon yang Diterapkan

### ✅ **OPSI B: Diskon Per Unit (Total = Diskon × Quantity)**

**Contoh:**
```
Sabun @ Rp 1.000/pcs
Diskon: Rp 200/pcs

Beli 1 pcs:
- Harga: 1 × Rp 1.000 = Rp 1.000
- Diskon: 1 × Rp 200 = Rp 200
- Subtotal: Rp 800

Beli 2 pcs:
- Harga: 2 × Rp 1.000 = Rp 2.000
- Diskon: 2 × Rp 200 = Rp 400
- Subtotal: Rp 1.600

Beli 5 pcs:
- Harga: 5 × Rp 1.000 = Rp 5.000
- Diskon: 5 × Rp 200 = Rp 1.000
- Subtotal: Rp 4.000
```

---

## 🎯 Kenapa Opsi B (Best Practice)?

### 1. **Standar Industri Retail**
- Shopify, Square, WooCommerce, dll → semua pakai sistem ini
- Konsisten dengan ekspektasi customer
- Professional standard

### 2. **Konsistensi Harga**
- 1 pcs @ Rp 800 (setelah diskon)
- 2 pcs @ Rp 1.600 (2 × Rp 800)
- 10 pcs @ Rp 8.000 (10 × Rp 800)
- **Harga per unit selalu sama!**

### 3. **Mudah Dipahami Customer**
```
"Sabun diskon jadi Rp 800/pcs"
"Beli 3 = 3 × Rp 800 = Rp 2.400"
```
✅ Simple & clear

### 4. **Marketing Friendly**
```
"HEMAT Rp 200 per pcs!"
"Beli 10, hemat Rp 2.000!"
```
✅ Menarik & jelas

---

## 💾 Data Structure

```kotlin
data class TransactionItemEntity(
    val productName: String,       // "Sabun"
    val productPrice: Double,      // 1000 (harga asli per pcs)
    val quantity: Int,             // 2
    val unitPrice: Double,         // 1000 (sama dengan productPrice)
    val discount: Double,          // 400 (TOTAL diskon = 200 × 2)
    val subtotal: Double           // 1600 = (1000 × 2) - 400
)
```

### Calculation Formula:
```kotlin
subtotal = (productPrice × quantity) - discount

// Untuk mendapat harga per pcs setelah diskon:
discountPerUnit = discount / quantity
discountedPricePerUnit = productPrice - discountPerUnit
```

---

## 🖨️ Tampilan di Struk (Updated)

### ESC/POS Thermal Printer:
```
================================
         TOKO SAYA
    Jl. Contoh No. 123
--------------------------------
Sabun Mandi
  @Rp 1.000/pcs (harga asli)
2 x Rp 800         Rp 1.600
  Diskon: -Rp 400

Shampoo
  @Rp 5.000/pcs
1 x Rp 4.000       Rp 4.000
  Diskon: -Rp 1.000

Pasta Gigi (no discount)
3 x Rp 2.000       Rp 6.000
--------------------------------
Subtotal           Rp 11.600
Diskon item        -Rp 1.400
PPN (11%)           Rp 1.122
--------------------------------
TOTAL              Rp 11.322
================================
```

### Penjelasan Format:
1. **Line 1:** Nama produk
2. **Line 2:** `@Rp X/pcs` = Harga asli per pcs (dengan @ untuk indicate strikethrough)
3. **Line 3:** `Qty x Harga setelah diskon per pcs = Subtotal`
4. **Line 4:** `Diskon: -Rp X` (total diskon untuk semua quantity)

### Example Breakdown:
```
Sabun Mandi
  @Rp 1.000/pcs         ← Harga asli
2 x Rp 800 = Rp 1.600   ← 2 pcs × Rp 800/pcs (1000-200)
  Diskon: -Rp 400       ← Total diskon (2 × 200)
```

---

## 📱 Implementation di Code

### ESCPosPrinter.kt (Bluetooth Thermal):
```kotlin
items.forEach { itx ->
    text(itx.productName.take(cpl))
    
    if (itx.discount > 0) {
        val originalPrice = itx.productPrice
        val discountPerUnit = itx.discount / itx.quantity
        val discountedPricePerUnit = originalPrice - discountPerUnit
        
        // Harga asli dengan /pcs
        text("  @${nf.format(originalPrice)}/pcs")
        
        // Qty x harga setelah diskon per pcs = subtotal
        text("${itx.quantity} x ${nf.format(discountedPricePerUnit)} = ${nf.format(itx.subtotal)}")
        
        // Total diskon
        text("  Diskon: -${nf.format(itx.discount)}")
    } else {
        // Simple format tanpa diskon
        text("${itx.quantity} x ${nf.format(itx.unitPrice)} = ${nf.format(itx.subtotal)}")
    }
}
```

### ReceiptPrinter.kt (PDF Thermal):
```kotlin
items.forEach { item ->
    canvas.drawText(name, left, y, textPaint)
    y += 16f
    
    if (item.discount > 0) {
        val originalPrice = item.productPrice
        val discountPerUnit = item.discount / item.quantity
        val discountedPricePerUnit = originalPrice - discountPerUnit
        
        // Harga asli dengan strikethrough
        val origPriceStr = "@${nf.format(originalPrice)}/pcs"
        canvas.drawText(origPriceStr, left + 8f, y, smallPaint)
        canvas.drawLine(...) // strikethrough
        y += 14f
        
        // Qty x harga setelah diskon per pcs
        val qtyPart = "${item.quantity} x ${nf.format(discountedPricePerUnit)}"
        canvas.drawText(qtyPart, left + 8f, y, smallPaint)
        canvas.drawText(subtotal, right, y, textPaint)
        y += 18f
        
        // Total diskon
        canvas.drawText("Diskon: -${nf.format(item.discount)}", left + 8f, y, smallPaint)
        y += 16f
    }
}
```

---

## 🧮 Contoh Perhitungan

### Skenario 1: Single Item dengan Diskon
```
Produk: Sabun @ Rp 1.000
Quantity: 2 pcs
Diskon per pcs: Rp 200

Calculation:
- productPrice = 1000
- quantity = 2
- discount = 400 (200 × 2)
- discountPerUnit = 400 / 2 = 200
- discountedPricePerUnit = 1000 - 200 = 800
- subtotal = (1000 × 2) - 400 = 1600

Struk:
Sabun
  @Rp 1.000/pcs
2 x Rp 800         Rp 1.600
  Diskon: -Rp 400
```

### Skenario 2: Multiple Items dengan Berbagai Diskon
```
Item 1: Sabun @ Rp 1.000, qty 2, disc Rp 400
Item 2: Shampoo @ Rp 5.000, qty 1, disc Rp 1.000
Item 3: Pasta @ Rp 2.000, qty 3, no disc

Struk:
Sabun
  @Rp 1.000/pcs
2 x Rp 800         Rp 1.600
  Diskon: -Rp 400

Shampoo
  @Rp 5.000/pcs
1 x Rp 4.000       Rp 4.000
  Diskon: -Rp 1.000

Pasta
3 x Rp 2.000       Rp 6.000
--------------------------------
Subtotal           Rp 11.600
Diskon item        -Rp 1.400
PPN (11%)           Rp 1.122
--------------------------------
TOTAL              Rp 11.322
```

---

## ✅ Validation di Code

### Max Discount Check:
```kotlin
val maxDiscount = product.price * quantity
val safeDiscount = discountAmount.coerceIn(0.0, maxDiscount)
```

**Purpose:**
- Prevent diskon lebih besar dari total harga
- Sabun Rp 1.000, qty 2 → max discount = Rp 2.000
- Jika user input Rp 3.000 → system coerce to Rp 2.000

---

## 🎨 Visual Examples

### Format 1: Item dengan Diskon
```
┌──────────────────────────────┐
│ Sabun Mandi                  │
│   @Rp 1.000/pcs  (original)  │
│ 2 x Rp 800     = Rp 1.600    │
│   Diskon: -Rp 400            │
└──────────────────────────────┘
```

### Format 2: Item tanpa Diskon
```
┌──────────────────────────────┐
│ Pasta Gigi                   │
│ 3 x Rp 2.000   = Rp 6.000    │
└──────────────────────────────┘
```

---

## 📊 Comparison: Opsi A vs Opsi B

### Opsi A (Flat Discount - TIDAK DIPAKAI):
```
Sabun @ Rp 1.000
Diskon: Rp 200 (flat)

1 pcs: Rp 1.000 - Rp 200 = Rp 800 (Rp 800/pcs)
2 pcs: Rp 2.000 - Rp 200 = Rp 1.800 (Rp 900/pcs) ❌
5 pcs: Rp 5.000 - Rp 200 = Rp 4.800 (Rp 960/pcs) ❌
```
❌ Harga per pcs berubah-ubah → confusing!

### Opsi B (Per Unit Discount - ✅ DIPAKAI):
```
Sabun @ Rp 1.000
Diskon: Rp 200/pcs

1 pcs: Rp 1.000 - Rp 200 = Rp 800 (Rp 800/pcs) ✅
2 pcs: Rp 2.000 - Rp 400 = Rp 1.600 (Rp 800/pcs) ✅
5 pcs: Rp 5.000 - Rp 1.000 = Rp 4.000 (Rp 800/pcs) ✅
```
✅ Harga per pcs konsisten → clear & fair!

---

## 🔧 Files Modified

1. ✅ `ESCPosPrinter.kt`
   - Calculate `discountPerUnit` and `discountedPricePerUnit`
   - Display format: `@Rp X/pcs` for original price
   - Display format: `Qty x Rp Y = Subtotal` for discounted price
   - Display total discount amount

2. ✅ `ReceiptPrinter.kt` (Thermal PDF)
   - Same calculation logic
   - Strikethrough for original price
   - Clear display of per-unit discount

3. ✅ `ReceiptPrinter.kt` (A4 PDF)
   - Same logic applied

---

## 📦 Build Status

✅ **BUILD SUCCESSFUL**
```
42 actionable tasks: 6 executed, 36 up-to-date
Warnings: Only deprecated API (cosmetic)
```

---

## 🎯 Summary

### ✅ Logika Diskon:
**OPSI B** - Diskon per unit, total diskon = diskon per unit × quantity

### ✅ Tampilan Struk:
```
Produk
  @Rp X/pcs (harga asli)
Qty x Rp Y = Subtotal (Y = harga setelah diskon per pcs)
  Diskon: -Rp Z (total diskon)
```

### ✅ Benefits:
- Standar industri retail ✅
- Harga per unit konsisten ✅
- Mudah dipahami customer ✅
- Marketing friendly ✅
- Best practice ✅

### ✅ Example:
```
Sabun @ Rp 1.000
Diskon Rp 200/pcs
Beli 2 pcs:

Sabun
  @Rp 1.000/pcs
2 x Rp 800         Rp 1.600
  Diskon: -Rp 400
```

**Customer lihat:**
- "Oh, sabun harga asli Rp 1.000"
- "Dapat diskon jadi Rp 800/pcs"
- "Saya beli 2, jadi 2 × Rp 800 = Rp 1.600"
- "Total hemat Rp 400"

✅ **Clear, transparent, professional!**

---

**Status: ✅ COMPLETE & DOCUMENTED**

