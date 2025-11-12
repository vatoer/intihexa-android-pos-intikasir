# Authentication Feature Module

## Overview
Complete, modular authentication feature untuk IntiKasir POS dengan clean architecture, modern UI, dan security best practices.

## Architecture

### Domain Layer (`domain/`)
- **Models**: `AuthResult`, `AuthSession`, `AuthErrorType`
- **Repository Interface**: `AuthRepository`
- **Use Cases**:
  - `LoginUseCase` - Handle login logic
  - `LogoutUseCase` - Handle logout logic
  - `GetCurrentUserUseCase` - Get logged in user
  - `ValidatePinUseCase` - Validate PIN format
  - `CheckAuthStatusUseCase` - Check if user is logged in

### Data Layer (`data/`)
- **Repository Implementation**: `AuthRepositoryImpl`
- **Data Sources**:
  - `AuthLocalDataSource` - Database operations
  - `AuthPreferencesDataSource` - Session management dengan DataStore
- **Mapper**: `UserMapper` - Convert Entity ↔ Domain Model

### UI Layer (`ui/`)
- **Login**: `LoginScreen`, `LoginViewModel`, `LoginUiState`
- **Splash**: `SplashScreen`, `SplashViewModel`, `SplashUiState`
- **Components**:
  - `PinInputField` - Custom PIN input dengan number pad
  - `UserRoleSelector` - Role selection component

### Utilities (`util/`)
- `SecurityUtil` - PIN hashing (SHA-256)
- `PinValidator` - PIN validation dan strength checking
- `AuthTestData` - Sample data untuk development

### Navigation (`navigation/`)
- `AuthRoutes` - Route constants
- `AuthNavGraph` - Navigation graph setup

## Features

### ✅ Implemented
- ✅ PIN-based authentication (4-6 digits)
- ✅ Secure PIN hashing (SHA-256)
- ✅ Session management dengan DataStore
- ✅ Real-time PIN validation
- ✅ Custom number pad UI
- ✅ Splash screen dengan auth check
- ✅ Modern Material 3 UI
- ✅ Loading states dan error handling
- ✅ Auto-logout capability
- ✅ Clean architecture (Domain, Data, UI)
- ✅ Dependency injection dengan Hilt
- ✅ Reactive flows dengan Kotlin Coroutines

### 🚧 Future Enhancements
- [ ] Biometric authentication (fingerprint/face)
- [ ] PIN reset/recovery flow
- [ ] User registration screen (admin only)
- [ ] Session timeout dengan auto-logout
- [ ] Login attempt limiting
- [ ] Audit log untuk login/logout
- [ ] Multi-device session management
- [ ] Stronger hashing (bcrypt/argon2)

## Usage

### 1. Navigation Setup
```kotlin
// Di MainActivity atau NavHost
NavHost(
    navController = navController,
    startDestination = AUTH_GRAPH_ROUTE
) {
    authNavGraph(
        navController = navController,
        onAuthSuccess = {
            // Navigate to home/POS screen
            navController.navigate("home") {
                popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
            }
        }
    )
    
    // Other navigation graphs...
}
```

### 2. Check Auth Status
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    val currentUser by viewModel.getCurrentUser().collectAsState(null)
    
    currentUser?.let { user ->
        Text("Welcome, ${user.name}!")
    }
}
```

### 3. Logout
```kotlin
// Inject LogoutUseCase
class MyViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            // Navigate to login
        }
    }
}
```

## Security

### PIN Storage
- PINs are **never** stored in plain text
- SHA-256 hashing applied before storage
- Consider upgrading to bcrypt/argon2 for production

### Session Management
- Encrypted DataStore Preferences
- Session cleared on logout
- Auto-expire capability (implement if needed)

### Best Practices
- Use HTTPS for future API calls
- Implement certificate pinning
- Add rate limiting for login attempts
- Enable ProGuard/R8 obfuscation

## Testing

### Sample Users (Development)
Available via `AuthTestData`:
- **Admin**: PIN `1234`
- **Kasir 1**: PIN `5678`
- **Kasir 2**: PIN `9999`
- **Manager**: PIN `0000`

### Initialize Sample Data
```kotlin
// Di database initialization atau debug menu
viewModelScope.launch {
    val sampleUsers = AuthTestData.createSampleUsers()
    sampleUsers.forEach { user ->
        userDao.insertUser(user)
    }
}
```

## Dependencies
- Hilt (Dependency Injection)
- Room (Database)
- DataStore (Session storage)
- Jetpack Compose (UI)
- Kotlin Coroutines & Flow (Async)
- Navigation Compose (Navigation)

## File Structure
```
feature.auth/
├── data/
│   ├── mapper/
│   │   └── UserMapper.kt
│   ├── repository/
│   │   └── AuthRepositoryImpl.kt
│   └── source/
│       ├── AuthLocalDataSource.kt
│       └── AuthPreferencesDataSource.kt
├── di/
│   └── AuthModule.kt
├── domain/
│   ├── model/
│   │   └── AuthResult.kt
│   ├── repository/
│   │   └── AuthRepository.kt
│   └── usecase/
│       ├── CheckAuthStatusUseCase.kt
│       ├── GetCurrentUserUseCase.kt
│       ├── LoginUseCase.kt
│       ├── LogoutUseCase.kt
│       └── ValidatePinUseCase.kt
├── navigation/
│   ├── AuthNavGraph.kt
│   └── AuthRoutes.kt
├── ui/
│   ├── components/
│   │   ├── PinInputField.kt
│   │   └── UserRoleSelector.kt
│   ├── login/
│   │   ├── LoginScreen.kt
│   │   ├── LoginUiState.kt
│   │   └── LoginViewModel.kt
│   └── splash/
│       ├── SplashScreen.kt
│       ├── SplashUiState.kt
│       └── SplashViewModel.kt
└── util/
    ├── AuthTestData.kt
    ├── PinValidator.kt
    └── SecurityUtil.kt
```

## Contributors
- Built with ❤️ for IntiKasir POS
- Clean Architecture principles
- Material Design 3 guidelines

---

For issues or feature requests, please contact the development team.

