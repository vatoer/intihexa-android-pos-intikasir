# POS Reactive Transaction - Implementation Guide

## ✅ STATUS: IN PROGRESS

**Tanggal:** 15 November 2025  
**Tujuan:** Implementasi POS dengan database-driven reactive flow

---

## 🎯 Problem Statement

**Sebelumnya:**
- Cart disimpan di memory (ViewModel state)
- Navigasi POS → Cart → Payment kehilangan data
- Save hanya dilakukan saat final checkout
- Tidak ada persistence jika user keluar/crash

**Requirement Baru:**
- Buat transaksi DRAFT saat pertama kali masuk POS
- Setiap perubahan cart langsung save ke database
- Navigate dengan pass transaction ID
- Screen load data dari database (reactive)

---

## 🏗️ Arsitektur Baru

### Flow Diagram
```
User Login
  ↓
Click "Kasir"
  ↓
PosScreen.onCreate()
  ├→ Check: ada draft transaksi aktif?
  │   ├→ Ya: load transaction ID
  │   └→ Tidak: createEmptyDraft() → get ID
  ↓
PosViewModel(transactionId)
  ├→ Observe transaction dari DB (reactive)
  ├→ Observe transaction items dari DB (reactive)
  └→ Setiap cart change → updateTransactionItems(transactionId, items)
  ↓
User modifikasi cart
  ├→ Add product → save to DB instantly
  ├→ Change quantity → save to DB instantly
  ├→ Set discount → save to DB instantly
  └→ Remove item → save to DB instantly
  ↓
User click icon Cart
  ├→ Navigate to CartScreen(transactionId)
  ↓
CartScreen(transactionId)
  ├→ Load transaction dari DB
  ├→ Observe items dari DB
  └→ Show data (real-time)
  ↓
User back ke POS
  ├→ Navigate to PosScreen(transactionId)
  ├→ Data tetap sinkron (dari DB)
  ↓
User click "Lanjut Pembayaran"
  ├→ Navigate to PaymentScreen(transactionId)
  ↓
PaymentScreen(transactionId)
  ├→ Load transaction dari DB
  ├→ Update payment method → save to DB
  ├→ Update discount → save to DB
  ↓
User click "Checkout"
  ├→ finalizeTransaction(transactionId)
  │   ├→ Update status: DRAFT → COMPLETED
  │   ├→ Set cash received/change
  │   └→ Decrement stock
  ├→ Navigate to ReceiptScreen(transactionId)
```

---

## 📋 Components Updated

### 1. Repository Layer

#### TransactionRepository (Interface)
```kotlin
interface TransactionRepository {
    // Create empty draft
    suspend fun createEmptyDraft(cashierId: String, cashierName: String): String
    
    // Reactive getters
    fun getTransactionById(transactionId: String): Flow<TransactionEntity?>
    fun getTransactionItems(transactionId: String): Flow<List<TransactionItemEntity>>
    
    // Incremental updates
    suspend fun updateTransactionItems(transactionId: String, items: List<Pair<String, Int>>, itemDiscounts: Map<String, Double>)
    suspend fun updateTransactionTotals(transactionId: String, subtotal: Double, tax: Double, discount: Double, total: Double)
    suspend fun updateTransactionPayment(transactionId: String, paymentMethod: PaymentMethod, globalDiscount: Double)
    
    // Finalize
    suspend fun finalizeTransaction(transactionId: String, cashReceived: Double, cashChange: Double, notes: String?)
}
```

#### TransactionRepositoryImpl
- ✅ `createEmptyDraft()` - Buat transaksi kosong dengan status DRAFT
- ✅ `getTransactionById()` - Return Flow untuk reactive updates
- ✅ `getTransactionItems()` - Return Flow untuk item list
- ✅ `updateTransactionItems()` - Delete old items, insert new ones
- ✅ `updateTransactionTotals()` - Update subtotal, tax, discount, total
- ✅ `updateTransactionPayment()` - Update payment method & discount
- ✅ `finalizeTransaction()` - Set status COMPLETED, decrement stock

### 2. DAO Layer

#### TransactionDao
- ✅ `getTransactionByIdFlow()` - Already exists
- ✅ `updateTransactionTotals()` - NEW
- ✅ `updateTransactionPayment()` - NEW
- ✅ `finalizeTransaction()` - NEW

#### TransactionItemDao
- ✅ `getItemsByTransactionIdFlow()` - NEW (reactive)
- ✅ `getItemsByTransactionId()` - NEW (suspend)
- ✅ `deleteItemsByTransactionId()` - NEW

### 3. ViewModel Layer

#### PosViewModel (Perlu diubah total)
**Current State:**
```kotlin
data class UiState(
    val products: List<Product>,
    val cart: Map<String, CartItem>, // In-memory
    // ...
)
```

**New State:**
```kotlin
data class UiState(
    val transactionId: String? = null,
    val transaction: TransactionEntity? = null,
    val transactionItems: List<TransactionItemEntity> = emptyList(),
    val products: List<Product>,
    // Computed from transaction
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    // ...
)
```

