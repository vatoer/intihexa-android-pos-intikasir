### 🟢 Panduan Prompt AI: Palet Warna Aplikasi PoS (Tema: Fresh & Reliable)

**Peran AI:** Anda adalah AI asisten pengembangan yang bertugas menerapkan tema Material 3 kustom untuk aplikasi PoS (Point of Sale) Android.

**Tujuan:** Menerapkan palet warna berikut secara konsisten. Tema ini menggunakan **Hijau** sebagai warna primer untuk melambangkan **kesegaran (F&B)**, **keuangan**, dan **keberhasilan transaksi**.

**Patuhi pemetaan token dan aturan penerapan komponen dengan ketat.**

---

#### 1. Token Palet Warna Utama (Material 3)

Gunakan nilai Hex ini untuk menghasilkan skema warna (light mode).

| Token M3 | Kode Hex | Tujuan Penggunaan Utama |
| :--- | :--- | :--- |
| `Primary` | **`#006D3B`** | Tombol aksi utama (Bayar, Simpan), FAB, Navigasi aktif. |
| `OnPrimary` | **`#FFFFFF`** | Teks & Ikon di atas `Primary`. |
| `PrimaryContainer` | **`#9AF6B5`** | Latar belakang *highlight* ringan (misal: item terpilih). |
| `OnPrimaryContainer` | **`#00210E`** | Teks & Ikon di atas `PrimaryContainer`. |
| `Secondary` | **`#4F6354`** | Tombol aksi sekunder (Filter, Cetak), Chip filter. |
| `OnSecondary` | **`#FFFFFF`** | Teks & Ikon di atas `Secondary`. |
| `SecondaryContainer`| **`#D2E8D6`** | Latar belakang *highlight* sekunder. |
| `OnSecondaryContainer`| **`#0C1F14`** | Teks & Ikon di atas `SecondaryContainer`. |
| `Tertiary` | **`#3E6373`** | Aksen, *chip* Kategori, info non-kritis (misal: status member). |
| `OnTertiary` | **`#FFFFFF`** | Teks & Ikon di atas `Tertiary`. |
| `TertiaryContainer`| **`#C1E8FB`** | Latar belakang *highlight* aksen. |
| `OnTertiaryContainer`| **`#001F29`** | Teks & Ikon di atas `TertiaryContainer`. |
| `Background` | **`#FBFDF7`** | Latar belakang global aplikasi. |
| `OnBackground` | **`#1A1C1A`** | Teks isi/paragraf utama. |
| `Surface` | **`#FBFDF7`** | Latar belakang Komponen: Card, Sheet, Menu, TopAppBar. |
| `OnSurface` | **`#1A1C1A`** | Teks di atas Komponen (misal: Judul Kartu). |
| `SurfaceVariant` | **`#DDE5DA`** | Latar belakang komponen netral (misal: TextField non-aktif). |
| `OnSurfaceVariant` | **`#414942`** | Teks & ikon inaktif, *hint text*, *divider* (garis pemisah). |
| `Outline` | **`#717972`** | Garis batas untuk `OutlinedButton`, `TextField`. |

#### 2. Token Warna Semantik (Status)

Warna ini krusial untuk PoS dan harus digunakan di luar palet standar M3.

| Status | Kode Hex | Tujuan Penggunaan Utama |
| :--- | :--- | :--- |
| **Success** | **`#34A853`** | Teks/Chip "Lunas", "Berhasil Dicetak", "Online". |
| **Warning** | **`#FBBC04`** | Teks/Chip "Stok Menipis", "Diskon Segera Berakhir". |
| **Error** | **`#BA1A1A`** | (Gunakan token M3 `Error` standar) |
| `Error` | **`#BA1A1A`** | Teks/Chip "Stok Habis", "Transaksi Gagal", tombol "Hapus". |
| `OnError` | **`#FFFFFF`** | Teks & Ikon di atas `Error`. |
| `ErrorContainer` | **`#FFDAD6`** | Latar *highlight* ringan untuk pesan error. |
| `OnErrorContainer`| **`#410002`** | Teks di atas `ErrorContainer`. |

---

#### 3. Aturan Penerapan Komponen (Wajib Diikuti)

Berikut cara memetakan token di atas ke komponen spesifik:

1.  **TopAppBar (Bilah Atas):**
    * `background-color:` **`Surface`**
    * `title-text-color:` **`OnSurface`**
    * `navigation-icon-color:` **`OnSurface`**
    * `action-icon-color:` **`OnSurfaceVariant`** (atau `OnSurface`)
    * **Aturan Kritis:** JANGAN gunakan `Primary` sebagai warna latar `TopAppBar`.

2.  **Tombol Aksi Utama (cth: "Bayar", "Simpan"):**
    * Gunakan `FilledButton`.
    * `background-color:` **`Primary`**
    * `text/icon-color:` **`OnPrimary`**

3.  **Tombol Aksi Sekunder (cth: "Batal", "Cetak Resi", "Filter"):**
    * Gunakan `OutlinedButton` atau `TextButton`.
    * `text/icon-color:` **`Primary`** (jika `TextButton`)
    * `outline-color:` **`Outline`** (jika `OutlinedButton`)
    * `text-color:` **`Primary`** (jika `OutlinedButton`)

4.  **Tombol Aksi Berbahaya (cth: "Hapus Item", "Batalkan Pesanan"):**
    * Gunakan `FilledButton` atau `TextButton`.
    * `background-color:` **`Error`** (jika `FilledButton`)
    * `text/icon-color:` **`OnError`** (jika `FilledButton`)
    * `text/icon-color:` **`Error`** (jika `TextButton`)

5.  **Floating Action Button (FAB) (cth: "+ Pesanan Baru"):**
    * `background-color:` **`Primary`**
    * `icon-color:` **`OnPrimary`**

6.  **Navigasi (Bottom Navigation Bar / Navigation Rail):**
    * `background-color:` **`Surface`**
    * `active-indicator-color:` **`SecondaryContainer`**
    * `active-icon-color:` **`OnSecondaryContainer`**
    * `active-text-color:` **`OnSecondaryContainer`**
    * `inactive-icon-color:` **`OnSurfaceVariant`**
    * `inactive-text-color:` **`OnSurfaceVariant`**

7.  **Teks dan Ikon:**
    * **Teks Utama (Nama Produk, Harga):** Gunakan **`OnSurface`** atau **`OnBackground`**.
    * **Teks Sekunder (Deskripsi, Hint):** Gunakan **`OnSurfaceVariant`**.
    * **Ikon Dekoratif (dalam list):** Gunakan **`OnSurfaceVariant`** atau **`Secondary`**.
    * **Ikon Kategori (Chip):** Gunakan **`Tertiary`** atau **`OnTertiaryContainer`** (dengan latar `TertiaryContainer`).