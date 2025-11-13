# Default Users Initialization

## Overview
Implementasi inisialisasi user default menggunakan clean architecture best practices. User default hanya dibuat sekali saat aplikasi pertama kali dijalankan (database masih kosong).

## Default Users
Saat aplikasi pertama kali di-install/dijalankan, akan dibuat 2 user default:

1. **Admin**
   - PIN: `1234`
   - Role: ADMIN
   - Nama: Admin

2. **Kasir 1**
   - PIN: `5678`
   - Role: CASHIER
   - Nama: Kasir 1

## Architecture Flow

```
SplashViewModel
    ↓
InitializeDefaultUsersUseCase
    ↓
AuthRepository.initializeDefaultUsers()
    ↓
AuthRepositoryImpl.createDefaultUsers()
    ↓
AuthLocalDataSource.insertUser()
    ↓
UserDao (Room Database)
```

## Implementation Details

### 1. Repository Layer
**File**: `AuthRepositoryImpl.kt`

Menambahkan fungsi:
- `initializeDefaultUsers()` - Inisialisasi user default jika database kosong
- `hasUsers()` - Check apakah sudah ada user di database
- `createDefaultUsers()` - Private function untuk membuat list user default

### 2. Use Case Layer
**File**: `InitializeDefaultUsersUseCase.kt`

Use case untuk memanggil repository initialization.

### 3. Presentation Layer
**File**: `SplashViewModel.kt`

ViewModel Splash Screen yang:
- Memanggil `InitializeDefaultUsersUseCase` saat init
- Check authentication status setelah inisialisasi
- Handle error dengan gracefully

### 4. Main Activity
**File**: `MainActivity.kt`

Tidak lagi memanggil `createSampleUsers()` secara langsung. Inisialisasi sepenuhnya dilakukan di layer data melalui SplashViewModel.

## Best Practices Applied

1. ✅ **Single Responsibility**: Setiap layer punya tanggung jawab yang jelas
2. ✅ **Separation of Concerns**: Logic inisialisasi di repository, bukan di UI
3. ✅ **Idempotency**: Inisialisasi hanya dilakukan jika database kosong
4. ✅ **Clean Architecture**: Use case → Repository → Data Source
5. ✅ **Dependency Injection**: Semua dependency di-inject via Hilt
6. ✅ **Error Handling**: Menangani potential error di ViewModel

## Testing

Untuk testing, Anda dapat:
1. Uninstall aplikasi
2. Install ulang
3. Saat splash screen, user default akan dibuat
4. Login dengan PIN `1234` (Admin) atau `5678` (Kasir 1)

## Notes

- User default menggunakan PIN yang di-hash dengan SHA-256
- Inisialisasi dilakukan secara asynchronous di background
- Tidak mengganggu flow authentication check
- Jika terjadi error saat inisialisasi, aplikasi tetap berjalan normal

