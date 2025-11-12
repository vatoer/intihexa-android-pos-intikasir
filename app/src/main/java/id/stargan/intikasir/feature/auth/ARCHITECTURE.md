# Auth Feature Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          UI LAYER (Presentation)                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐        │
│  │ SplashScreen │  │ LoginScreen  │  │   Components       │        │
│  │              │  │              │  │  - PinInputField   │        │
│  │ - Auto check │  │ - PIN input  │  │  - UserRoleSelect  │        │
│  │   auth       │  │ - Number pad │  │  - Custom numpad   │        │
│  │ - Navigate   │  │ - Validation │  │                    │        │
│  └──────┬───────┘  └──────┬───────┘  └────────────────────┘        │
│         │                 │                                          │
│         ▼                 ▼                                          │
│  ┌──────────────┐  ┌──────────────┐                                │
│  │SplashVM      │  │ LoginVM      │  ◀── MVI Pattern                │
│  │              │  │              │      Single State Flow          │
│  │- UiState     │  │- UiState     │                                │
│  │- UiEvent     │  │- UiEvent     │                                │
│  └──────┬───────┘  └──────┬───────┘                                │
│         │                 │                                          │
└─────────┼─────────────────┼──────────────────────────────────────────┘
          │                 │
          ▼                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER (Business Logic)                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌───────────────────────────────────────────────────────────┐     │
│  │                      Use Cases                             │     │
│  │  ┌─────────────────┐  ┌─────────────────┐                │     │
│  │  │ LoginUseCase    │  │ LogoutUseCase   │                │     │
│  │  │ - Validate PIN  │  │ - Clear session │                │     │
│  │  │ - Authenticate  │  │                 │                │     │
│  │  └────────┬────────┘  └─────────────────┘                │     │
│  │           │                                                │     │
│  │  ┌────────▼──────────────────────────┐                   │     │
│  │  │  GetCurrentUserUseCase            │                   │     │
│  │  │  CheckAuthStatusUseCase           │                   │     │
│  │  │  ValidatePinUseCase               │                   │     │
│  │  └───────────────────────────────────┘                   │     │
│  └──────────────────────┬────────────────────────────────────┘     │
│                         │                                            │
│                         ▼                                            │
│  ┌─────────────────────────────────────────────────────────┐       │
│  │           AuthRepository (Interface)                     │       │
│  │  + login(pin): Flow<AuthResult>                         │       │
│  │  + logout()                                             │       │
│  │  + getCurrentUser(): Flow<User?>                        │       │
│  │  + isLoggedIn(): Flow<Boolean>                          │       │
│  │  + validatePinFormat(pin): Result<Unit>                 │       │
│  │  + hashPin(pin): String                                 │       │
│  └─────────────────────────────────────────────────────────┘       │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────┐       │
│  │               Domain Models                              │       │
│  │  - AuthResult (Success | Error | Loading)               │       │
│  │  - AuthSession (user, loginTime, isActive)              │       │
│  │  - AuthErrorType (enum)                                 │       │
│  │  - User (domain model)                                  │       │
│  └─────────────────────────────────────────────────────────┘       │
│                                                                       │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       DATA LAYER (Implementation)                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────────────────────────────────────────────────┐       │
│  │        AuthRepositoryImpl                                │       │
│  │  - Implements AuthRepository interface                   │       │
│  │  - Coordinates data sources                              │       │
│  │  - Business logic implementation                         │       │
│  │  - Error handling & mapping                              │       │
│  └─────────┬───────────────────────────────┬───────────────┘       │
│            │                               │                         │
│            ▼                               ▼                         │
│  ┌──────────────────────┐    ┌────────────────────────┐           │
│  │AuthLocalDataSource   │    │AuthPreferencesDataSrc  │           │
│  │                      │    │                        │           │
│  │- getUserByPin()      │    │- saveLoginSession()    │           │
│  │- getUserById()       │    │- clearLoginSession()   │           │
│  │- getActiveUsers()    │    │- getCurrentUserId()    │           │
│  │- insertUser()        │    │- isLoggedIn()          │           │
│  │- updateUser()        │    │- getLoginTime()        │           │
│  └──────────┬───────────┘    └────────┬───────────────┘           │
│             │                          │                             │
│             ▼                          ▼                             │
│  ┌──────────────────┐      ┌──────────────────┐                   │
│  │   UserDao        │      │   DataStore      │                   │
│  │   (Room)         │      │  (Preferences)   │                   │
│  └──────────────────┘      └──────────────────┘                   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────┐       │
│  │                  Mapper                                  │       │
│  │  UserMapper: Entity ←→ Domain Model                     │       │
│  └─────────────────────────────────────────────────────────┘       │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     CROSS-CUTTING CONCERNS                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │  SecurityUtil    │  │  PinValidator    │  │  AuthTestData   │  │
│  │  - hashPin()     │  │  - validate()    │  │  - sampleUsers  │  │
│  │  - verifyPin()   │  │  - checkStrength │  │  - testData     │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘  │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────┐       │
│  │            Dependency Injection (Hilt)                   │       │
│  │  AuthModule: Binds AuthRepository ← AuthRepositoryImpl  │       │
│  └─────────────────────────────────────────────────────────┘       │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────┐       │
│  │                  Navigation                              │       │
│  │  - AuthRoutes (SPLASH, LOGIN, etc.)                     │       │
│  │  - authNavGraph() for NavHost integration               │       │
│  └─────────────────────────────────────────────────────────┘       │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

