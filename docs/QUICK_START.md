# Quick Start Guide - Inti Kasir

## 🚀 Cara Menjalankan Aplikasi

### Prerequisites
1. **Java JDK 11 atau lebih baru**
   - Download: https://www.oracle.com/java/technologies/downloads/
   - Atau gunakan: `brew install openjdk@11` (macOS)

2. **Android Studio Hedgehog (2023.1.1) atau lebih baru**
   - Download: https://developer.android.com/studio

3. **Android SDK API 29+**
   - Install via Android Studio SDK Manager

---

## 📥 Setup Project

### 1. Buka Project
```bash
# Navigate ke folder project
cd /Volumes/X9/intihexa/Android/intihexa-android-pos-intikasir

# Buka di Android Studio
open -a "Android Studio" .
```

### 2. Gradle Sync
- Android Studio akan otomatis detect dan prompt untuk sync
- Klik **"Sync Now"**
- Tunggu sampai selesai download dependencies

### 3. Firebase Setup (Optional untuk tahap awal)
Untuk testing basic UI, Firebase tidak diperlukan. Namun jika ingin setup:

```bash
# Replace google-services.json dengan file dari Firebase Console
# Download dari: Firebase Console > Project Settings > Your apps
cp /path/to/downloaded/google-services.json app/google-services.json
```

---

## ▶️ Run Application

### Menggunakan Emulator
1. **Buat/Start Emulator:**
   - Tools → Device Manager
   - Create Device atau pilih yang sudah ada
   - Minimum: API 29 (Android 10)
   - Recommended: API 34 (Android 14)

2. **Run App:**
   - Klik tombol ▶️ **Run** (atau Shift+F10)
   - Pilih emulator
   - Tunggu build selesai

### Menggunakan Physical Device
1. **Enable Developer Options:**
   - Settings → About Phone
   - Tap "Build Number" 7x
   - Back → Developer Options
   - Enable "USB Debugging"

2. **Connect via USB:**
   - Hubungkan device ke komputer
   - Allow USB debugging prompt

3. **Run App:**
   - Klik ▶️ Run
   - Pilih your device

---

## 🎨 Apa yang Akan Terlihat

Saat ini aplikasi akan menampilkan:

### POS Screen (Main Screen)
```
┌─────────────────────────────────────────────────────────┐
│  Inti Kasir - POS                              ☰        │
├────────────────────────────────┬────────────────────────┤
│  🔍 Cari produk...             │  Keranjang (0 item)    │
├────────────────────────────────┤                        │
│  [ Semua ] [ 🍔 Makanan ]      │                        │
│  [ 🥤 Minuman ] [ 🍿 Snack ]   │  (Empty state)         │
│                                │                        │
│  ┌──────┐  ┌──────┐  ┌──────┐ │                        │
│  │ Nasi │  │ Mie  │  │ Es   │ │  ──────────────────    │
│  │Goreng│  │Goreng│  │ Teh  │ │  Subtotal   Rp 0       │
│  │      │  │      │  │      │ │  Pajak(10%) Rp 0       │
│  │15,000│  │12,000│  │3,000 │ │  ──────────────────    │
│  │      │  │      │  │      │ │  TOTAL      Rp 0       │
│  └──────┘  └──────┘  └──────┘ │                        │
│  ┌──────┐  ┌──────┐           │  ┌──────────────────┐  │
│  │ Kopi │  │Keripik│           │  │   🛒 BAYAR      │  │
│  │      │  │      │            │  └──────────────────┘  │
│  │5,000 │  │8,000 │            │                        │
│  └──────┘  └──────┘            │                        │
└────────────────────────────────┴────────────────────────┘
```

### Fitur yang Berfungsi:
- ✅ Klik produk untuk tambah ke cart
- ✅ Quantity controls (+/-)
- ✅ Remove item dari cart
- ✅ Real-time calculation (subtotal, tax, total)
- ✅ Search bar (UI only, belum functional)
- ✅ Category filter (UI only)

### Fitur yang Belum Berfungsi:
- ❌ Checkout/payment (button disabled)
- ❌ Data persistence (masih dummy data)
- ❌ Search functionality
- ❌ Category filtering
- ❌ Login system

---

## 🔧 Development Mode

### Lihat Database (Room Inspector)
1. Run app di emulator/device
2. Tools → App Inspection
3. Pilih tab "Database Inspector"
4. Explore tables (User, Product, Transaction, dll)

