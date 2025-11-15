# Komponen Ringkasan Pesanan (Order Summary Component)

## Ringkasan
Komponen ringkasan pesanan yang reusable dan konsisten digunakan di semua screen POS (Kasir, Keranjang, dan Pembayaran).

## File yang Dibuat

### OrderSummaryCard.kt
**Lokasi**: `app/src/main/java/id/stargan/intikasir/feature/pos/ui/components/OrderSummaryCard.kt`

Komponen card yang menampilkan breakdown lengkap pesanan:
- **Subtotal (bruto)**: Total harga sebelum diskon item
- **Diskon item**: Total diskon per-item (jika ada)
- **Subtotal**: Total setelah diskon item
- **PPN**: Pajak (jika diaktifkan)
- **Diskon global**: Diskon transaksi (jika ada)
- **Total**: Total akhir yang harus dibayar

## File yang Diupdate

### 1. PaymentScreenReactive.kt
**Perubahan**:
- Import `OrderSummaryCard` component
- Menghitung `itemDiscountTotal` dan `grossSubtotal` dari `transactionItems`
- Replace Card ringkasan dengan `OrderSummaryCard` component
- Komponen tetap compact dan user-friendly

**Komponen yang digunakan**:
```kotlin
OrderSummaryCard(
    grossSubtotal = grossSubtotal,
    itemDiscount = itemDiscountTotal,
    netSubtotal = state.subtotal,
    taxRate = state.taxRate,
    taxAmount = state.tax,
    globalDiscount = state.globalDiscount,
    total = state.total
)
```

### 2. CartSummaryReactive.kt
**Perubahan**:
- Sekarang menggunakan `OrderSummaryCard` untuk konsistensi
- Menghitung `itemDiscountTotal` dan `grossSubtotal`
- Wrapped dalam Surface dengan shadow elevation untuk visual consistency

**Fungsi**:
- Menampilkan ringkasan yang sama persis dengan screen lainnya
- Digunakan di POS screen (top section)

### 3. CartScreenReactive.kt
**Perubahan**:
- Import `OrderSummaryCard` dan `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`
- Menghitung `itemDiscountTotal` dan `grossSubtotal`
- Replace Card ringkasan di bottomBar dengan `OrderSummaryCard`
- Hapus import dan variabel yang tidak digunakan

**Fungsi**:
- Menampilkan ringkasan lengkap sebelum lanjut ke pembayaran

## Keuntungan Implementasi

### 1. **Konsistensi UI/UX**
- Semua screen (POS, Cart, Payment) menampilkan ringkasan yang identik
- User experience lebih baik karena tidak bingung dengan format berbeda

### 2. **Maintainability**
- Single source of truth untuk layout ringkasan
- Perubahan format cukup dilakukan di satu tempat (`OrderSummaryCard.kt`)
- Mengurangi code duplication

### 3. **Transparency**
- User dapat melihat breakdown lengkap:
  - Harga asli (bruto)
  - Diskon per-item
  - Subtotal bersih
  - Pajak
  - Diskon global
  - Total akhir

### 4. **Data Integrity**
- Semua perhitungan menggunakan data dari database (reactive)
- Item discount disimpan dan dipertahankan saat quantity berubah
- ViewModel memastikan discount tidak melebihi harga * quantity

## Perhitungan yang Dilakukan

```kotlin
// Di setiap screen yang menggunakan OrderSummaryCard:

// 1. Gross Subtotal (harga asli sebelum diskon item)
val grossSubtotal = transactionItems.sumOf { it.unitPrice * it.quantity }

// 2. Item Discount Total
val itemDiscountTotal = transactionItems.sumOf { it.discount }

// 3. Net Subtotal (dari state, sudah dihitung di ViewModel)
val netSubtotal = state.subtotal // = grossSubtotal - itemDiscountTotal

// 4. Tax (dari state, sudah dihitung di ViewModel)
val taxAmount = state.tax // = netSubtotal * taxRate

// 5. Global Discount (dari transaction entity)
val globalDiscount = state.globalDiscount

// 6. Total (dari state)
val total = state.total // = netSubtotal + taxAmount - globalDiscount
```

## Testing Checklist

- [x] Build berhasil tanpa error
- [ ] POS Screen menampilkan ringkasan dengan benar
- [ ] Cart Screen menampilkan ringkasan dengan benar
- [ ] Payment Screen menampilkan ringkasan dengan benar
- [ ] Diskon item terlihat di semua screen
- [ ] Format mata uang konsisten (Rp dengan spasi)
- [ ] Perhitungan matematis akurat
- [ ] UI responsive dan tidak overlap

## Screenshot Locations

Untuk dokumentasi lebih lanjut, tambahkan screenshot dari:
1. POS Screen - bagian top summary
2. Cart Screen - bagian bottom summary
3. Payment Screen - bagian top summary

## Next Steps (Optional)

1. Tambahkan animasi saat nilai berubah
2. Tambahkan tooltip untuk menjelaskan setiap baris
3. Export/Print invoice dengan format yang sama
4. Tambahkan unit test untuk perhitungan

