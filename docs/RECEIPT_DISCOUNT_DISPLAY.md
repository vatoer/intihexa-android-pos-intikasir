# Receipt Item Discount Display Improvement

## Tanggal: 18 November 2025

## Masalah yang Diperbaiki

### Item dengan Diskon Tidak Jelas di Struk

**Sebelum:**
```
Produk A
1 x Rp 4.000 = Rp 4.000
```
❌ **Masalah:**
- Tidak terlihat harga asli (Rp 5.000)
- Tidak terlihat ada diskon Rp 1.000
- User bingung kenapa harga berbeda

**Sesudah:**
```
Produk A
  @Rp 5.000 (harga asli, dicoret)
1 x Rp 4.000 = Rp 4.000
  Diskon: -Rp 1.000
```
✅ **Perbaikan:**
- Harga asli ditampilkan dengan tanda @ (strikethrough)
- Quantity x harga setelah diskon = subtotal
- Diskon amount jelas terlihat

---

## Implementation

### 1. ESC/POS Thermal Printer (Bluetooth)
**File:** `ESCPosPrinter.kt`

**Format untuk item dengan diskon:**
```
Produk A
  @Rp 5.000           ← Harga asli (dengan @)
1 x Rp 4.000 = Rp 4.000  ← Qty x harga diskon = subtotal
  Diskon: -Rp 1.000   ← Jumlah diskon
```

**Format untuk item tanpa diskon:**
```
Produk A
1 x Rp 4.000 = Rp 4.000
```

**Code:**
```kotlin
items.forEach { itx ->
    text(itx.productName.take(cpl))
    
    if (itx.discount > 0) {
        val originalPrice = itx.productPrice
        // Show original price with @ prefix
        text("  @${nf.format(originalPrice)}")
        
        // Discounted line
        val qty = "${itx.quantity} x ${nf.format(itx.unitPrice)}"
        val sub = nf.format(itx.subtotal)
        text(qty + " " + sub)
        
        // Discount amount
        text("  Diskon: -${nf.format(itx.discount)}")
    } else {
        // Simple format without discount
        val qty = "${itx.quantity} x ${nf.format(itx.unitPrice)}"
        val sub = nf.format(itx.subtotal)
        text(qty + " " + sub)
    }
}
```

---

### 2. Thermal PDF (58mm/80mm)
**File:** `ReceiptPrinter.kt` - `generateThermalReceiptPdf()`

**Format untuk item dengan diskon:**
```
Produk A
  @Rp 5.000 (strikethrough line)
1 x Rp 4.000         Rp 4.000
  Diskon: -Rp 1.000
```

**Code:**
```kotlin
items.forEach { item ->
    canvas.drawText(name, left, y, textPaint)
    y += 16f
    
    if (item.discount > 0) {
        // Original price with strikethrough
        val origPriceStr = "@${nf.format(originalPrice)}"
        canvas.drawText(origPriceStr, left + 8f, y, smallPaint)
        
        // Draw strikethrough line
        val lineY = y - 4f
        canvas.drawLine(left + 8f, lineY, left + 8f + textWidth, lineY, smallPaint)
        y += 14f
    }
    
    // Quantity x discounted price = subtotal
    val qtyPart = "${item.quantity} x ${nf.format(item.unitPrice)}"
    val subPart = nf.format(item.subtotal)
    canvas.drawText(qtyPart, left + 8f, y, smallPaint)
    canvas.drawText(subPart, right - paint.measureText(subPart), y, textPaint)
    y += 18f
    
    if (item.discount > 0) {
        val discountPart = "Diskon: -${nf.format(item.discount)}"
        canvas.drawText(discountPart, left + 8f, y, smallPaint)
        y += 16f
    }
}
```

---

### 3. A4 PDF (Standard Printer)
**File:** `ReceiptPrinter.kt` - `generateReceiptPdf()`

**Format untuk item dengan diskon:**
```
Nama Produk    Qty    Harga (strikethrough)    Subtotal
                      Harga diskon: Rp 4.000
                      Diskon: -Rp 1.000
```

**Code:**
```kotlin
items.forEach { item ->
    if (item.discount > 0) {
        // Line 1: Name, Qty, Original Price (strikethrough), Subtotal
        canvas.drawText(name, xPadding, y, normalPaint)
        canvas.drawText(qty, qtyColumnX, y, normalPaint)
        
        val origPriceStr = nf.format(originalPrice)
        canvas.drawText(origPriceStr, priceColumnX, y, normalPaint)
        // Strikethrough
        canvas.drawLine(priceColumnX, y - 4f, priceColumnX + textWidth, y - 4f, normalPaint)
        
        canvas.drawText(sub, rightX, y, normalPaint)
        y += 18f
        
        // Line 2: Discounted price
        canvas.drawText("Harga diskon: ${nf.format(discountedPrice)}", xPadding + 20f, y, smallPaint)
        y += 14f
        
        // Line 3: Discount amount
        canvas.drawText("Diskon: -${nf.format(item.discount)}", xPadding + 20f, y, smallPaint)
        y += 18f
    } else {
        // Simple format
        canvas.drawText(name, xPadding, y, normalPaint)
        canvas.drawText(qty, qtyColumnX, y, normalPaint)
        canvas.drawText(price, priceColumnX, y, normalPaint)
        canvas.drawText(sub, rightX, y, normalPaint)
        y += 18f
    }
}
```

---

## Visual Examples

### ESC/POS Thermal (58mm)
```
================================
         TOKO SAYA
    Jl. Contoh No. 123
--------------------------------
Produk A (diskon 20%)
  @Rp 5.000
1 x Rp 4.000         Rp 4.000
  Diskon: -Rp 1.000

Produk B (tanpa diskon)
2 x Rp 3.000         Rp 6.000
--------------------------------
Subtotal            Rp 10.000
Diskon item         -Rp 1.000
PPN (11%)            Rp 1.000
--------------------------------
TOTAL               Rp 10.000
================================
```

