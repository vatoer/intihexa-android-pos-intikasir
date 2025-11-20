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
- Menggunakan command `ESC a 1` untuk center alignment sebelum print logo
- Menggunakan command `ESC * 0` (8-dot single-density mode, 60 DPI) untuk kompatibilitas lebih baik
- Bitmap dikonversi ke monochrome (threshold = 128)
- Dicetak dalam strip 8 dots vertical (bukan 24 dots untuk menghindari buffer overflow)
- Setiap kolom pixel = 1 byte (8 bits)
- Setelah selesai, reset ke left alignment dengan `ESC a 0`

**Perubahan dari versi sebelumnya:**
- ❌ **Sebelumnya:** Menggunakan manual padding dengan spasi → **Tidak konsisten**
- ✅ **Sekarang:** Menggunakan `ESC a 1` command → **Reliable centering**
- ❌ **Sebelumnya:** 24-dot double-density (ESC * 33) → **Buffer overflow**
- ✅ **Sekarang:** 8-dot single-density (ESC * 0) → **Better compatibility**
- ❌ **Sebelumnya:** Logo size up to 384 dots → **Printer stuck**
- ✅ **Sekarang:** Logo size max 150 dots → **Smooth printing**

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

### 2025-11-20 (Update 3) - FINAL FIX
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