DATA FLOW EXAMPLE: Login
═══════════════════════════

User Input PIN
    │
    ▼
LoginScreen (UI)
    │
    ▼
LoginViewModel.onEvent(PinChanged)
    │
    ▼
ValidatePinUseCase ──► Validate format
    │
    ▼
LoginUseCase.invoke(pin)
    │
    ▼
AuthRepository.login(pin)
    │
    ▼
AuthRepositoryImpl
    │
    ├──► hashPin(pin) using SecurityUtil
    │
    ├──► AuthLocalDataSource.getUserByPin(hashedPin)
    │    └──► UserDao.getUserByPin() → Room DB
    │
    ├──► Check user active status
    │
    └──► AuthPreferencesDataSource.saveLoginSession()
         └──► DataStore (persist session)
    │
    ▼
Return AuthResult.Success(user)
    │
    ▼
Flow back to LoginViewModel
    │
    ▼
Update LoginUiState
    │
    ▼
LoginScreen shows success → Navigate to Home


SECURITY FLOW
═════════════

Plain PIN (user input)
    │
    ▼
SHA-256 Hashing (SecurityUtil)
    │
    ▼
Hashed PIN (64 char hex)
    │
    ├──► Compare with DB hash (login)
    │
    └──► Store in DB (registration)


SESSION MANAGEMENT
══════════════════

Login Success
    │
    ▼
Save to DataStore:
  - userId: String
  - loginTime: Long
  - isLoggedIn: Boolean
    │
    ▼
Session Active
    │
    ├──► Can access protected screens
    │
    └──► Auto-logout on logout() call
         or app restart (optional timeout)
```

## Key Design Patterns

1. **Clean Architecture**: Clear separation of concerns
2. **Repository Pattern**: Abstract data access
3. **Use Case Pattern**: Single responsibility business logic
4. **MVI Pattern**: Unidirectional data flow in UI
5. **Observer Pattern**: Reactive flows with StateFlow
6. **Dependency Injection**: Hilt for DI
7. **Mapper Pattern**: Separate entity from domain models

## Component Responsibilities

### Domain Layer
- ✅ Business rules and logic
- ✅ Platform independent
- ✅ No Android dependencies
- ✅ Easily testable

### Data Layer
- ✅ Data access implementation
- ✅ Database operations
- ✅ API calls (future)
- ✅ Caching strategy

### UI Layer
- ✅ User interaction
- ✅ State management
- ✅ UI rendering
- ✅ Navigation

## Benefits of This Architecture

1. **Modularity**: Each component is independent
2. **Testability**: Easy to unit test each layer
3. **Scalability**: Easy to add new features
4. **Maintainability**: Clear code organization
5. **Reusability**: Components can be reused
6. **Separation of Concerns**: Each layer has one job
7. **Type Safety**: Kotlin type system enforced
8. **Reactive**: Real-time updates with Flow

