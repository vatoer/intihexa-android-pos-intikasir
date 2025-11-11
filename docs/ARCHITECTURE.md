# Arsitektur Aplikasi Inti Kasir

## 1. Arsitektur Aplikasi

### Clean Architecture + MVVM
Aplikasi ini menggunakan **Clean Architecture** dengan pola **MVVM (Model-View-ViewModel)** untuk memisahkan concerns dan meningkatkan testability.

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

### Layer Structure:

#### 1. **Presentation Layer** (`ui/`)
- **Screens**: Composable functions untuk UI
- **ViewModels**: Business logic dan state management
- **Navigation**: Navigasi antar screen
- **Components**: Reusable UI components

#### 2. **Domain Layer** (`domain/`)
- **Models**: Data classes untuk domain
- **Use Cases**: Business logic operations
- **Repository Interfaces**: Abstraksi data sources

#### 3. **Data Layer** (`data/`)
- **Local**: Room Database entities, DAOs
- **Remote**: Firebase Firestore, API services
- **Repository Implementations**: Implementasi repository interfaces
- **DataStore**: Preferences dan settings

---

## 2. Offline-First Strategy

### Strategi:
1. **Single Source of Truth**: Room Database sebagai sumber data utama
2. **Sync on Demand**: User trigger manual sync ke cloud
3. **Conflict Resolution**: Last-write-wins strategy

### Flow:
```
User Action → ViewModel → Use Case → Repository
                                        ↓
                            ┌───────────┴───────────┐
                            │                       │
                         Room DB              Firebase
                     (Offline-First)        (Cloud Sync)
```

### Implementation:
- Semua operasi CRUD dilakukan ke Room terlebih dahulu
- Background sync worker untuk upload data ke Firestore
- Download data dari Firestore saat manual sync
- Timestamp-based conflict resolution

---

## 3. Sistem Aktivasi Kode

### Backend: Firebase Functions + Firestore

#### Database Schema (Firestore):
```
licenses/
  ├── {licenseCode}
  │   ├── code: String (unique)
  │   ├── isActive: Boolean
  │   ├── deviceId: String (null jika belum diaktivasi)
  │   ├── activatedAt: Timestamp
  │   ├── expiresAt: Timestamp (optional, untuk subscription)
  │   └── createdAt: Timestamp
```

#### Flow Aktivasi:
1. User input kode aktivasi
2. App mengambil `device_id` unik (Android ID)
3. Call Firebase Function untuk validasi:
   - Cek apakah kode exist dan `isActive = true`
   - Cek apakah `deviceId` null (belum diaktivasi)
   - Jika valid, bind `deviceId` ke license
4. Simpan status aktivasi di DataStore
5. Unlock aplikasi

#### Alternative: REST API
Jika tidak menggunakan Firebase, bisa menggunakan simple REST API:
- Endpoint: `POST /api/licenses/activate`
- Body: `{code, deviceId}`
- Response: `{success, message, expiresAt}`

---

## 4. Model Data (Room Database)

### Entity Relationships:
```
User ─────┐
          │
          ├──> Transaction ──> TransactionItem ──> Product
          │                                            │
          │                                            │
          └────────────────────────────────────────────┘
                                                       │
                                                   Category
                                                   
StoreSettings (Single Row)
```

### Timestamps & Sync:
Setiap entity memiliki:
- `id`: String (UUID)
- `createdAt`: Long (timestamp)
- `updatedAt`: Long (timestamp)
- `syncedAt`: Long? (null jika belum sync ke cloud)
- `isDeleted`: Boolean (soft delete untuk sync)

---

## 5. Navigation Structure

```
SplashScreen
    │
    ├─> ActivationScreen (jika belum activated)
    │
    └─> LoginScreen
            │
            ├─> POSScreen (Kasir & Admin)
            │
            └─> (Admin Only)
                    ├─> ProductManagementScreen
                    ├─> ReportsScreen
                    ├─> SettingsScreen
                    │       ├─> StoreSettingsScreen
                    │       ├─> UserManagementScreen
                    │       ├─> PrinterSettingsScreen
                    │       └─> TaxSettingsScreen
                    └─> ProfileScreen
```

---

## 6. Dependency Injection (Hilt)

### Why Hilt?
**Hilt 2.52** adalah framework dependency injection yang **officially recommended** oleh Google untuk Android development (2025).

**Advantages:**
- ✅ Compile-time safety (errors caught before runtime)
- ✅ First-class Jetpack Compose support
- ✅ Android lifecycle-aware
- ✅ Less boilerplate than Dagger 2
- ✅ Predefined components for Android
- ✅ Excellent testing support
- ✅ Active maintenance by Google

> **Important:** Hilt is **NOT deprecated**. It remains the official recommendation and is actively maintained. See `DEPENDENCY_INJECTION.md` for details.

### Module Structure:
- **DatabaseModule**: Room Database, DAOs
- **NetworkModule**: Firebase, Retrofit, API services
- **RepositoryModule**: Repository implementations (using @Binds)
- **AppModule**: Application-level dependencies

