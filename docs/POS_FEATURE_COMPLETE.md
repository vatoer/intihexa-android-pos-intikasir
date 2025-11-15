# POS Feature - Complete Implementation Guide

## ✅ Status: COMPLETE & BUILD SUCCESS & INTEGRATED

Build Status: **BUILD SUCCESSFUL in 42s**

---

## 🔄 UPDATE: Navigation & Settings Integration

### PPN Management
- **Sebelumnya**: Input manual di POS screen
- **Sekarang**: Diatur di **Pengaturan Toko** (Settings)
- **Database**: `StoreSettingsEntity.taxPercentage`
- **Auto-load**: Reactive via Flow dari settings
- **UI**: Read-only info di POS: "PPN: 11% (diatur di Pengaturan Toko)"

### Navigation
```kotlin
// Routes
Home → Kasir (HomeRoutes.CASHIER) → PosScreen
Home → Pengaturan (HomeRoutes.SETTINGS) → StoreSettingsScreen

// Files
- feature/pos/navigation/PosRoutes.kt ✅
- feature/pos/navigation/PosNavGraph.kt ✅
- HomeNavGraph.kt (updated) ✅
```

### How to Set PPN
```
1. Home → Pengaturan
2. Toggle "Aktifkan PPN"
3. Input persentase (misal: 11)
4. Simpan
5. Buka POS → PPN auto-loaded
```

**Lihat detail lengkap di:** `POS_INTEGRATION_COMPLETE.md`

---

## 📋 Fitur yang Sudah Diimplementasikan

### 1. ✅ Dual Checkout Buttons (Simpan & Bayar)
**Tombol Simpan (Draft)**
- Menyimpan transaksi dengan status `PENDING`
- **TIDAK** mengurangi stok produk
- Cocok untuk transaksi yang belum dibayar/ditunda
- Cart dikosongkan setelah simpan
- Menampilkan pesan sukses: "Draft transaksi berhasil disimpan"

**Tombol Bayar (Complete)**
- Menyimpan transaksi dengan status `COMPLETED`
- **Mengurangi stok produk** secara otomatis
- Dialog konfirmasi pembayaran muncul
- Validasi uang diterima (untuk metode CASH)
- Cart dikosongkan setelah bayar
- Menampilkan pesan sukses: "Transaksi berhasil disimpan"

### 2. ✅ Diskon Per Item
**Cara Pakai:**
1. Tambahkan produk ke keranjang
2. Klik icon titik tiga (⋮) di sebelah Stepper
3. Dialog "Diskon Item" muncul
4. Masukkan nominal diskon (Rp)
5. Diskon ditampilkan di bawah harga produk
6. Subtotal otomatis dikurangi diskon

**Validasi:**
- Diskon max = harga × qty
- Diskon min = 0
- Tersimpan di `CartItem.itemDiscount`
- Dikirim ke database via `TransactionItemEntity.discount`

### 3. ✅ Validasi Pembayaran
**Metode CASH:**
- Input "Tunai Diterima" wajib
- Jika uang < total → error "Uang kurang!"
- Field menjadi merah (isError = true)
- Tombol Konfirmasi tetap bisa diklik tapi transaksi tidak disimpan
- Menampilkan kembalian real-time

**Metode Non-CASH (QRIS, Card, Transfer):**
- Input "Tunai Diterima" disembunyikan
- Langsung simpan dengan nilai cashReceived = total

### 4. ✅ Diskon Global & PPN
**Diskon Global:**
- Input Rp di atas list produk
- Dikurangi dari subtotal
- Max = subtotal (tidak bisa lebih)
- Rumus: `total = (subtotal - diskonGlobal) + tax`

**PPN (Pajak):**
- Input persen (%) di atas list produk
- Maksimal 25%
- Dihitung dari: `(subtotal - diskon) × taxRate`
- Rumus: `tax = taxableBase × taxRate`

### 5. ✅ Metode Pembayaran
Pilihan via dropdown:
- **CASH** (Tunai)
- **QRIS**
- **CARD** (Kartu Debit/Kredit)
- **TRANSFER** (Transfer Bank)

Tersimpan di `TransactionEntity.paymentMethod`

### 6. ✅ Loading States & Error Handling
**Loading:**
- Tombol Simpan/Bayar menampilkan CircularProgressIndicator saat `isSaving = true`
- Tombol di-disable saat loading

**Error/Success Feedback:**
- Snackbar untuk error pembayaran
- Snackbar untuk pesan sukses
- Auto-dismiss setelah ditampilkan

### 7. ✅ Perbaikan Deprecation
- `Divider()` → `HorizontalDivider()`
- `menuAnchor()` → `menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)`
- Build clean tanpa error (hanya warning hiltViewModel import yang aman)

---

## 🏗️ Arsitektur & Database

