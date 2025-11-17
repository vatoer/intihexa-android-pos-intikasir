# Inti Kasir - Aplikasi POS Android

**Inti Kasir** adalah aplikasi Point of Sale (POS) Android native yang dirancang khusus untuk Usaha Kecil Menengah (UKM) di Indonesia. Aplikasi ini menawarkan sistem kasir yang andal, modern, dan mudah digunakan dengan fitur offline-first.

---

## 🎨 Design System - "Fresh Commerce"

Aplikasi menggunakan design system profesional yang dioptimalkan untuk penggunaan jangka panjang:

- **🎯 Primary (Teal)**: #00897B - Professional, Fresh, Trustworthy
- **🔥 Secondary (Orange)**: #FF6F00 - Energetic, Urgency, Appetite  
- **💎 Tertiary (Purple)**: #5E35B1 - Premium, Analytics
- **✅ Success (Green)**: #388E3C - Completed transactions
- **⚠️ Warning (Amber)**: #FF8F00 - Low stock alerts
- **❌ Error (Red)**: #D32F2F - Form errors

**Karakteristik**:
- ✅ WCAG AAA compliant (accessibility)
- ✅ Eye-friendly untuk penggunaan 8+ jam
- ✅ Material 3 Design Language
- ✅ Dark mode support

📖 **Color Guide**: `/docs/ai-color-guidance.md` & `/docs/COLOR_QUICK_GUIDE.md`

---

## 🎯 Fitur Utama

### ✅ Sudah Diimplementasikan

#### 1. **Arsitektur & Database**
- ✅ Clean Architecture + MVVM
- ✅ Room Database untuk offline-first
- ✅ Hilt Dependency Injection
- ✅ Modular structure untuk reusability

#### 2. **Data Layer**
- ✅ Entity Models (User, Product, Category, Transaction, TransactionItem, StoreSettings)
- ✅ DAO (Data Access Objects) untuk semua entity
- ✅ Database schema dengan relasi lengkap
- ✅ Support untuk soft delete & sync tracking

#### 3. **Domain Layer**
- ✅ Domain models terpisah dari entity
- ✅ Enums untuk User Role, Payment Method, Transaction Status
- ✅ CartItem model untuk shopping cart

#### 4. **UI/Presentation Layer**
- ✅ POS Screen dengan Material 3 Design
- ✅ Product Grid dengan kategori filter
- ✅ Shopping Cart dengan quantity controls
- ✅ Real-time subtotal & tax calculation
- ✅ Responsive 2-panel layout (Product Grid + Cart)

### 🚧 Dalam Pengembangan

- [ ] Sistem Aktivasi & Lisensi
- [ ] Manajemen Pengguna (Login dengan PIN)
- [ ] CRUD Produk & Kategori
- [ ] Payment Dialog & Checkout Flow
- [ ] Integration dengan Bluetooth Printer
- [ ] Laporan Penjualan
- [ ] Pengaturan Toko
- [ ] Firebase Sync

---

## 🏗️ Arsitektur

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Jetpack Compose UI + ViewModels)      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Domain Layer                   │
│  (Use Cases, Domain Models, Repos)      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│           Data Layer                    │
│  (Room DB, Firebase, DataStore, API)    │
└─────────────────────────────────────────┘
```

**Lihat detail:** [ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 🛠️ Tech Stack

| Kategori | Teknologi | Version |
|----------|-----------|---------|
| **Language** | Kotlin | 2.0.21 |
| **UI Framework** | Jetpack Compose | BOM 2024.11.00 |
| **Design System** | Material 3 | Latest |
| **Architecture** | Clean Architecture + MVVM | - |
| **Database** | Room | 2.6.1 |
| **Cloud Sync** | Firebase Firestore | 33.7.0 |
| **Dependency Injection** | **Hilt 2.52** ✅ | **Google's Official DI** |
| **Async** | Kotlin Coroutines + Flow | 1.9.0 |
| **Navigation** | Navigation Compose | 2.8.5 |
| **Network** | Retrofit + OkHttp | 2.11.0 |
| **Preferences** | DataStore | 1.1.1 |

> **Note:** Hilt is the **officially recommended** dependency injection framework by Google for Android development as of 2025. It is **actively maintained** and **NOT deprecated**.

---

## 📦 Database Schema

### Entities

1. **User** - Admin dan Kasir
2. **Category** - Kategori produk
3. **Product** - Data produk dengan stok
4. **Transaction** - Header transaksi
5. **TransactionItem** - Detail item per transaksi
6. **StoreSettings** - Pengaturan toko (single row)

### Relasi

```
User ─────┐
          │
          ├──> Transaction ──> TransactionItem ──> Product
          │                                            │
          │                                            │
          └────────────────────────────────────────────┘
                                                       │
                                                   Category
