**Peran (Role):**
Anda adalah seorang Arsitek Sistem (System Architect) dan Pengembang Android Senior yang ahli dalam membuat aplikasi Point of Sale (POS) yang tangguh dan skalabel.

**Proyek (Project):**
Kita akan merancang dan membangun aplikasi POS Android *native* bernama **"Inti Kasir"**.

**Target Audiens:**
Usaha Kecil Menengah (UKM) di Indonesia (misalnya: warung, kafe kecil, toko retail) yang membutuhkan sistem kasir yang andal, modern, namun tetap mudah digunakan.

**Tumpukan Teknologi (Tech Stack):**
* **UI:** Jetpack Compose
* **Desain:** Material 3 (M3)
* **Database Lokal:** Room (untuk fungsionalitas *offline-first*)
* **Backend & Sinkronisasi:** (Mohon berikan rekomendasi, misal: Firebase Realtime Database/Firestore atau Supabase)

---

### RINCIAN FITUR INTI (FEATURE BREAKDOWN)

Harap berikan arsitektur dan model data untuk fitur-fitur berikut:

#### 1. Instalasi & Aktivasi (Side-Loading)
Ini adalah fitur unik. Aplikasi tidak akan didistribusikan melalui Google Play Store, tetapi melalui file APK (side-loading).
* **Alur:**
    1.  Pengguna menginstal APK.
    2.  Saat aplikasi dibuka pertama kali, aplikasi **terkunci** dan menampilkan **Layar Aktivasi**.
    3.  Pengguna harus memasukkan **Kode Aktivasi** unik (yang mereka beli dari kami).
    4.  Aplikasi memvalidasi kode ini ke sebuah *backend* (server lisensi).
    5.  Jika valid, aplikasi "terbuka" (unlocked) dan terikat pada perangkat tersebut.
* **Tugas:** Jelaskan cara terbaik merancang sistem lisensi/aktivasi sederhana ini.

#### 2. Manajemen Pengguna (Fitur Pengguna)
Aplikasi harus mendukung banyak pengguna dalam satu perangkat.
* **Peran (Roles):**
    1.  **Admin (Pemilik):** Akses penuh. Bisa melihat laporan, mengatur produk, mengelola pengguna, dan mengakses Pengaturan.
    2.  **Kasir (Staf):** Akses terbatas. Hanya bisa melakukan Transaksi.
* **Fitur:**
    * Login berbasis PIN 4 digit untuk perpindahan cepat antar pengguna.
    * Admin bisa menambah/mengedit/menghapus akun Kasir.

#### 3. Manajemen Produk (Fitur Produk)
* **Fitur:**
    * CRUD (Create, Read, Update, Delete) untuk produk.
    * **Kategori Produk:** (misal: Makanan, Minuman).
    * **Manajemen Inventori (Stok):**
        * Input stok awal.
        * Stok otomatis berkurang saat terjadi transaksi.
        * Opsi untuk mengaktifkan/menonaktifkan pelacakan stok per produk.

#### 4. Transaksi (Fitur Transaksi)
Ini adalah layar utama aplikasi (layar kasir).
* **Fitur:**
    * Tampilan *grid* (kisi-kisi) produk yang bisa dipilih.
    * Filter produk berdasarkan Kategori.
    * Fungsi pencarian produk.
    * **Keranjang Belanja (Cart):** Daftar produk yang dipilih, bisa mengubah jumlah (quantity), dan menghapus item.
    * **Pembayaran:**
        * Pilihan metode (Tunai, QRIS, Kartu).
        * Input jumlah uang tunai, kalkulator otomatis untuk kembalian.
    * **Cetak Struk (Receipt):** Koneksi ke printer Bluetooth termal untuk mencetak struk.
    * Simpan transaksi (Mencatat semua detail pesanan).

#### 5. Laporan (Fitur Laporan)
* **Fitur:**
    * Laporan Penjualan Harian, Mingguan, Bulanan.
    * Menampilkan **Total Omzet** dan **Total Transaksi**.
    * **Laporan Produk Terlaris** (Top-Selling Items).
    * Filter laporan berdasarkan rentang tanggal.

#### 6. Pengaturan (Fitur Pengaturan)
* **Fitur (Hanya Admin):**
    * **Pengaturan Toko:** Nama Toko, Alamat, No. Telepon (untuk ditampilkan di struk).
    * **Pengaturan Printer:** Pindai (scan) dan hubungkan ke printer Bluetooth.
    * **Pengaturan Pajak/Layanan:** (misal: PPN 10%, Servis 5%).
    * Manajemen Pengguna (CRUD untuk Kasir).
    * Tombol "Sinkronisasi Data" manual ke *cloud*.

### 7. Modularity (Modulitas)
    * Keseluruhan kode program ditulis secara modular untuk mempermudah reusability
---

### TUGAS ANDA (YOUR TASKS)

Berdasarkan semua informasi di atas, tolong berikan saya:

1.  **Rekomendasi Arsitektur:**
    * Apa arsitektur *Android* terbaik (misal: MVVM, MVI)?
    * Bagaimana strategi *offline-first* menggunakan **Room** dan sinkronisasi ke **Firebase/Supabase**?
    * Bagaimana desain sistem **Aktivasi Kode** (backend/database apa yang dipakai untuk menyimpan dan memvalidasi kode)?

2.  **Model Data (Skema Database):**
    * Buatkan skema tabel/model data (dalam format Kotlin data class) untuk **Room Database**. Saya butuh model untuk:
        * `User (Pengguna)`
        * `Product (Produk)`
        * `Category (Kategori)`
        * `Transaction (Transaksi)`
        * `TransactionItem (Detail item per transaksi)`

3.  **Contoh Kode (Code Example):**
    * Berikan saya contoh kode **Jetpack Compose** dengan **Material 3** untuk membuat **Layar Transaksi (POS)** utama.
    * Harus mencakup 2 bagian: Grid Produk (di kiri) dan Keranjang Belanja/Cart (di kanan).

4.  **Alur (Flow):**
    * Jelaskan alur *logic* saat tombol "Bayar" ditekan (Mulai dari validasi, simpan ke database Room, kurangi stok, hingga cetak struk).