### Thermal PDF (80mm)
```
╔══════════════════════════════════╗
║         TOKO SAYA                ║
║    Jl. Contoh No. 123            ║
╠══════════════════════════════════╣
║ ITEM                    JUMLAH   ║
╠──────────────────────────────────╣
║ Produk A                         ║
║   @Rp 5.000 (strikethrough)      ║
║   1 x Rp 4.000         Rp 4.000  ║
║   Diskon: -Rp 1.000              ║
║                                  ║
║ Produk B                         ║
║   2 x Rp 3.000         Rp 6.000  ║
╠══════════════════════════════════╣
║ Subtotal              Rp 10.000  ║
║ Diskon item           -Rp 1.000  ║
║ PPN (11%)              Rp 1.000  ║
╠══════════════════════════════════╣
║ TOTAL                 Rp 10.000  ║
╚══════════════════════════════════╝
```

### A4 PDF (Table Format)
```
┌─────────────┬─────┬──────────────┬──────────┐
│ Item        │ Qty │ Harga        │ Subtotal │
├─────────────┼─────┼──────────────┼──────────┤
│ Produk A    │ 1   │ Rp 5.000     │ Rp 4.000 │
│             │     │ (strikethrough)         │
│ Harga diskon: Rp 4.000                      │
│ Diskon: -Rp 1.000                           │
├─────────────┼─────┼──────────────┼──────────┤
│ Produk B    │ 2   │ Rp 3.000     │ Rp 6.000 │
└─────────────┴─────┴──────────────┴──────────┘
```

---

## Benefits

### 1. Transparency (Transparansi)
- ✅ Customer tahu harga asli produk
- ✅ Customer tahu berapa diskon yang didapat
- ✅ Builds trust dengan customer

### 2. Clarity (Kejelasan)
- ✅ Tidak ada kebingungan kenapa harga berbeda
- ✅ Clear breakdown dari perhitungan
- ✅ Professional appearance

### 3. Compliance (Kepatuhan)
- ✅ Sesuai dengan praktik retail yang baik
- ✅ Informasi lengkap di struk
- ✅ Audit trail yang jelas

### 4. Consistency (Konsistensi)
- ✅ Format sama untuk semua jenis printer
- ✅ ESC/POS, Thermal PDF, A4 PDF semua konsisten
- ✅ User experience yang seragam

---

## Technical Details

### Data Structure
```kotlin
data class TransactionItemEntity(
    val productName: String,
    val productPrice: Double,    // Harga asli/katalog
    val quantity: Int,
    val unitPrice: Double,        // Harga setelah diskon item
    val discount: Double,         // Jumlah diskon (productPrice - unitPrice) * quantity
    val subtotal: Double          // unitPrice * quantity
)
```

### Logic Flow
```kotlin
if (item.discount > 0) {
    // Item has discount
    val originalPrice = item.productPrice       // e.g., 5000
    val discountedPrice = item.unitPrice        // e.g., 4000
    val discountAmount = item.discount          // e.g., 1000
    
    // Display:
    // 1. Product name
    // 2. Original price (strikethrough)
    // 3. Qty x discounted price = subtotal
    // 4. Discount amount
} else {
    // No discount - simple format
    // 1. Product name
    // 2. Qty x price = subtotal
}
```

---

## Files Modified

1. ✅ `feature/pos/print/ESCPosPrinter.kt`
   - Updated `writeReceipt()` function
   - Added original price display with @
   - Added discount amount line
   - Conditional formatting based on discount

2. ✅ `feature/pos/print/ReceiptPrinter.kt`
   - Updated `generateThermalReceiptPdf()` function
   - Added strikethrough for original price
   - Updated `generateReceiptPdf()` function (A4)
   - Added discounted price line

---

## Testing Checklist

### ESC/POS Thermal:
- [x] Item tanpa diskon: Simple format
- [x] Item dengan diskon: Show original price dengan @
- [x] Item dengan diskon: Show qty x discounted price
- [x] Item dengan diskon: Show discount amount
- [x] Format rapi dalam 32/48 chars

### Thermal PDF:
- [x] Item tanpa diskon: Simple format
- [x] Item dengan diskon: Show original price dengan strikethrough
- [x] Item dengan diskon: Show qty x discounted price
- [x] Item dengan diskon: Show discount amount
- [x] Layout rapi dan terbaca

### A4 PDF:
- [x] Item tanpa diskon: Simple table row
- [x] Item dengan diskon: Original price dengan strikethrough
- [x] Item dengan diskon: Discounted price line
- [x] Item dengan diskon: Discount amount line
- [x] Table alignment correct

---

## Build Status

✅ **BUILD SUCCESSFUL**
```
42 actionable tasks: 13 executed, 29 up-to-date
Warnings: Only deprecated Locale (cosmetic)
```

---

## Summary

**Problem:** Diskon item tidak terlihat di struk - hanya tampil harga final tanpa informasi harga asli dan diskon

**Solution:**
- ✅ Show harga asli dengan strikethrough/@ prefix
- ✅ Show qty x harga setelah diskon = subtotal
- ✅ Show discount amount separately
- ✅ Implemented di semua format: ESC/POS, Thermal PDF, A4 PDF

**Result:**
- ✅ Customer tahu harga asli
- ✅ Customer tahu berapa diskon yang didapat
- ✅ Transparent dan professional
- ✅ Builds trust

---

**Status: ✅ COMPLETE**

Struk sekarang menampilkan informasi diskon item dengan lengkap dan jelas!

