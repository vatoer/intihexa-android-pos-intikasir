# Dependency Injection in Android - Hilt Best Practices (2025)

## ⚠️ Important Clarification: Hilt is NOT Deprecated

**As of November 2025, Hilt is still the officially recommended dependency injection framework by Google for Android development.**

### Official Status
- ✅ **Actively Maintained** by Google
- ✅ **Recommended** in official Android documentation
- ✅ **Latest Version:** 2.52 (Released 2024)
- ✅ **First-class support** for Jetpack Compose
- ✅ **Regular updates** and bug fixes

---

## 🔄 History of DI in Android

### Timeline:
1. **Manual DI** (2008-2015) - Constructor injection
2. **Dagger 2** (2015-2020) - Complex but powerful
3. **Hilt** (2020-Present) - Simplified Dagger for Android
4. **Koin** (Alternative) - Kotlin-first DI

### Why Hilt?
Hilt was created by Google specifically to simplify Dagger 2 for Android:
- Less boilerplate
- Predefined components for Android
- Lifecycle-aware scoping
- Better Jetpack integration
- Easier learning curve

---

## 📊 Current DI Options in Android (2025)

| Framework | Status | Recommendation | Use Case |
|-----------|--------|----------------|----------|
| **Hilt** | ✅ Active | **Recommended** | Production apps, official support |
| **Koin** | ✅ Active | Alternative | Kotlin-only projects, simpler syntax |
| **Dagger 2** | ✅ Active | Not recommended | Legacy projects only |
| **Manual DI** | ✅ Valid | Small projects | Learning, very small apps |

---

## ✅ Why We're Using Hilt in Inti Kasir

### 1. **Official Google Support**
```kotlin
// Officially recommended by Android team
// Source: https://developer.android.com/training/dependency-injection/hilt-android
```

### 2. **Compile-Time Safety**
```kotlin
// Hilt verifies DI graph at compile time
// Errors caught before runtime
@HiltAndroidApp
class IntiKasirApplication : Application()
```

### 3. **Jetpack Compose Integration**
```kotlin
// First-class support for Compose
@Composable
fun PosScreen(
    viewModel: PosViewModel = hiltViewModel() // ✅ Built-in support
) {
    // ...
}
```

### 4. **Android Lifecycle Awareness**
```kotlin
// Automatic scoping to Android components
@HiltViewModel // ✅ Scoped to ViewModel lifecycle
class PosViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel()
```

### 5. **Standard Components**
```kotlin
// Predefined Android components
@AndroidEntryPoint // ✅ For Activity
@AndroidEntryPoint // ✅ For Fragment
@HiltViewModel     // ✅ For ViewModel
@HiltWorker        // ✅ For WorkManager
```

---

## 🆕 Latest Hilt Features (2024-2025)

### Version 2.52 Updates:
1. ✅ **Better Kotlin support**
2. ✅ **Improved build times**
3. ✅ **Enhanced error messages**
4. ✅ **Compose multiplatform preparation**
5. ✅ **Better KSP integration**

### Modern Best Practices:

#### 1. Use KSP instead of KAPT (Faster builds)
```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("com.google.dagger.hilt.android") version "2.52"
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52") // ✅ Use KSP
    
    // For Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}
```

#### 2. Module Organization (2025 Style)
```kotlin
// Separate modules by feature
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): IntiKasirDatabase {
        return Room.databaseBuilder(
            context,
            IntiKasirDatabase::class.java,
            "intikasir_database"
        ).build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Network dependencies
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Repository bindings
}
```

#### 3. ViewModels with Hilt (2025)
```kotlin
@HiltViewModel
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val savedStateHandle: SavedStateHandle // ✅ Automatic injection
) : ViewModel() {
    // ViewModel code
}

// Usage in Compose
@Composable
fun PosScreen(
    viewModel: PosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    // UI code
}
```