### Lihat Logcat
1. View → Tool Windows → Logcat
2. Filter by: `id.stargan.intikasir`

### Debug Mode
1. Set breakpoint di code
2. Run → Debug 'app' (atau Shift+F9)
3. Step through code

---

## 📝 Testing Basic Features

### Test 1: Add Product to Cart
1. Klik salah satu product card
2. Product akan muncul di cart panel (kanan)
3. Klik lagi untuk increase quantity
4. Lihat subtotal & total update otomatis

### Test 2: Modify Cart
1. Klik tombol `-` untuk kurangi quantity
2. Quantity akan berkurang
3. Klik tombol `🗑️` untuk hapus item
4. Item akan hilang dari cart

### Test 3: Tax Calculation
1. Add beberapa products ke cart
2. Perhatikan:
   - Subtotal = sum of all items
   - Tax = 10% dari subtotal
   - Total = subtotal + tax

---

## 🐛 Troubleshooting

### Error: "Unable to locate a Java Runtime"
**Solution:**
```bash
# Install Java JDK
brew install openjdk@11

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
```

### Error: "SDK location not found"
**Solution:**
1. Buka Android Studio
2. File → Project Structure → SDK Location
3. Set Android SDK location (biasanya: ~/Library/Android/sdk)

### Error: "Unresolved reference"
**Solution:**
1. File → Invalidate Caches → Invalidate and Restart
2. Atau: Build → Clean Project
3. Lalu: Build → Rebuild Project

### Error: "Failed to resolve: com.google.firebase"
**Solution:**
```kotlin
// Sementara comment plugins Firebase di build.gradle.kts
// alias(libs.plugins.google.services) apply false

// Dan di app/build.gradle.kts
// alias(libs.plugins.google.services)
```

### Gradle Sync Error
**Solution:**
1. Tools → SDK Manager → SDK Tools
2. Install:
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
   - Android SDK Tools
3. Sync again

---

## 🎯 Next Steps for Development

### Immediate Next Steps:
1. **Implement Repositories**
   ```kotlin
   // Create ProductRepository
   interface ProductRepository {
       fun getAllProducts(): Flow<List<Product>>
       suspend fun getProductById(id: String): Product?
   }
   ```

2. **Create PosViewModel**
   ```kotlin
   @HiltViewModel
   class PosViewModel @Inject constructor(
       private val productRepository: ProductRepository
   ) : ViewModel() {
       // Implement business logic
   }
   ```

3. **Connect ViewModel to UI**
   ```kotlin
   @Composable
   fun PosScreen(viewModel: PosViewModel = hiltViewModel()) {
       val uiState by viewModel.uiState.collectAsState()
       // Use real data
   }
   ```

### Files to Create Next:
```
domain/repository/
├── ProductRepository.kt
├── CategoryRepository.kt
└── TransactionRepository.kt

data/repository/
├── ProductRepositoryImpl.kt
├── CategoryRepositoryImpl.kt
└── TransactionRepositoryImpl.kt

data/mapper/
├── ProductMapper.kt
├── CategoryMapper.kt
└── TransactionMapper.kt

ui/screen/pos/
└── PosViewModel.kt
```

---

## 📚 Resources

### Documentation
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture
- [PAYMENT_FLOW.md](PAYMENT_FLOW.md) - Payment flow logic
- [FIREBASE_SETUP.md](FIREBASE_SETUP.md) - Firebase setup guide
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - What's implemented

### External Resources
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

## 💡 Tips

1. **Hot Reload**: Compose preview updates automatically
2. **Live Edit**: Enable in Android Studio for instant UI updates
3. **Compose Preview**: Use `@Preview` annotation untuk preview composables
4. **Database Inspector**: Best untuk debug Room database
5. **Layout Inspector**: Tools → Layout Inspector untuk inspect UI hierarchy

---

## ✅ Checklist Setup

- [ ] Java JDK installed
- [ ] Android Studio installed
- [ ] Project opened in Android Studio
- [ ] Gradle sync successful
- [ ] Emulator/device ready
- [ ] App running successfully
- [ ] Can see POS screen
- [ ] Can add products to cart
- [ ] Calculations working

---

**Happy Coding! 🚀**

Jika ada pertanyaan atau masalah, cek dokumentasi atau buat issue.

