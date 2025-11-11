# 🚀 Inti Kasir - Quick Reference Card

## ⚡ Dependency Injection - Hilt 2.52

### ✅ Status: ACTIVE & RECOMMENDED
**Hilt is Google's official DI framework for Android - NOT deprecated!**

---

## 📋 Common Hilt Annotations

### Application
```kotlin
@HiltAndroidApp
class IntiKasirApplication : Application()
```

### Activity
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

### ViewModel
```kotlin
@HiltViewModel
class PosViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel()
```

### Composable
```kotlin
@Composable
fun PosScreen(
    viewModel: PosViewModel = hiltViewModel()
) { }
```

### Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): IntiKasirDatabase = Room.databaseBuilder(/*...*/).build()
}
```

### Repository Binding
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository
}
```

---

## 🏗️ Project Structure Quick View

```
app/src/main/java/id/stargan/intikasir/
├── data/
│   ├── local/entity/     # Room entities (6)
│   ├── local/dao/        # DAOs (6)
│   ├── local/database/   # Database
│   └── repository/       # Implementations
├── domain/
│   ├── model/           # Domain models
│   └── repository/      # Interfaces
├── ui/
│   ├── screen/          # Compose screens
│   └── theme/           # Material 3 theme
├── di/                  # Hilt modules
└── IntiKasirApplication.kt
```

---

## 📦 Dependencies (build.gradle.kts)

### Hilt
```kotlin
implementation("com.google.dagger:hilt-android:2.52")
ksp("com.google.dagger:hilt-compiler:2.52")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
```

### Room
```kotlin
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```

### Compose
```kotlin
implementation(platform("androidx.compose:compose-bom:2024.11.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
```

---

## 🔧 Common Commands

### Gradle Sync
```bash
./gradlew --refresh-dependencies
```

### Clean Build
```bash
./gradlew clean
./gradlew assembleDebug
```

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 🗄️ Database Cheat Sheet

### Entities
- UserEntity (users)
- CategoryEntity (categories)
- ProductEntity (products)
- TransactionEntity (transactions)
- TransactionItemEntity (transaction_items)
- StoreSettingsEntity (store_settings)

### DAOs
- UserDao
- CategoryDao
- ProductDao
- TransactionDao
- TransactionItemDao
- StoreSettingsDao

---

## 🎨 Compose Basics

### State
```kotlin
val state by viewModel.state.collectAsState()
```

### Navigation
```kotlin
val navController = rememberNavController()
```

### Material 3
```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.typography.titleLarge
```

---

## 🔐 Common Scopes

```kotlin
@Singleton              // App lifetime
@ActivityScoped         // Activity lifetime
@ViewModelScoped        // ViewModel lifetime
@ActivityRetainedScoped // Survives config changes
```

---

## 📱 Key Screens

1. **POS Screen** - Main transaction screen
2. **Login Screen** - PIN authentication
3. **Activation Screen** - License validation
4. **Products Screen** - Product management
5. **Reports Screen** - Sales reports
6. **Settings Screen** - Configuration

---

## 🐛 Quick Troubleshooting

### "Unresolved reference: dagger"
```bash
# Sync Gradle files
File → Sync Project with Gradle Files
```

### "Cannot find symbol: DaggerApplicationComponent"
```bash
# Clean and rebuild
./gradlew clean
Build → Rebuild Project
```

### Build errors after Hilt update
```bash
# Invalidate caches
File → Invalidate Caches → Invalidate and Restart
```

---

## 📚 Documentation Links

- Main: `README.md`
- Architecture: `docs/ARCHITECTURE.md`
- DI Guide: `docs/DEPENDENCY_INJECTION.md`
- Quick Start: `docs/QUICK_START.md`
- All Docs: `docs/INDEX.md`

---

## 💡 Pro Tips

1. **Use KSP** instead of KAPT (faster builds)
2. **Scope appropriately** (don't make everything Singleton)
3. **Use @Binds** for interface binding (faster than @Provides)
4. **Lazy inject** heavy dependencies
5. **Check compile errors** first (Hilt catches issues at compile-time)

---

## ✅ Checklist for New Feature

- [ ] Create domain model
- [ ] Create repository interface
- [ ] Create repository implementation
- [ ] Add @Inject constructor
- [ ] Bind in RepositoryModule (if needed)
- [ ] Create ViewModel with @HiltViewModel
- [ ] Inject in Composable with hiltViewModel()
- [ ] Write tests

---

**Version:** 1.0  
**Last Updated:** November 11, 2025  
**Hilt Version:** 2.52 ✅ (Active & Recommended)

