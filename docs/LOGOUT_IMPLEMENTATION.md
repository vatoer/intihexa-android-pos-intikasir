# Logout Implementation - Best Practice

## Overview
Implementasi logout yang proper dengan clean architecture, state management, dan user experience yang baik.

## Flow Diagram

```
HomeScreen
    ↓ (Click Logout Icon)
AlertDialog (Konfirmasi)
    ↓ (Klik "Ya")
HomeViewModel.logout()
    ↓
LogoutUseCase()
    ↓
AuthRepository.logout()
    ↓ (Clear Session)
DataStore.clearLoginSession()
    ↓ (Callback onLogoutComplete)
Navigate to LoginScreen
    ↓ (Clear Back Stack)
LoginScreen (User bisa login ulang)
```

## Implementation Details

### 1. **HomeViewModel**
File: `feature/home/ui/HomeViewModel.kt`

**Responsibilities:**
- Load current user info
- Handle logout process
- Manage logout state (loading indicator)

**Key Features:**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel()
```

**States:**
- `currentUser: StateFlow<User?>` - User yang sedang login
- `isLoggingOut: StateFlow<Boolean>` - Loading state saat logout

**Methods:**
- `loadCurrentUser()` - Load user info dari use case
- `logout(onLogoutComplete: () -> Unit)` - Execute logout dengan callback

### 2. **HomeScreen Update**
File: `feature/home/ui/HomeScreen.kt`

**Changes:**
- ✅ Inject `HomeViewModel` dengan Hilt
- ✅ Display user name dan role di header
- ✅ Logout dialog dengan loading state
- ✅ Disable buttons saat logout sedang proses
- ✅ Callback ke ViewModel untuk handle logout

**User Info Display:**
```kotlin
Text(
    text = "Halo, ${user.name} (${user.role.name})",
    ...
)
```

**Logout Dialog dengan Loading:**
```kotlin
if (isLoggingOut) {
    CircularProgressIndicator + "Sedang logout..."
} else {
    "Apakah Anda yakin ingin keluar?"
}
```

### 3. **MainActivity Navigation**
File: `MainActivity.kt`

**Navigation Logic:**
```kotlin
onLogout = {
    navController.navigate(AuthRoutes.LOGIN) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
```

**Explanation:**
- `navigate(AuthRoutes.LOGIN)` - Navigate langsung ke Login Screen (bukan Splash)
- `popUpTo(0) { inclusive = true }` - Clear semua back stack
- `launchSingleTop = true` - Hindari duplicate Login Screen

## Why Login Screen, Not Splash?

### Decision Rationale:

1. **User Context:**
   - User sudah pernah login sebelumnya
   - User secara aktif memilih untuk logout
   - User expect untuk kembali ke halaman login

2. **UX Best Practice:**
   - Splash Screen untuk first launch / cold start
   - Login Screen untuk re-authentication
   - Lebih cepat (no unnecessary delay dari splash)

3. **Flow Comparison:**

   **❌ Bad (Navigate to Splash):**
   ```
   Logout → Splash (1.5s delay) → Login
   ```
   
   **✅ Good (Navigate to Login):**
   ```
   Logout → Login (instant)
   ```

## Security Implementation

### 1. **Session Clearing**
```kotlin
LogoutUseCase → AuthRepository.logout() → DataStore.clearLoginSession()
```

**What gets cleared:**
- User ID dari DataStore
- Login timestamp
- Session flags
- Any cached user data

### 2. **Back Stack Management**
```kotlin
popUpTo(0) { inclusive = true }
```
- Clear semua screen dari back stack
- User tidak bisa kembali ke HomeScreen dengan back button
- Prevent unauthorized access

### 3. **Navigation Protection**
```kotlin
launchSingleTop = true
```
- Hindari multiple Login Screen instances
- Clean navigation state

## User Experience Features

### 1. **Loading Indicator**
- Show `CircularProgressIndicator` saat logout
- Disable buttons saat proses logout
- Prevent multiple logout requests

### 2. **User Info Display**
- Tampilkan nama user di header
- Tampilkan role (ADMIN/CASHIER)
- Personalized greeting: "Halo, [Name] ([Role])"

### 3. **Confirmation Dialog**
- AlertDialog sebelum logout
- Prevent accidental logout
- "Ya" / "Batal" options
- Can't dismiss saat logout proses

### 4. **Error Handling**
```kotlin
try {
    logoutUseCase()
    onLogoutComplete()
} catch (e: Exception) {
    e.printStackTrace()
    // Handle error gracefully
}
```

## Testing Scenarios

### 1. **Normal Logout Flow**
1. User di HomeScreen
2. Click logout icon
3. Dialog konfirmasi muncul
4. Click "Ya"
5. Loading indicator muncul
6. Navigate ke Login Screen
7. Back button tidak bisa kembali ke Home

### 2. **Cancel Logout**
1. User di HomeScreen
2. Click logout icon
3. Dialog konfirmasi muncul
4. Click "Batal"
5. Dialog dismiss
6. User tetap di HomeScreen

### 3. **Multiple Logout Attempts**
1. User click logout
2. Click "Ya"
3. Saat loading, buttons disabled
4. Tidak bisa click "Ya" lagi
5. Tidak bisa dismiss dialog

### 4. **After Logout**
1. User di Login Screen
2. Back button tidak bisa ke Home
3. User harus login ulang
4. Session sudah cleared

## Architecture Benefits

### 1. **Separation of Concerns**
- ViewModel: Business logic & state
- Screen: UI & user interaction
- UseCase: Single responsibility
- Repository: Data operations

### 2. **Testability**
- ViewModel dapat di-test secara isolated
- UseCase dapat di-mock
- Navigation dapat di-verify

### 3. **Maintainability**
- Clear responsibility per layer
- Easy to modify logout logic
- Easy to add features (e.g., logout reason)

### 4. **Scalability**
- Easy to add analytics
- Easy to add logout confirmation reason
- Easy to add "logout from all devices"

## Future Enhancements

### 1. **Logout Reason**
```kotlin
fun logout(reason: LogoutReason, onLogoutComplete: () -> Unit)

enum class LogoutReason {
    USER_INITIATED,
    SESSION_EXPIRED,
    FORCE_LOGOUT,
    SECURITY_VIOLATION
}
```

### 2. **Analytics**
```kotlin
// Track logout events
analytics.logEvent("user_logout", mapOf(
    "user_role" to currentUser.role,
    "session_duration" to sessionDuration
))
```

### 3. **Logout from All Devices**
- Integrate dengan backend
- Invalidate all sessions
- Notify other devices

### 4. **Session Timeout**
- Auto logout setelah inactive period
- Show countdown dialog
- Option to extend session

## Code Quality

### ✅ Best Practices Applied:

1. **Dependency Injection**: Hilt untuk all dependencies
2. **State Management**: StateFlow untuk reactive UI
3. **Coroutines**: Async operations dengan viewModelScope
4. **Error Handling**: Try-catch dengan logging
5. **Loading States**: User feedback saat proses
6. **Confirmation Dialog**: Prevent accidental logout
7. **Back Stack Management**: Security & UX
8. **Single Responsibility**: Each layer has one job
9. **Clean Architecture**: Use case → Repository → DataSource
10. **Material Design 3**: Modern UI components

## Summary

Implementasi logout ini mengikuti:
- ✅ Clean Architecture principles
- ✅ Material Design 3 guidelines
- ✅ Android UX best practices
- ✅ Security best practices
- ✅ SOLID principles
- ✅ Testable code structure

User logout dari HomeScreen akan:
1. Clear session di DataStore
2. Navigate ke Login Screen
3. Clear back stack (tidak bisa back ke Home)
4. Display loading indicator
5. Handle error gracefully

Flow ini memberikan user experience yang smooth, secure, dan sesuai ekspektasi pengguna.

