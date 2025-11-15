# Payment Screen UI Improvements

## Tanggal: 15 November 2025

## Perubahan yang Dilakukan

### 1. **Metode Pembayaran: Layout 1x4 Horizontal dengan Icon**

**Sebelum**: 2x2 grid tanpa icon
**Setelah**: **1x4 horizontal layout** (1 baris, 4 kolom) dengan icon yang jelas

#### Icon untuk Setiap Metode:
- 💵 **CASH** → `Icons.Default.Payments`
- 📱 **QRIS** → `Icons.Default.QrCode2`
- 🏦 **TRANSFER** → `Icons.Default.AccountBalance`
- 💳 **CARD** → `Icons.Default.CreditCard`

#### Keuntungan:
- ✅ Visual lebih jelas dengan icon di atas text
- ✅ Lebih mudah dikenali secara cepat
- ✅ Layout horizontal menghemat ruang vertikal
- ✅ Tombol optimal (height 64dp) dengan icon + text vertikal
- ✅ Text dengan font weight berbeda (SemiBold saat selected)
- ✅ Icon berubah warna saat selected (primary color)
- ✅ Setiap tombol width sama (weight = 1f)

### 2. **Scrollable Content**

**Problem**: Saat screen panjang, field catatan tidak terlihat karena keyboard atau konten terlalu banyak

**Solusi**: 
- Tambahkan `rememberScrollState()` dan `verticalScroll(scrollState)`
- Content sekarang bisa di-scroll untuk akses semua field
- Bottom button tetap sticky di bawah (tidak ikut scroll)

#### Implementasi:
```kotlin
val scrollState = rememberScrollState()

Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(scrollState)  // ← Added this
        .padding(horizontal = 12.dp, vertical = 8.dp),
    // ...
)
```

### 3. **Tombol Checkout → Bayar**

Changed button text from "Checkout" to "Bayar" untuk konsistensi bahasa Indonesia.

## Struktur Layout Final

```
┌─────────────────────────────┐
│ [←] Pembayaran              │ ← TopBar
├─────────────────────────────┤
│ ╔═══════════════════════╗   │
│ ║ Ringkasan Pesanan     ║   │ ← Scrollable
│ ║ Subtotal (bruto)      ║   │   Content
│ ║ Diskon item           ║   │
│ ║ Subtotal              ║   │
│ ║ PPN                   ║   │
│ ║ Diskon global         ║   │
│ ║ Total                 ║   │
│ ╚═══════════════════════╝   │
│                              │
│ [Diskon Global Field]        │
│                              │
│ Metode Pembayaran            │
│ ┌─────┬─────┬─────┬─────┐   │
│ │ 💵  │ 📱  │ 🏦  │ 💳  │   │ ← 1 Row, 4 Columns
│ │CASH │QRIS │TRNF │CARD │   │   (Horizontal)
│ └─────┴─────┴─────┴─────┘   │
│                              │
│ Cash diterima (if CASH)      │
│ [Quick amounts: 3x2 grid]    │
│ [Custom amount field]        │
│                              │
│ [Catatan field]              │ ← Can scroll here
├─────────────────────────────┤
│ [        Bayar        ]      │ ← Sticky Bottom
└─────────────────────────────┘
```

## Testing Checklist

- [x] Build berhasil tanpa error
- [ ] Scroll bekerja dengan baik
- [ ] Icon payment method tampil dengan benar
- [ ] Tombol payment method responsif saat di-tap
- [ ] Selected state terlihat jelas (background + icon color)
- [ ] Keyboard muncul → bisa scroll ke field catatan
- [ ] Bottom button tetap terlihat dan tidak overlap

## Files Modified

1. `app/src/main/java/id/stargan/intikasir/feature/pos/ui/payment/PaymentScreenReactive.kt`
   - Added imports for scroll and icons
   - Added `scrollState`
   - Changed payment method layout from 2x2 to 1x4
   - Added icons for each payment method
   - Made content scrollable
   - Changed "Checkout" to "Bayar"

## Preview

### Payment Methods - Horizontal Layout (1x4)
```
┌──────┬──────┬──────┬──────┐
│  💵  │  📱  │  🏦  │  💳  │
│ CASH │ QRIS │TRANS │ CARD │  ← 1 Row, 4 Equal Columns
└──────┴──────┴──────┴──────┘
   ↑ Selected (blue background)
```

**Layout Details**:
- **Row**: Horizontal arrangement dengan `Arrangement.spacedBy(8.dp)`
- **Buttons**: Setiap tombol menggunakan `Modifier.weight(1f)` untuk width yang sama
- **Height**: 64dp untuk ruang icon + text
- **Content**: 
  - Icon (24dp) di atas
  - Spacer 4dp
  - Text (labelSmall) di bawah
- **Selected State**: 
  - Background: primary color dengan alpha 0.12
  - Icon: primary color
  - Text: SemiBold font weight

## Next Improvements (Optional)

1. Animasi saat ganti payment method
2. Disable payment methods yang belum aktif (jika ada business logic)
3. Tambah instruksi untuk masing-masing payment method
4. QR Code scanner untuk QRIS
5. Validasi nomor rekening untuk Transfer

