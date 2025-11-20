# Logo Printing Implementation

## Overview
Logo printing untuk thermal receipt telah direfactor ke file terpisah untuk memudahkan debugging dan maintenance.

## File Structure

### ThermalLogoPrinter.kt
Helper class untuk mencetak logo pada thermal printer via ESC/POS commands.

**Lokasi:** `app/src/main/java/id/stargan/intikasir/feature/pos/print/ThermalLogoPrinter.kt`

**Fungsi Utama:**
- `printLogo(context, out, settings)` - Mencetak logo ke thermal printer

### Ukuran Logo

**Formula:** Logo = 1/3 lebar kertas (square)
**Catatan:** Ukuran dikurangi untuk mencegah buffer overflow pada thermal printer

#### ESC/POS Thermal Printer:
- Paper 58mm → Logo size = 100×100 dots (~12.5×12.5mm)
- Paper 80mm → Logo size = 150×150 dots (~19×19mm)

**Alasan ukuran dikurangi:**
- Mencegah buffer overflow pada printer thermal
- Meningkatkan kompatibilitas dengan berbagai model printer
- Mempercepat proses printing

**Kalkulasi:**
```kotlin
val targetLogoSize = when {
    paperWidthMm >= 80 -> 150 // ~19mm for 80mm paper
    else -> 100 // ~12.5mm for 58mm paper
}
```

#### PDF Receipt (A4 Portrait):
- Page width = 595px
- Logo size = 198x198px (595/3)

#### PDF Receipt (Thermal):
- 58mm: pageWidth=384px → Logo=128x128px
- 80mm: pageWidth=576px → Logo=192x192px

### Posisi Logo
- **ESC/POS:** Logo dicetak di tengah menggunakan `ESC a 1` (center align command)
- **PDF:** Logo di-center horizontal menggunakan koordinat canvas

## Technical Details

### ESC/POS Logo Printing
- Menggunakan **manual padding** dengan perhitungan: `leftPaddingDots = (paperWidthDots - bitmapWidth) / 2`
- Padding dikonversi ke karakter: `leftPaddingChars = leftPaddingDots / 12` (12 dots per char)
- Menggunakan command `ESC * 0` (8-dot single-density mode, 60 DPI) untuk kompatibilitas
- Bitmap **WAJIB di-crop ke square** terlebih dahulu sebelum scaling
- Dicetak dalam strip 8 dots vertical
- Setiap kolom pixel = 1 byte (8 bits)
- Hanya 1 line feed setelah logo selesai

**Cropping ke Square (PENTING!):**
```kotlin
val cropSize = min(originalBitmap.width, originalBitmap.height)
val xOffset = (originalBitmap.width - cropSize) / 2
val yOffset = (originalBitmap.height - cropSize) / 2

val squareBitmap = Bitmap.createBitmap(
    originalBitmap,
    xOffset, yOffset,
    cropSize, cropSize
)
```

**Perubahan dari versi sebelumnya:**
- ❌ **Sebelumnya:** Menggunakan `ESC a 1` untuk center → **Tidak reliable untuk bitmap**
- ✅ **Sekarang:** Manual padding calculation → **Reliable centering**
- ❌ **Sebelumnya:** Logo size 100-150 dots → **Terlalu besar untuk struk**
- ✅ **Sekarang:** Logo size 64-96 dots → **Proporsional untuk struk**
- ❌ **Sebelumnya:** Extra line feed + delay 100ms → **Spacing terlalu besar**
- ✅ **Sekarang:** 1 line feed + delay 50ms → **Spacing pas**

### Debugging
Semua fungsi logo printing memiliki Log.d untuk tracking:
- Original bitmap size
- Target logo size
- Scaled bitmap size
- Print success/failure

## Usage

### ESC/POS
```kotlin
if (settings.printLogo) {
    val logoSuccess = ThermalLogoPrinter.printLogo(context, out, settings)
    if (logoSuccess) {
        // Logo printed successfully
    }
}
```

### PDF
Logo printing sudah terintegrasi di `generateReceiptPdf()` dan `generateThermalReceiptPdf()`.

## Changes Log

### 2025-11-20 (Update 5) - SOLUTION SEMPURNA ✅✅✅
- **Fix:** Logo persegi panjang → Gunakan 24-dot double-density mode (200x200 DPI) untuk aspect ratio 1:1 yang benar
- **Fix:** Logo di kiri → Gunakan `GS L` command (Set Left Margin) untuk centering yang presisi
- **Fix:** Spacing terlalu lebar → Hapus line feed di ThermalLogoPrinter, hanya 1 LF di ESCPosPrinter
- **Technical:** Mode 8-dot (60 DPI) menyebabkan aspect ratio tidak 1:1, diganti dengan 24-dot (200 DPI)
- **Technical:** Manual padding dengan spasi tidak akurat, diganti dengan left margin command

