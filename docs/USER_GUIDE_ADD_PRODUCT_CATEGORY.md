# 📖 User Guide - Menambahkan Produk & Kategori

## ✅ Cara Menambahkan Produk Baru

### Langkah-langkah:

1. **Buka Halaman Produk**
   - Dari Home, klik menu "Produk"
   - Anda akan masuk ke halaman "Daftar Produk"

2. **Klik Tombol Tambah (+)**
   - Di pojok kanan bawah, akan ada tombol melayang (FAB) berwarna primary
   - Tombol ini berbentuk bulat dengan ikon (+)
   - **CATATAN:** Tombol ini hanya muncul untuk user dengan role **ADMIN**

3. **Isi Form Produk**
   - **Nama Produk*** (wajib diisi)
   - **SKU** (opsional - kode produk)
   - **Barcode** (opsional - klik tombol scan untuk scan)
   - **Kategori** (pilih dari dropdown)
   - **Deskripsi** (opsional)
   - **Harga Jual*** (wajib diisi, harus > 0)
   - **Harga Modal** (opsional)
   - **Stok*** (wajib diisi, harus >= 0)
   - **Stok Minimum** (opsional - untuk alert stok rendah)
   - **URL Gambar** (opsional - klik tombol pick untuk pilih)
   - **Status Aktif** (switch on/off)

4. **Simpan Produk**
   - Klik tombol "Simpan" di bawah form
   - Validasi akan berjalan otomatis
   - Jika ada error, akan muncul pesan merah di bawah field
   - Jika berhasil, akan kembali ke Daftar Produk
   - Produk baru akan muncul di list

---

## ✅ Cara Menambahkan Kategori Baru

### Langkah-langkah:

1. **Buka Halaman Kategori**
   
   **Cara 1 - Dari Daftar Produk:**
   - Buka halaman "Daftar Produk"
   - Klik ikon Category (📁) di toolbar atas
   
   **Cara 2 - Dari Form Produk:**
   - Saat mengisi form produk
   - Di bagian dropdown Kategori
   - Klik "Kelola Kategori" (jika tersedia)

2. **Klik Tombol Tambah (+)**
   - Di pojok kanan bawah halaman Kelola Kategori
   - Tombol melayang (FAB) berwarna primary
   - Bentuk bulat dengan ikon (+)