### Entity Structure
```kotlin
TransactionEntity {
    id: String (UUID)
    transactionNumber: String  // INV-YYYYMMDD-####
    cashierId: String
    cashierName: String
    paymentMethod: PaymentMethod
    subtotal: Double
    tax: Double
    discount: Double (global)
    total: Double
    cashReceived: Double
    cashChange: Double
    status: TransactionStatus (PENDING/COMPLETED)
    notes: String?
    createdAt: Long
    updatedAt: Long
}

TransactionItemEntity {
    id: String (UUID)
    transactionId: String (FK)
    productId: String (FK)
    productName: String (snapshot)
    productPrice: Double (snapshot)
    productSku: String?
    quantity: Int
    unitPrice: Double
    discount: Double (per item)
    subtotal: Double
    createdAt: Long
}
```

### Repository Methods
```kotlin
interface TransactionRepository {
    // Bayar (COMPLETED + stock decrement)
    suspend fun createTransaction(...): String
    
    // Simpan (PENDING + no stock change)
    suspend fun createDraftTransaction(...): String
}
```

### ViewModel State
```kotlin
data class UiState(
    val products: List<Product>
    val cart: Map<String, CartItem>
    val taxRate: Double
    val discountGlobal: Double
    val paymentMethod: PaymentMethod
    val isSaving: Boolean
    val paymentError: String?
    val successMessage: String?
    // ... computed properties
    val cartItems: List<CartItem>
    val totalQuantity: Int
    val subtotal: Double
    val tax: Double
    val total: Double
)
```

---

## 💰 Perhitungan Total (Step by Step)

```kotlin
// 1. Subtotal (sum semua item setelah diskon per item)
subtotal = cartItems.sumOf { it.subtotal }
// it.subtotal = (price × quantity) - itemDiscount

// 2. Diskon Global
discount = discountGlobal.coerceAtMost(subtotal)

// 3. Taxable Base
taxableBase = subtotal - discount

// 4. PPN/Tax
tax = taxableBase × taxRate

// 5. Total Akhir
total = taxableBase + tax

// Untuk CASH:
cashReceived = input dari user
cashChange = cashReceived - total (jika >= 0)
```

---

## 📱 UI Layout (Portrait)

```
┌──────────────────────────────────┐
│  CartSummary (non-scroll)        │
│  - Item count                     │
│  - Subtotal, Pajak, Total         │
│  - Chip list produk               │
├──────────────────────────────────┤
│  [Diskon Rp] [PPN %] [Metode ▼]  │ ← Kontrol
├──────────────────────────────────┤
│  [🔍 Cari produk...]              │
├──────────────────────────────────┤
│  ┌────────────────────────────┐  │
│  │ Produk 1    [⋮] [- 2 +]   │  │ ← Scrollable
│  │ Diskon: Rp 5.000           │  │
│  ├────────────────────────────┤  │
│  │ Produk 2    [Tambah]       │  │
│  └────────────────────────────┘  │
├──────────────────────────────────┤
│  [Simpan]          [Bayar]       │ ← Bottom Bar
└──────────────────────────────────┘
```

---

## 🎯 User Flow

### Flow 1: Simpan Draft
```
1. Tambah produk ke cart
2. Atur diskon (opsional)
3. Atur PPN (opsional)
4. Pilih metode pembayaran
5. Klik "Simpan"
   ↓
   - Transaksi status PENDING tersimpan
   - Stok TIDAK berkurang
   - Cart dikosongkan
   - Snackbar: "Draft transaksi berhasil disimpan"
```

### Flow 2: Bayar Langsung (CASH)
```
1. Tambah produk ke cart
2. Atur diskon/PPN (opsional)
3. Klik "Bayar"
   ↓
   Dialog Pembayaran
4. Input "Tunai Diterima"
5. Lihat kembalian
6. Klik "Konfirmasi"
   ↓
   - Validasi uang cukup
   - Transaksi status COMPLETED
   - Stok berkurang
   - Cart dikosongkan
   - Snackbar: "Transaksi berhasil disimpan"
```

### Flow 3: Set Diskon Per Item
```
1. Produk sudah di cart
2. Klik icon ⋮ di item
   ↓
   Dialog "Diskon Item"
3. Input nominal (Rp)
4. Klik "Simpan"
   ↓
   - Diskon tersimpan
   - Subtotal item dikurangi diskon
   - Total keseluruhan update
```

---

## 🔧 Component Structure

```
feature/pos/
├── domain/
│   ├── model/
│   │   └── CartItem.kt
│   ├── TransactionRepository.kt
│   └── di/
│       └── PosModule.kt
├── data/
│   └── TransactionRepositoryImpl.kt
└── ui/
    ├── PosScreen.kt
    ├── PosViewModel.kt
    └── components/
        ├── CartSummary.kt
        ├── PosProductItem.kt (+ dialog diskon)
        └── PayButton.kt
```

---

## 🧪 Testing Checklist

### Basic Operations
- [ ] Tambah produk ke cart
- [ ] Ubah quantity via Stepper
- [ ] Hapus item (qty = 0 atau icon delete)
- [ ] Search produk by name/barcode
- [ ] Clear cart (tombol "Kosongkan")

### Diskon
- [ ] Set diskon global (max = subtotal)
- [ ] Set diskon per item (dialog ⋮)
- [ ] Lihat diskon tercermin di subtotal

### PPN
- [ ] Set PPN 0-25%
- [ ] Lihat tax dihitung dari (subtotal - diskon)

