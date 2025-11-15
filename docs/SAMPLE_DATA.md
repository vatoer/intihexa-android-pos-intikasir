# Sample Data Initialization

## Overview
Aplikasi IntiKasir secara otomatis membuat sample data saat pertama kali diinstal untuk memudahkan testing dan demonstrasi fitur.

## Sample Data yang Dibuat

### 5 Kategori

1. **Makanan** 🍔
   - Warna: #FF6B6B (Merah)
   - Deskripsi: Produk makanan dan cemilan

2. **Minuman** 🥤
   - Warna: #4ECDC4 (Cyan)
   - Deskripsi: Minuman segar dan kemasan

3. **Elektronik** 🔌
   - Warna: #45B7D1 (Biru)
   - Deskripsi: Peralatan elektronik

4. **Alat Tulis** ✏️
   - Warna: #96CEB4 (Hijau)
   - Deskripsi: Perlengkapan tulis dan kantor

5. **Kebutuhan Rumah** 🏠
   - Warna: #FFEAA7 (Kuning)
   - Deskripsi: Produk kebutuhan sehari-hari

### 10 Produk

#### Kategori Makanan
1. **Nasi Goreng**
   - SKU: MKN-001
   - Harga: Rp 15.000
   - Modal: Rp 10.000
   - Stok: 50
   - Min Stok: 10

2. **Mie Ayam**
   - SKU: MKN-002
   - Harga: Rp 12.000
   - Modal: Rp 8.000
   - Stok: 40
   - Min Stok: 10

#### Kategori Minuman
3. **Es Teh Manis**
   - SKU: MNM-001
   - Harga: Rp 5.000
   - Modal: Rp 2.000
   - Stok: 100
   - Min Stok: 20

4. **Kopi Susu**
   - SKU: MNM-002
   - Harga: Rp 8.000
   - Modal: Rp 4.000
   - Stok: 80
   - Min Stok: 15

#### Kategori Elektronik
5. **Kabel USB Type-C**
   - SKU: ELK-001
   - Harga: Rp 25.000
   - Modal: Rp 15.000
   - Stok: 30
   - Min Stok: 5

6. **Earphone**
   - SKU: ELK-002
   - Harga: Rp 35.000
   - Modal: Rp 20.000
   - Stok: 25
   - Min Stok: 5

#### Kategori Alat Tulis
7. **Pulpen**
   - SKU: ATK-001
   - Harga: Rp 3.000
   - Modal: Rp 1.500
   - Stok: 100
   - Min Stok: 20

8. **Buku Tulis**
   - SKU: ATK-002
   - Harga: Rp 5.000
   - Modal: Rp 3.000
   - Stok: 60
   - Min Stok: 15

#### Kategori Kebutuhan Rumah
9. **Sabun Cuci Piring**
   - SKU: RMH-001
   - Harga: Rp 12.000
   - Modal: Rp 8.000
   - Stok: 35
   - Min Stok: 10

10. **Tisu Wajah**
    - SKU: RMH-002
    - Harga: Rp 8.000
    - Modal: Rp 5.000
    - Stok: 50
    - Min Stok: 10

## Implementasi Teknis

### DatabaseCallback
Sample data dibuat melalui `DatabaseCallback` yang dipanggil saat database pertama kali dibuat:

```kotlin
class DatabaseCallback(
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch(Dispatchers.IO) {
            populateSampleData(db)
        }
    }
}
```

### Integrasi dengan Room
Callback ditambahkan saat membuat Room database:

```kotlin
Room.databaseBuilder(context, IntiKasirDatabase::class.java, DATABASE_NAME)
    .fallbackToDestructiveMigration(dropAllTables = true)
    .addCallback(DatabaseCallback(CoroutineScope(SupervisorJob())))
    .build()
```

## Kapan Sample Data Dibuat?

Sample data hanya dibuat **sekali** saat:
- Aplikasi pertama kali diinstal
- Database dibuat untuk pertama kalinya
- Setelah clear data aplikasi

## Testing
Untuk menguji sample data:
1. Uninstall aplikasi atau clear app data
2. Install/jalankan aplikasi kembali
3. Buka menu "Daftar Produk"
4. Sample data akan otomatis muncul

## Catatan
- Data sample menggunakan UUID untuk ID yang unique
- Semua produk memiliki stok yang cukup untuk testing transaksi
- Kategori memiliki warna berbeda untuk visualisasi yang lebih baik
- Harga modal (cost) diset lebih rendah dari harga jual untuk simulasi profit