#### 4. Repository Pattern with Hilt
```kotlin
// Interface in domain layer
interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
}

// Implementation in data layer
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val firebaseDataSource: FirebaseDataSource
) : ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllActiveProducts()
            .map { entities -> entities.map { it.toDomain() } }
    }
}

// Binding module
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

#### 5. Testing with Hilt (2025)
```kotlin
@HiltAndroidTest
class PosScreenTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: ProductRepository
    
    @Before
    fun init() {
        hiltRule.inject()
    }
    
    @Test
    fun testProductDisplay() {
        // Test code
    }
}
```

---

## 🆚 Hilt vs Alternatives (2025 Comparison)

### Hilt vs Koin

| Feature | Hilt | Koin |
|---------|------|------|
| **Compile-time safety** | ✅ Yes | ❌ No (runtime) |
| **Build speed** | Good | Better |
| **Learning curve** | Medium | Easy |
| **Android integration** | ✅ Official | Community |
| **Boilerplate** | Low | Very Low |
| **Error detection** | Compile-time | Runtime |
| **Multiplatform** | Android only | ✅ Yes |

**Recommendation:** Use **Hilt** for Android-only apps (like Inti Kasir)

### Hilt vs Manual DI

```kotlin
// Manual DI (lots of boilerplate)
class PosViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    // ...
}

// In Activity
class MainActivity : ComponentActivity() {
    private val database by lazy { 
        Room.databaseBuilder(/*...*/).build() 
    }
    private val dao by lazy { database.productDao() }
    private val repository by lazy { ProductRepositoryImpl(dao) }
    private val viewModel by lazy { PosViewModel(repository) }
    // ❌ Manual wiring, error-prone
}

// With Hilt (automatic)
@HiltViewModel
class PosViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ✅ Automatic injection, type-safe
}
```

---

## 📚 Official Resources (2025)

### Documentation
- [Hilt Official Docs](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt Codelab](https://developer.android.com/codelabs/android-hilt)
- [Hilt with Compose](https://developer.android.com/jetpack/compose/libraries#hilt)

### GitHub
- [Hilt Repository](https://github.com/google/dagger)
- [Release Notes](https://github.com/google/dagger/releases)

### Sample Apps
- [Now in Android](https://github.com/android/nowinandroid) - Google's official sample
- [Architecture Samples](https://github.com/android/architecture-samples)

---

## ⚡ Performance Optimizations (2025)

### 1. Use KSP for Faster Builds
```kotlin
// 30-50% faster than KAPT
plugins {
    id("com.google.devtools.ksp")
}
```

### 2. Lazy Injection
```kotlin
@HiltViewModel
class PosViewModel @Inject constructor(
    private val repository: Lazy<ProductRepository> // ✅ Lazy loading
) : ViewModel()
```

### 3. Scope Appropriately
```kotlin
@Singleton // ✅ App-wide singleton
@ActivityScoped // ✅ Activity scope
@ViewModelScoped // ✅ ViewModel scope
```

### 4. AssistedInject for Dynamic Parameters
```kotlin
@HiltViewModel(assistedFactory = PosViewModel.Factory::class)
class PosViewModel @AssistedInject constructor(
    @Assisted private val initialProductId: String,
    private val repository: ProductRepository
) : ViewModel() {
    
    @AssistedFactory
    interface Factory {
        fun create(initialProductId: String): PosViewModel
    }
}
```

---

## 🔮 Future of Hilt

### Upcoming Features:
1. **Compose Multiplatform Support** (In development)
2. **Better KMP Integration** (Planned)
3. **Improved Build Performance** (Ongoing)
4. **Enhanced Testing APIs** (Roadmap)

### Google's Commitment:
> "Hilt is the recommended solution for dependency injection in Android apps, and works seamlessly with Compose."
> 
> — Android Developer Documentation, 2024

---

## ✅ Conclusion for Inti Kasir

**We are using Hilt 2.52 (latest stable) because:**

1. ✅ **Official Google recommendation**
2. ✅ **Best Jetpack Compose integration**
3. ✅ **Compile-time safety**
4. ✅ **Active maintenance**
5. ✅ **Production-ready**
6. ✅ **Large community**
7. ✅ **Excellent documentation**

**Hilt is NOT deprecated and will continue to be the standard for Android DI.**

---

## 📝 Migration Path (If needed in future)

If Google ever deprecates Hilt (unlikely), migration path would be:

```
Hilt → Manual DI (Always possible)
Hilt → Koin (Easy migration)
Hilt → New official solution (Google would provide tools)
```

But for now and foreseeable future: **Hilt is the way to go!** ✅

---

**Last Updated:** November 11, 2025  
**Hilt Version:** 2.52  
**Status:** ✅ Actively Maintained & Recommended