**New Methods:**
```kotlin
// Init with transaction ID or create new
suspend fun initializeTransaction(cashierId: String, cashierName: String)

// Cart operations (auto-save to DB)
suspend fun addProduct(productId: String)
suspend fun updateQuantity(productId: String, quantity: Int)
suspend fun setItemDiscount(productId: String, discount: Double)
suspend fun removeProduct(productId: String)

// Computed totals (auto-update DB)
private suspend fun recalculateAndSave()
```

### 4. Screen Layer

#### PosScreen
**Old:**
```kotlin
@Composable
fun PosScreen(
    onNavigateToPayment: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: PosViewModel = hiltViewModel()
)
```

**New:**
```kotlin
@Composable
fun PosScreen(
    transactionId: String? = null, // From navigation args
    onNavigateToPayment: (String) -> Unit, // Pass transaction ID
    onNavigateToCart: (String) -> Unit, // Pass transaction ID
    viewModel: PosViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        if (transactionId != null) {
            viewModel.loadTransaction(transactionId)
        } else {
            viewModel.initializeTransaction(currentUser?.id ?: "", currentUser?.name ?: "")
        }
    }
}
```

#### CartScreen
**New:**
```kotlin
@Composable
fun CartScreen(
    transactionId: String, // Required from navigation
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit,
    viewModel: PosViewModel = hiltViewModel() // Shared ViewModel
) {
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    
    val transaction by viewModel.transaction.collectAsState()
    val items by viewModel.transactionItems.collectAsState()
}
```

#### PaymentScreen
**New:**
```kotlin
@Composable
fun PaymentScreen(
    transactionId: String, // Required from navigation
    onPaymentSuccess: (String) -> Unit,
    viewModel: PosViewModel = hiltViewModel() // Shared ViewModel
) {
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
}
```

### 5. Navigation

#### Routes
```kotlin
object PosRoutes {
    const val POS = "pos"
    const val POS_WITH_TRANSACTION = "pos/{transactionId}"
    const val CART = "cart/{transactionId}"
    const val PAYMENT = "payment/{transactionId}"
    const val RECEIPT = "receipt/{transactionId}"
    
    fun posWithTransaction(transactionId: String) = "pos/$transactionId"
    fun cart(transactionId: String) = "cart/$transactionId"
    fun payment(transactionId: String) = "payment/$transactionId"
    fun receipt(transactionId: String) = "receipt/$transactionId"
}
```

#### Navigation Graph
```kotlin
composable(PosRoutes.POS) {
    PosScreen(
        transactionId = null, // New transaction
        onNavigateToCart = { transactionId ->
            navController.navigate(PosRoutes.cart(transactionId))
        },
        onNavigateToPayment = { transactionId ->
            navController.navigate(PosRoutes.payment(transactionId))
        }
    )
}

composable(
    route = PosRoutes.CART,
    arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
) { backStackEntry ->
    val transactionId = backStackEntry.arguments?.getString("transactionId")!!
    CartScreen(
        transactionId = transactionId,
        onNavigateBack = { navController.navigateUp() },
        onNavigateToPayment = { transactionId ->
            navController.navigate(PosRoutes.payment(transactionId))
        }
    )
}
```

---

## ✅ Implementation Checklist

### Backend (Repository & DAO)
- [x] Add new methods to TransactionRepository interface
- [x] Implement methods in TransactionRepositoryImpl
- [x] Add DAO methods: updateTransactionTotals, updateTransactionPayment, finalizeTransaction
- [x] Add TransactionItemDao: getItemsByTransactionIdFlow, deleteItemsByTransactionId

### ViewModel
- [ ] Refactor PosViewModel to use transaction ID
- [ ] Add transaction loading from DB (reactive)
- [ ] Update cart operations to save to DB
- [ ] Add recalculate & save logic
- [ ] Remove old in-memory cart logic

### Screens
- [ ] Update PosScreen to accept transactionId param
- [ ] Update CartScreen to load from DB
- [ ] Update PaymentScreen to load from DB
- [ ] Add LaunchedEffect for initialization

### Navigation
- [ ] Add transactionId to routes
- [ ] Update HomeNavGraph with new routes
- [ ] Pass transactionId between screens

### Testing
- [ ] Test: Create empty draft on POS open
- [ ] Test: Add product → saved to DB
- [ ] Test: Navigate POS → Cart → data intact
- [ ] Test: Navigate Cart → back to POS → data intact
- [ ] Test: Checkout → status COMPLETED, stock decremented

---

## 🎯 Benefits

### Reliability
- ✅ No data loss on navigation
- ✅ Survives app crash/restart
- ✅ Audit trail (every change logged)

### Performance
- ✅ Reactive UI (Flow observables)
- ✅ Single source of truth (database)
- ✅ No memory leaks (DB handles lifecycle)

### Features Enabled
- ✅ Multiple draft transactions
- ✅ Resume interrupted transactions
- ✅ Transaction history tracking
- ✅ Real-time collaboration (future: multi-user)

---

## 📝 Next Steps

1. ✅ **Repository & DAO layer** - DONE
2. **Refactor PosViewModel** - IN PROGRESS
3. **Update Screens** - PENDING
4. **Navigation wiring** - PENDING
5. **Testing & validation** - PENDING

---

**Status:** Backend infrastructure ready, now implementing ViewModel & UI layers.