### Metode Pembayaran
- [ ] Pilih CASH → input tunai muncul
- [ ] Pilih QRIS → input tunai hilang
- [ ] Validasi tunai < total → error

### Simpan vs Bayar
- [ ] Simpan → status PENDING, stok tidak kurang
- [ ] Bayar CASH → validasi tunai, stok kurang
- [ ] Bayar QRIS → langsung simpan, stok kurang

### Edge Cases
- [ ] Qty > stock → tidak bisa tambah
- [ ] Diskon item > (price × qty) → dibatasi
- [ ] PPN > 25% → dibatasi
- [ ] Cart kosong → tombol disabled
- [ ] Saat saving → tombol disabled + loading

---

## 🚀 Next Features (Siap Dikembangkan)

### 1. Refund Transaction
```kotlin
// Repository method
suspend fun refundTransaction(
    transactionId: String,
    refundedBy: String,
    reason: String?
): String

// Implementation:
// - Update status → REFUNDED
// - Restock products (increment stock)
// - Save refund metadata
```

### 2. Sales Report
```kotlin
// Use cases ready:
suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double
suspend fun getTransactionCount(startDate: Long, endDate: Long): Int
suspend fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int): List<TransactionItem>
```

### 3. Persist Cart (DataStore/SharedPreferences)
```kotlin
// Auto-save cart on change
// Restore cart on app restart
suspend fun saveCart(cart: Map<String, CartItem>)
suspend fun loadCart(): Map<String, CartItem>
```

### 4. Print Receipt (Thermal Printer)
```kotlin
// Generate receipt string
fun generateReceipt(transaction: Transaction): String
// Support ESC/POS commands
// Bluetooth/USB printer integration
```

### 5. QRIS Integration
```kotlin
// Generate QRIS payload
suspend fun generateQRIS(amount: Double): String
// Check payment status
suspend fun checkQRISPayment(transactionId: String): PaymentStatus
```

---

## 📊 Database Queries (Ready to Use)

```kotlin
// TransactionDao
fun getAllTransactions(): Flow<List<TransactionEntity>>
fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
fun getTransactionsByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>
suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double?
suspend fun getTransactionCount(startDate: Long, endDate: Long): Int

// TransactionItemDao
fun getItemsByTransaction(transactionId: String): Flow<List<TransactionItemEntity>>
suspend fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int = 10): List<TransactionItemEntity>

// ProductDao
suspend fun getProductsByIds(ids: List<String>): List<ProductEntity>
suspend fun decrementStock(productId: String, qty: Int)
```

---

## 🎨 Best Practices Implemented

### ✅ Jetpack Compose
- Single source of truth (UiState)
- Unidirectional data flow
- Stateless composables
- rememberCoroutineScope for suspend calls
- LaunchedEffect for side effects
- Snackbar for user feedback

### ✅ Hilt/DI
- Constructor injection (ViewModel)
- @Binds for repository
- @Singleton scope
- Module organization

### ✅ Room/Database
- Transactional operations
- Foreign keys & cascading
- Snapshot data (product name/price)
- Indexed queries
- Flow for reactive updates

### ✅ Error Handling
- Try-catch di repository
- Error state di ViewModel
- User feedback via Snackbar
- Validation sebelum simpan

### ✅ Code Quality
- Modular structure
- Separation of concerns
- Kotlin best practices
- Type-safe navigation ready
- Documentation inline

---

## 📝 Summary

**Total Implementasi:**
- 2 modes checkout (Simpan Draft + Bayar Complete)
- Diskon per item + diskon global
- PPN/Tax configurable
- 4 metode pembayaran
- Validasi pembayaran CASH
- Loading states & error handling
- Stock management automatic
- Transaction numbering (INV-YYYYMMDD-####)
- Snapsho product data
- Clean deprecation fixes

**Build Status:** ✅ SUCCESS  
**Errors:** 0  
**Warnings:** 1 (hiltViewModel import - safe, cosmetic)

**Siap Production:** Ya, dengan catatan:
- Tambahkan user authentication real (currentUserId)
- Tambahkan print receipt jika perlu
- Tambahkan refund flow jika perlu
- Tambahkan sync ke backend jika perlu

---

## 🔗 Integration Points

### Navigation (siap disambung)
```kotlin
composable("pos") { 
    PosScreen(
        onPay = { total -> 
            // Navigate ke receipt/success
            navController.navigate("receipt/${viewModel.uiState.value.lastSavedTransactionId}")
        }
    )
}
```

### Auth Integration
```kotlin
// Ganti hardcoded cashier
val currentUser by authViewModel.currentUser.collectAsState()
viewModel.finalizeTransaction(
    cashierId = currentUser?.id ?: "",
    cashierName = currentUser?.name ?: ""
)
```

### Receipt Screen (next)
```kotlin
@Composable
fun ReceiptScreen(transactionId: String) {
    // Load transaction + items
    // Display receipt
    // Print button
    // Share button
    // Back to POS
}
```

---

Dokumentasi ini siap digunakan sebagai reference lengkap untuk fitur POS! 🎉