3. **Isi Form Kategori**
   - **Nama Kategori*** (wajib diisi)
   - **Deskripsi** (opsional)
   - **Warna** (opsional - kode hex seperti #FF5722)
   - **Ikon** (opsional - emoji atau nama ikon)

4. **Simpan Kategori**
   - Klik tombol "Simpan"
   - Jika berhasil, kategori baru akan muncul di list
   - Kategori ini bisa langsung digunakan saat menambah produk

---

## 🎯 Lokasi Tombol Tambah

### Di Halaman Daftar Produk:
```
┌─────────────────────────────────┐
│  ← Daftar Produk      🔍 ⚙️ 📁 │  ← TopBar
├─────────────────────────────────┤
│                                 │
│  [Produk 1]                     │
│  [Produk 2]                     │
│  [Produk 3]                     │  ← List
│  ...                            │
│                                 │
│                            ┌─┐  │
│                            │+│  │  ← FAB (Admin only)
└────────────────────────────└─┘──┘
```

**Tombol (+) di kanan bawah = Tambah Produk**

### Di Halaman Kelola Kategori:
```
┌─────────────────────────────────┐
│  ← Kelola Kategori             │  ← TopBar
├─────────────────────────────────┤
│                                 │
│  [Kategori A]                   │
│  [Kategori B]                   │
│  [Kategori C]                   │  ← List
│  ...                            │
│                                 │
│                            ┌─┐  │
│                            │+│  │  ← FAB
└────────────────────────────└─┘──┘
```

**Tombol (+) di kanan bawah = Tambah Kategori**

---

## 🔐 Akses Berdasarkan Role

### ADMIN:
- ✅ **Dapat** melihat tombol FAB (+) di Daftar Produk
- ✅ **Dapat** menambah produk baru
- ✅ **Dapat** mengedit produk
- ✅ **Dapat** menghapus produk
- ✅ **Dapat** mengelola kategori

### KASIR (Cashier):
- ❌ **Tidak dapat** melihat tombol FAB (+) di Daftar Produk
- ❌ **Tidak dapat** menambah produk
- ❌ **Tidak dapat** mengedit produk
- ❌ **Tidak dapat** menghapus produk
- ❌ **Tidak dapat** mengelola kategori
- ✅ **Hanya dapat** melihat daftar produk dan detailnya

---

## 🐛 Troubleshooting

### ❓ Tidak Melihat Tombol (+) di Daftar Produk?

**Kemungkinan Penyebab:**

1. **Role bukan ADMIN**
   - Solusi: Login dengan user yang memiliki role ADMIN
   - Default admin: username "admin", password "admin123"

2. **Bug pada state isAdmin**
   - Sudah diperbaiki dengan menambahkan `GetCurrentUserUseCase`
   - ViewModel sekarang memeriksa role user saat init

3. **Build belum update**
   - Solusi: Clean build project
   ```bash
   ./gradlew.bat clean build
   ```

### ❓ Tombol (+) Ada Tapi Tidak Berfungsi?

**Cek:**
1. Pastikan tidak ada error di console
2. Pastikan navigation sudah terkonfigurasi
3. Pastikan ProductFormScreen sudah di-register di NavGraph

---

## 📝 Validasi Form

### Produk:
- ✅ Nama: Wajib diisi, tidak boleh kosong
- ✅ Harga: Wajib diisi, harus angka > 0
- ✅ Stok: Wajib diisi, harus angka >= 0
- ✅ Field lain: Opsional

### Kategori:
- ✅ Nama: Wajib diisi, tidak boleh kosong
- ✅ Field lain: Opsional

---

## 🎨 Visual Guide

### Tombol FAB (Floating Action Button)

**Karakteristik:**
- 🔵 Warna: Primary color (biasanya biru)
- ⭕ Bentuk: Bulat
- ➕ Ikon: Plus (+)
- 📍 Posisi: Kanan bawah (floating)
- 🎯 Ukuran: 56dp diameter
- ✨ Efek: Shadow/elevation
- 👆 Action: Click untuk tambah

**Contoh Kode:**
```kotlin
FloatingActionButton(
    onClick = { /* Navigate to Add Form */ }
) {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Tambah Produk"
    )
}
```

---

## 🔄 Flow Lengkap

### Flow Tambah Produk:
```
Home 
  → Menu Produk 
    → Daftar Produk 
      → FAB (+) [Admin Only]
        → Form Produk Baru
          → Isi Data
            → Klik Simpan
              → Validasi
                → Berhasil ✅
                  → Kembali ke Daftar Produk
                    → Produk Baru Muncul di List
```

### Flow Tambah Kategori:
```
Daftar Produk 
  → Ikon Category 
    → Kelola Kategori
      → FAB (+)
        → Form Kategori Baru
          → Isi Data
            → Klik Simpan
              → Berhasil ✅
                → Kategori Baru Muncul di List
```

---

## ✅ Checklist User

Sebelum menambah produk, pastikan:

- [ ] Login sebagai ADMIN
- [ ] Kategori sudah dibuat (opsional tapi direkomendasikan)
- [ ] Data produk sudah siap (nama, harga, stok)
- [ ] Barcode/SKU sudah ada (jika perlu)
- [ ] Gambar produk sudah tersedia (opsional)

---

## 📞 Kontak Support

Jika masih ada masalah:
1. Cek dokumentasi di folder `/docs`
2. Lihat error log di Logcat
3. Rebuild project: `./gradlew.bat clean build`
4. Restart aplikasi

---

**Updated:** November 14, 2025  
**Status:** ✅ FAB Implemented & Working  
**User Role Required:** ADMIN

