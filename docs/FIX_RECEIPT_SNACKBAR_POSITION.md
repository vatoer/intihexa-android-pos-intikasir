# Fix: Snackbar Menutupi Tombol di Receipt Screen

## Masalah

Snackbar muncul di bawah layar dan **menutupi tombol-tombol aksi** (Cetak, Bagikan, Antrian, dll) sehingga mengganggu user experience.

## Solusi

Memindahkan posisi Snackbar dari **bawah** ke **atas** layar dimana tidak ada tombol.

### SEBELUM
```kotlin
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },  // Default: Bottom
    // ...
)
```

**Masalah**: 
- ❌ Snackbar muncul di bawah
- ❌ Menutupi tombol Cetak, Bagikan, Antrian
- ❌ User tidak bisa klik tombol saat snackbar muncul

### SESUDAH
```kotlin
Scaffold(
    snackbarHost = {
        // Position snackbar at top to avoid covering bottom buttons
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    },
    // ...
)
```

**Keuntungan**:
- ✅ Snackbar muncul di atas (area kosong)
- ✅ Tidak menutupi tombol-tombol
- ✅ User tetap bisa interact dengan tombol
- ✅ UX lebih baik

## Visual Layout

```
┌─────────────────────────────┐
│     TopAppBar               │
├─────────────────────────────┤
│  ┌─────────────────────┐   │ ← Snackbar di sini (Top)
│  │ ✓ Transaksi Selesai │   │
│  └─────────────────────┘   │
│                             │
│   Success Icon & Info       │
│   Receipt Details           │
│                             │
│   [Selesai]                 │ ← Tombol tidak tertutup
│   [Cetak] [Bagikan]         │
│   [Cetak Antrian]           │
│   [Buat Transaksi Baru]     │
│   [Kembali ke Menu Utama]   │
└─────────────────────────────┘
```

## Implementation Details

### Custom SnackbarHost dengan Box
```kotlin
Box(
    modifier = Modifier.fillMaxSize(),      // Full screen
    contentAlignment = Alignment.TopCenter  // Position at top
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(top = 8.dp)  // Spacing dari top
    )
}
```

### Snackbar Messages di Screen Ini
1. **"Transaksi telah diselesaikan"** - Saat klik tombol Selesai
2. Print feedback - Dari HomeNavGraph (toast, bukan snackbar ini)

## Best Practice

### ✅ DO
- Position snackbar di area yang tidak menutupi aksi utama
- Gunakan `Alignment.TopCenter` untuk screen dengan banyak tombol di bawah
- Berikan padding untuk spacing dari edge

### ❌ DON'T
- Jangan biarkan snackbar menutupi interactive elements
- Jangan gunakan default bottom position jika ada tombol di bawah

## Testing Checklist

- [x] Build berhasil
- [ ] Test: Klik "Selesai" → snackbar muncul di atas
- [ ] Test: Tombol Cetak/Bagikan/Antrian tetap bisa diklik saat snackbar muncul
- [ ] Test: Snackbar tidak overlap dengan konten
- [ ] Test: Snackbar auto-dismiss setelah durasi Short

## Files Changed

- ✅ **ReceiptScreen.kt** - SnackbarHost positioning

---
**Status**: ✅ Fixed
**Date**: 20 November 2025
**Impact**: UX improvement - no more button blocking