**Solusi Akhir:**
```kotlin
// 1. Crop ke square
val cropSize = min(width, height)
val squareBitmap = Bitmap.createBitmap(original, xOffset, yOffset, cropSize, cropSize)

// 2. Scale ke target (64 atau 96 dots)
val scaled = Bitmap.createScaledBitmap(square, target, target, true)

// 3. Set left margin untuk centering
val leftMarginMm = (paperWidthDots - bitmap.width) / 2 / 8
GS L nL nH  // 0x1D 0x4C nL nH

// 4. Print dengan 24-dot mode (1:1 aspect ratio)
ESC * 33 nL nH  // 0x1B 0x2A 0x21

// 5. Reset margin
GS L 0x00 0x00
```

### 2025-11-20 (Update 4)
- **Fix:** Logo persegi panjang → Memastikan cropping benar-benar square dengan validasi
- **Fix:** Logo di kiri → Manual padding calculation yang akurat (tidak pakai ESC a command)
- **Fix:** Spacing terlalu lebar → Hanya 1 line feed setelah logo
- **Improvement:** Ukuran logo lebih kecil dan proporsional (64-96 dots)
- **Improvement:** Delay dikurangi jadi 50ms untuk lebih responsif

### 2025-11-20 (Update 3)
- **Fix:** Logo aspect ratio - Logo yang sebelumnya memanjang ke atas sekarang benar-benar square
  - Menambahkan cropping ke square sebelum scaling
  - Menggunakan dimensi terkecil dari width/height untuk crop center
- **Fix:** Print berhenti setelah logo
  - Menghapus `text("")` call yang menyebabkan conflict
  - Menambahkan `flush()` setelah logo selesai
  - Menambahkan delay 100ms untuk memberi waktu printer memproses bitmap
- **Improvement:** Lebih stabil dan reliable

### 2025-11-20 (Update 2)
- **Fix:** Printer stuck issue - Mengurangi ukuran logo max (58mm: 100 dots, 80mm: 150 dots)
- **Fix:** Logo posisi kiri - Menggunakan `ESC a 1` command untuk center alignment
- **Fix:** Buffer overflow - Mengganti 24-dot mode dengan 8-dot mode
- **Improvement:** Lebih kompatibel dengan berbagai model thermal printer

### 2025-11-20 (Update 1)
- **Refactor:** Logo printing untuk ESC/POS dipindahkan ke `ThermalLogoPrinter.kt`
- **Fix:** Ukuran logo disesuaikan dengan formula 1/3 lebar kertas (square)
- **Fix:** Logo di-center horizontal untuk ESC/POS dan PDF
- **Improvement:** Menambahkan detailed logging untuk debugging

## Troubleshooting

### Masalah: Logo memanjang ke atas (bukan square)
**Penyebab:** Original bitmap langsung di-scale tanpa crop ke square terlebih dahulu
**Solusi:** 
- Crop bitmap ke square menggunakan dimensi terkecil
- Ambil bagian center dari bitmap original
- Baru kemudian scale ke target size

### Masalah: Print berhenti setelah logo
**Penyebab:** 
- Conflict antara alignment commands
- Buffer tidak di-flush dengan baik
- Printer butuh waktu untuk proses bitmap

**Solusi:**
- Hapus `text("")` setelah logo
- Panggil `flush()` setelah selesai print logo
- Tambahkan delay 100ms sebelum melanjutkan
- Pastikan reset alignment (ESC a 0) dipanggil

### Masalah: Printer stuck / tidak mencetak keseluruhan
**Penyebab:** Ukuran logo terlalu besar menyebabkan buffer overflow
**Solusi:** Kurangi ukuran logo max (sekarang: 100-150 dots)

### Masalah: Logo tercetak di kiri, tidak di tengah
**Penyebab:** Manual padding dengan spasi tidak konsisten
**Solusi:** Gunakan `ESC a 1` command untuk center alignment

### Masalah: Logo tidak tercetak sama sekali
**Kemungkinan penyebab:**
1. File logo tidak ada atau corrupt
2. Format file tidak didukung
3. Izin file tidak mencukupi

**Solusi:** 
- Periksa log untuk detail error
- Pastikan file logo valid (JPEG/PNG)
- Pastikan path logo benar di StoreSettings

