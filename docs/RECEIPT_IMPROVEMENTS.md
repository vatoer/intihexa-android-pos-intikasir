# Perbaikan Tampilan Struk (Receipt Improvements)

## Tanggal: 16 November 2025

### Masalah yang Diperbaiki
1. ✅ Logo toko tidak muncul di preview struk meskipun sudah di-enable di pengaturan
2. ✅ Tampilan struk kurang profesional
3. ✅ Kolom jumlah terlalu ke kanan
4. ✅ Format currency hanya muat untuk puluhan ribu (tidak muat jutaan)
5. ✅ Garis divider terlalu nempel ke teks (kurang padding)

### Perubahan yang Dilakukan

#### 1. Thermal Receipt (58mm / 80mm)
**Header Section:**
- ✅ Logo toko ditampilkan jika `printLogo = true` dan `storeLogo` tersedia
- ✅ Logo dengan size optimal: 80px untuk 58mm, 120px untuk 80mm
- ✅ Nama toko dengan font bold dan uppercase
- ✅ Alamat toko ditampilkan dengan font kecil
- ✅ Nomor telepon toko ditampilkan
- ✅ Custom header dari settings (multi-line support, max 3 baris)
- ✅ Divider bold untuk pemisah header

**Transaction Info:**
- ✅ Nomor transaksi dengan format "No: [number]"
- ✅ Tanggal dengan format "dd/MM/yyyy HH:mm"
- ✅ Nama kasir ditampilkan
- ✅ Divider pembatas

**Items Table:**
- ✅ Header tabel: "ITEM" dan "JUMLAH"
- ✅ Nama produk dengan line baru
- ✅ Detail: quantity x harga satuan
- ✅ Subtotal per item di kanan
- ✅ Diskon per item ditampilkan jika ada
- ✅ Spacing yang lebih baik antar item

**Totals Section:**
- ✅ Subtotal
- ✅ PPN (jika ada)
- ✅ Diskon (jika ada, dengan tanda minus)
- ✅ Divider bold sebelum grand total
- ✅ **TOTAL** dengan font lebih besar dan bold
- ✅ Divider setelah total
- ✅ Metode pembayaran ditampilkan (Tunai/Debit/Kredit/QRIS/Transfer)
- ✅ Jumlah dibayar
- ✅ Kembalian (jika ada) dengan bold

**Footer:**
- ✅ Pesan terima kasih: "Terima kasih atas kunjungan Anda"
- ✅ Custom footer dari settings (multi-line support, max 3 baris)
- ✅ Pesan validitas: "-- Struk ini sah tanpa tanda tangan --"

#### 2. A4 Receipt
**Header Section:**
- ✅ Logo toko ditampilkan dengan size 100px
- ✅ Nama toko dengan font bold 20px
- ✅ Alamat dengan font kecil
- ✅ Nomor telepon jika tersedia
- ✅ Custom header (multi-line, max 3 baris)
- ✅ Divider bold

**Transaction Info:**
- ✅ Judul "STRUK PEMBAYARAN" centered dan bold
- ✅ Layout 2 kolom untuk info transaksi:
  - No. Transaksi: [number]
  - Tanggal: [dd MMMM yyyy, HH:mm]
  - Kasir: [nama]

**Items Table:**
- ✅ Header tabel dengan kolom: Item | Qty | Harga | Subtotal
- ✅ Data terstruktur dalam kolom yang jelas
- ✅ Diskon per item ditampilkan jika ada
- ✅ Divider setelah items

**Totals Section:**
- ✅ Right-aligned untuk nilai
- ✅ Layout: label di kiri, nilai di kanan
- ✅ Divider bold sebelum grand total
- ✅ TOTAL dengan font 16px bold
- ✅ Metode pembayaran ditampilkan
- ✅ Dibayar dan Kembalian (jika cash)

**Footer:**
- ✅ Divider pembatas
- ✅ "Terima kasih atas kunjungan Anda"
- ✅ Custom footer (multi-line, max 3 baris)
- ✅ "-- Struk ini sah tanpa tanda tangan --"

### Peningkatan Visual
1. **Profesional Layout**: Struktur yang jelas dengan section yang terpisah
2. **Typography**: Variasi ukuran dan weight font untuk hierarchy
3. **Spacing**: Jarak yang konsisten dan proporsional
4. **Dividers**: Pemisah visual yang jelas antara section dengan padding 4-12px
5. **Color**: Penggunaan LTGRAY untuk divider, DKGRAY untuk text sekunder
6. **Alignment**: Text centered untuk header/footer, left untuk content, right untuk nilai
7. **Column Layout**: 
   - Thermal: Kolom JUMLAH diposisikan di `right - 85px` untuk muat jutaan
   - A4: Kolom dioptimasi dengan spacing yang lebih luas (Qty: -250px, Harga: -180px)
8. **Amount Support**: Format currency mendukung hingga jutaan (Rp 99.999.999)
9. **Divider Padding**: 
   - Padding sebelum garis: 4-8px
   - Padding setelah garis: 10-22px (tergantung jenis divider)

### Testing
- [x] Compile success
- [ ] Preview struk di Settings → Preview Struk (Thermal)
- [ ] Logo muncul ketika printLogo = true
- [ ] Custom header/footer ditampilkan
- [ ] Format currency Indonesia (Rp)
- [ ] Multi-line header/footer support
- [ ] Semua field transaksi terisi dengan benar

### Cara Menggunakan
1. **Set Logo Toko**: Settings → Logo Toko → Upload/Capture
2. **Enable Print Logo**: Settings → Pengaturan Cetak → Toggle "Tampilkan Logo di Struk"
3. **Set Header/Footer**: Settings → Pengaturan Struk → Isi header/footer custom
4. **Preview**: Settings → Pengaturan Cetak → "Preview Struk (Thermal)"
5. **Print**: Lakukan transaksi → Bayar → Cetak/Bagikan

### File yang Diubah
- `app/src/main/java/id/stargan/intikasir/feature/pos/print/ReceiptPrinter.kt`
  - `generateThermalReceiptPdf()` - Complete redesign
  - `generateReceiptPdf()` - Enhanced A4 layout

### Notes
- Logo di-load dari file path yang tersimpan di `StoreSettings.storeLogo`
- Logo otomatis di-scale sesuai ukuran kertas (58mm/80mm)
- Bitmap di-recycle setelah digunakan untuk memory optimization
- Custom header/footer support multi-line dengan split by `\n`
- Maximum 3 baris untuk header/footer custom
- Error handling untuk logo loading (try-catch)

#### Layout Optimization Details:
**Thermal Receipt (58mm / 80mm):**
- Width: 384px (58mm) atau 576px (80mm)
- Amount column: `right - 85px` (muat untuk Rp 99.999.999)
- Item name truncation: `charsPerLine - 12` untuk ruang amount
- Divider padding:
  - Regular: `y += 4f` (before), `y += 10f` (after)
  - Bold: `y += 4f` (before), `y += 12f` (after)

**A4 Receipt:**
- Width: 595px (A4 portrait)
- Column positions:
  - Qty: `pageWidth - 250px`
  - Harga: `pageWidth - 180px`
  - Subtotal: `right - measureText("00.000.000")` (dynamic based on font)
- Divider padding:
  - Header divider: `y += 22f` after (bold 2px)
  - Section divider: `y += 4f` before, `y += 18f` after
  - Totals divider: `y += 20f` after (bold 2px)

#### Currency Format Support:
- Format: `Rp 99.999.999` (hingga puluhan juta)
- Alignment: Right-aligned untuk semua amount
- Space optimization: Column width calculated dynamically