```

---

## 🚀 Cara Menjalankan Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) atau lebih baru
- JDK 11 atau lebih baru
- Android SDK API Level 29 atau lebih tinggi

### Setup

1. **Clone repository**
```bash
git clone <repository-url>
cd intihexa-android-pos-intikasir
```

2. **Buka project di Android Studio**

3. **Tambahkan google-services.json**
   - Download dari Firebase Console
   - Letakkan di folder `app/`

4. **Sync Gradle**
   - Klik "Sync Project with Gradle Files"

5. **Run aplikasi**
   - Pilih device/emulator
   - Klik Run ▶️

---

## 📱 Screenshots

### POS Screen
![POS Screen](docs/screenshots/pos-screen.png)
- Grid produk dengan kategori filter
- Keranjang belanja real-time
- Kalkulasi otomatis subtotal, pajak, dan total

---

## 🔄 Offline-First Strategy

### Prinsip
1. **Single Source of Truth**: Room Database
2. **Sync on Demand**: User trigger manual sync
3. **Conflict Resolution**: Last-write-wins

### Flow
```
User Action → Room DB (Immediate) → Firebase (Background Sync)
```

**Semua operasi CRUD dilakukan ke Room terlebih dahulu**, kemudian di-sync ke Firebase saat:
- User klik tombol "Sync"
- Background worker (periodic)
- Connectivity restored

---

## 🔐 Sistem Aktivasi

Aplikasi menggunakan sistem aktivasi berbasis kode lisensi:

### Flow Aktivasi
1. User install APK (side-loading)
2. Aplikasi terkunci, tampilkan layar aktivasi
3. User input kode aktivasi unik
4. Validasi ke backend (Firebase/API)
5. Bind kode ke device ID
6. Unlock aplikasi

### Backend
- **Firebase Functions** untuk validasi
- **Firestore** untuk menyimpan data lisensi
- Alternative: REST API dengan server sendiri

---

## 💳 Payment Flow

### Alur Pembayaran
```
Click "BAYAR" → Validasi → Payment Dialog → Proses Payment
   ↓
Save Transaction → Save Items → Update Stock
   ↓
Print Receipt → Success → Clear Cart
```

### Supported Payment Methods
- 💵 Tunai (Cash)
- 📱 QRIS
- 💳 Kartu Debit/Kredit
- 🏦 Transfer Bank

**Lihat detail lengkap:** [PAYMENT_FLOW.md](docs/PAYMENT_FLOW.md)

---

## 🖨️ Printer Integration

### Supported Printers
- Thermal Bluetooth Printers (ESC/POS Protocol)
- Contoh: Epson TM-T82, Zjiang, iMin

### Receipt Format
```
================================
        NAMA TOKO
     Alamat Toko
     Telp: 0812345678
================================
No: INV-20251111-0001
Tanggal: 11/11/2025 14:30
Kasir: John Doe
--------------------------------
Nasi Goreng
  1 x Rp 15,000      Rp 15,000
Es Teh
  2 x Rp 3,000       Rp 6,000
--------------------------------
Subtotal           Rp 21,000
PPN (10%)          Rp 2,100
--------------------------------
TOTAL              Rp 23,100
Tunai              Rp 30,000
Kembalian          Rp 6,900
================================
      Terima Kasih
================================
```

---

## 📊 Laporan

### Jenis Laporan
1. **Laporan Harian** - Penjualan hari ini
2. **Laporan Mingguan** - 7 hari terakhir
3. **Laporan Bulanan** - Bulan berjalan
4. **Custom Range** - Pilih tanggal sendiri

### Metrik
- Total Omzet
- Total Transaksi
- Produk Terlaris
- Metode Pembayaran
- Performa Kasir

---

## 👥 Manajemen Pengguna

### User Roles

#### 1. **Admin (Pemilik)**
- ✅ Akses penuh ke semua fitur
- ✅ Lihat laporan
- ✅ Kelola produk & kategori
- ✅ Kelola pengguna
- ✅ Akses pengaturan

#### 2. **Kasir (Staff)**
- ✅ Akses POS (transaksi)
- ❌ Tidak bisa lihat laporan
- ❌ Tidak bisa kelola produk
- ❌ Tidak bisa kelola pengguna

### Login
- **PIN 4 digit** untuk perpindahan cepat
- Auto-logout setelah idle (opsional)

---

## ⚙️ Pengaturan

### Store Settings
- Nama toko
- Alamat
- Nomor telepon
- Logo (untuk struk)

### Tax & Service
- PPN (Pajak Pertambahan Nilai)
- Service charge
- Customizable percentage

### Printer
- Scan & connect Bluetooth printer
- Test print
- Auto-print on checkout

---

## 📝 Development Roadmap

### Phase 1: Core Features ✅
- [x] Setup project & dependencies
- [x] Database schema & entities
- [x] Basic POS UI

### Phase 2: Business Logic 🚧
- [ ] Authentication & authorization
- [ ] Product management
- [ ] Transaction processing
- [ ] Stock management

### Phase 3: Advanced Features
- [ ] Reports & analytics
- [ ] Cloud sync
- [ ] Printer integration
- [ ] License activation

### Phase 4: Polish
- [ ] Error handling
- [ ] Loading states
- [ ] Animations
- [ ] Testing

---

## 🧪 Testing

### Test Coverage
- Unit Tests (ViewModels, Use Cases)
- Integration Tests (Repository, Database)
- UI Tests (Compose screens)
- E2E Tests (Critical flows)

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

---

## 📄 License

Proprietary - All rights reserved

---

## 👨‍💻 Developer

**Intihexa Team**
- Website: [intihexa.com](https://intihexa.com)
- Email: support@intihexa.com

---

## 📞 Support

Butuh bantuan? Hubungi kami:
- 📧 Email: support@intikasir.com
- 📱 WhatsApp: +62 xxx-xxxx-xxxx
- 📚 Dokumentasi: [docs.intikasir.com](https://docs.intikasir.com)

---

## 🙏 Acknowledgments

- Material 3 Design System
- Android Jetpack Libraries
- Firebase Platform
- Open source community