### Modern Best Practices (2025):
```kotlin
// Use KSP instead of KAPT (30-50% faster)
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("com.google.dagger.hilt.android") version "2.52"
}

// ViewModel injection
@HiltViewModel
class PosViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel()

// Compose integration
@Composable
fun PosScreen(viewModel: PosViewModel = hiltViewModel()) {
    // Automatic injection
}
```

---

## 7. State Management

### ViewModels menggunakan:
- **StateFlow** untuk UI state
- **SharedFlow** untuk one-time events
- **Sealed classes** untuk UI states dan events

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

## 8. Testing Strategy

- **Unit Tests**: ViewModels, Use Cases, Repositories
- **Integration Tests**: Database operations
- **UI Tests**: Jetpack Compose screens
- **End-to-End Tests**: Critical user flows

---

## 9. Security Considerations

1. **License Validation**: Validate pada setiap app start
2. **PIN Security**: Hash PIN dengan BCrypt atau SHA-256
3. **Local Data**: Room Database dengan encryption (SQLCipher)
4. **Cloud Data**: Firebase Security Rules untuk protect data
5. **API Keys**: Gunakan BuildConfig untuk hide keys

---

## 10. Performance Optimization

1. **Lazy Loading**: Load products dengan pagination
2. **Image Caching**: Coil untuk product images
3. **Database Indexing**: Index pada foreign keys dan search fields
4. **Debouncing**: Search input dengan debounce
5. **Background Work**: WorkManager untuk sync operations

---

## Tech Stack Summary

| Layer | Technology | Version | Status |
|-------|-----------|---------|--------|
| UI | Jetpack Compose + Material 3 | 2024.11.00 | ✅ Latest |
| Architecture | Clean Architecture + MVVM | - | ✅ Best Practice |
| DI | **Hilt** | **2.52** | ✅ **Google Official** |
| Database | Room | 2.6.1 | ✅ Stable |
| Cloud Sync | Firebase Firestore | 33.7.0 | ✅ Latest |
| Navigation | Navigation Compose | 2.8.5 | ✅ Stable |
| Async | Kotlin Coroutines + Flow | 1.9.0 | ✅ Stable |
| Network | Retrofit + OkHttp | 2.11.0 | ✅ Latest |
| Preferences | DataStore | 1.1.1 | ✅ Stable |
| Image Loading | Coil (optional) | - | Future |
| Printer | Bluetooth API | - | Native |

> **DI Note:** Hilt is actively maintained by Google and is the recommended DI solution for Android. It is **NOT deprecated**. See [DEPENDENCY_INJECTION.md](DEPENDENCY_INJECTION.md) for detailed explanation.

---

## Project Structure

```
app/src/main/java/id/stargan/intikasir/
├── IntiKasirApplication.kt
├── MainActivity.kt
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── IntiKasirDatabase.kt
│   │   │   └── dao/
│   │   │       ├── UserDao.kt
│   │   │       ├── ProductDao.kt
│   │   │       ├── CategoryDao.kt
│   │   │       ├── TransactionDao.kt
│   │   │       └── StoreSettingsDao.kt
│   │   └── entity/
│   │       ├── UserEntity.kt
│   │       ├── ProductEntity.kt
│   │       ├── CategoryEntity.kt
│   │       ├── TransactionEntity.kt
│   │       ├── TransactionItemEntity.kt
│   │       └── StoreSettingsEntity.kt
│   │
│   ├── remote/
│   │   ├── firebase/
│   │   │   └── FirebaseDataSource.kt
│   │   └── api/
│   │       └── LicenseApiService.kt
│   │
│   ├── repository/
│   │   ├── UserRepositoryImpl.kt
│   │   ├── ProductRepositoryImpl.kt
│   │   ├── TransactionRepositoryImpl.kt
│   │   └── LicenseRepositoryImpl.kt
│   │
│   └── preferences/
│       └── AppPreferences.kt
│
├── domain/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Product.kt
│   │   ├── Category.kt
│   │   ├── Transaction.kt
│   │   ├── TransactionItem.kt
│   │   ├── CartItem.kt
│   │   └── StoreSettings.kt
│   │
│   ├── repository/
│   │   ├── UserRepository.kt
│   │   ├── ProductRepository.kt
│   │   ├── TransactionRepository.kt
│   │   └── LicenseRepository.kt
│   │
│   └── usecase/
│       ├── auth/
│       ├── product/
│       ├── transaction/
│       ├── report/
│       └── license/
│
├── ui/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   ├── components/
│   │   ├── AppBar.kt
│   │   ├── ProductCard.kt
│   │   ├── CartItemCard.kt
│   │   └── ...
│   │
│   └── screen/
│       ├── splash/
│       ├── activation/
│       ├── login/
│       ├── pos/
│       ├── product/
│       ├── report/
│       └── settings/
│
└── di/
    ├── AppModule.kt
    ├── DatabaseModule.kt
    ├── NetworkModule.kt
    ├── RepositoryModule.kt
    └── UseCaseModule.kt
```

