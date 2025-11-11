# Alur Logika Pembayaran (Payment Flow)

## Flow Diagram
```
User Click "BAYAR" 
    ↓
Validasi Keranjang
    ↓
Tampilkan Dialog Pembayaran
    ↓
User Pilih Metode Pembayaran & Input Jumlah
    ↓
Validasi Pembayaran
    ↓
[TRANSACTION] Mulai Database Transaction
    ↓
Simpan Transaction ke Database
    ↓
Simpan Transaction Items ke Database
    ↓
Update Stock Produk (jika tracking enabled)
    ↓
[COMMIT] Commit Database Transaction
    ↓
Generate Nomor Transaksi
    ↓
Cetak Struk (Bluetooth Printer)
    ↓
Tampilkan Success Dialog
    ↓
Reset Keranjang
```

---

## Detailed Step-by-Step Logic

### 1. Validasi Keranjang Belanja
**Trigger:** User click tombol "BAYAR"

**Validasi:**
- ✅ Keranjang tidak kosong
- ✅ Semua produk memiliki harga valid (> 0)
- ✅ Semua produk memiliki quantity valid (> 0)
- ✅ Cek stok produk (jika tracking enabled)

**Code Example:**
```kotlin
fun validateCart(cartItems: List<CartItem>): Boolean {
    if (cartItems.isEmpty()) {
        showError("Keranjang kosong")
        return false
    }
    
    cartItems.forEach { item ->
        if (item.quantity <= 0) {
            showError("Jumlah produk tidak valid")
            return false
        }
        
        // Check stock if tracking enabled
        val product = productRepository.getProductById(item.productId)
        if (product.trackStock && product.stock < item.quantity) {
            showError("Stok ${product.name} tidak mencukupi")
            return false
        }
    }
    
    return true
}
```

---

### 2. Tampilkan Dialog Pembayaran

**UI Components:**
- Radio buttons untuk pilih metode pembayaran (Tunai, QRIS, Kartu, Transfer)
- TextField untuk input jumlah uang (jika Tunai)
- Display total pembayaran
- Display kembalian (jika Tunai)
- Tombol "Proses Pembayaran"

**For Cash Payment:**
- Auto-calculate change: `cashChange = cashReceived - total`
- Validation: `cashReceived >= total`

---

### 3. Proses Pembayaran

#### 3.1 Hitung Total
```kotlin
// 1. Subtotal
val subtotal = cartItems.sumOf { it.subtotal }

// 2. Tax (dari Store Settings)
val taxPercentage = storeSettings.taxPercentage
val tax = if (storeSettings.taxEnabled) {
    subtotal * (taxPercentage / 100.0)
} else {
    0.0
}

// 3. Service Charge (dari Store Settings)
val servicePercentage = storeSettings.servicePercentage
val service = if (storeSettings.serviceEnabled) {
    subtotal * (servicePercentage / 100.0)
} else {
    0.0
}

// 4. Discount (optional)
val discount = 0.0 // Bisa dikembangkan untuk promo

// 5. Total
val total = subtotal + tax + service - discount
```

#### 3.2 Generate Transaction Number
Format: `INV-YYYYMMDD-XXXX`

```kotlin
fun generateTransactionNumber(): String {
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val dateString = dateFormat.format(Date())
    val prefix = "INV-$dateString-"
    
    // Get last transaction number for today
    val lastNumber = transactionDao.getLastTransactionNumber(prefix)
    
    val sequence = if (lastNumber != null) {
        val lastSeq = lastNumber.substringAfterLast("-").toIntOrNull() ?: 0
        lastSeq + 1
    } else {
        1
    }
    
    return "$prefix${sequence.toString().padStart(4, '0')}"
}

// Example: INV-20251111-0001, INV-20251111-0002, ...
```

---

### 4. Simpan ke Database (Room Transaction)

**IMPORTANT:** Gunakan Room Database Transaction untuk memastikan atomicity

```kotlin
suspend fun processPayment(
    cartItems: List<CartItem>,
    paymentMethod: PaymentMethod,
    cashReceived: Double = 0.0,
    currentUser: User,
    storeSettings: StoreSettings
): Result<String> = withContext(Dispatchers.IO) {
    try {
        database.withTransaction {
            // 1. Create Transaction
            val transactionNumber = generateTransactionNumber()
            val transactionId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            
            val subtotal = cartItems.sumOf { it.subtotal }
            val tax = if (storeSettings.taxEnabled) {
                subtotal * (storeSettings.taxPercentage / 100.0)
            } else 0.0
            
            val service = if (storeSettings.serviceEnabled) {
                subtotal * (storeSettings.servicePercentage / 100.0)
            } else 0.0
            
            val total = subtotal + tax + service
            val cashChange = if (paymentMethod == PaymentMethod.CASH) {
                cashReceived - total
            } else 0.0
            
            val transaction = TransactionEntity(
                id = transactionId,
                transactionNumber = transactionNumber,
                transactionDate = now,
                cashierId = currentUser.id,
                cashierName = currentUser.name,
                paymentMethod = paymentMethod,
                subtotal = subtotal,
                tax = tax,
                service = service,
                discount = 0.0,
                total = total,
                cashReceived = cashReceived,
                cashChange = cashChange,
                status = TransactionStatus.COMPLETED,
                createdAt = now,
                updatedAt = now
            )
            
            // 2. Insert Transaction
            transactionDao.insertTransaction(transaction)
            
            // 3. Insert Transaction Items
            val transactionItems = cartItems.map { cartItem ->
                TransactionItemEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionId,
                    productId = cartItem.productId,
                    productName = cartItem.productName,
                    productPrice = cartItem.productPrice,
                    productSku = cartItem.productSku,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.productPrice,
                    discount = cartItem.discount,
                    subtotal = cartItem.subtotal,
                    createdAt = now
                )
            }
            transactionItemDao.insertItems(transactionItems)
            
            // 4. Update Product Stock
            cartItems.forEach { cartItem ->
                val product = productDao.getProductById(cartItem.productId)
                if (product?.trackStock == true) {
                    productDao.decreaseStock(
                        productId = cartItem.productId,
                        quantity = cartItem.quantity,
                        timestamp = now
                    )
                }
            }
            
            // 5. Return transaction number for success
            transactionNumber
        }
        
        Result.success(transactionNumber)
        
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

### 5. Cetak Struk (Receipt Printing)

**Printer:** Thermal Bluetooth Printer (ESC/POS Protocol)

```kotlin
fun printReceipt(
    transaction: Transaction,
    items: List<TransactionItem>,
    storeSettings: StoreSettings
) {
    val printer = BluetoothPrinter(storeSettings.printerAddress)
    
    if (!printer.isConnected()) {
        showError("Printer tidak terhubung")
        return
    }
    
    printer.apply {
        // Header
        printCenter(storeSettings.storeName, bold = true, size = 2)
        printCenter(storeSettings.storeAddress)
        printCenter(storeSettings.storePhone)
        printLine()
        
        // Transaction Info
        printLeft("No: ${transaction.transactionNumber}")
        printLeft("Tanggal: ${formatDate(transaction.transactionDate)}")
        printLeft("Kasir: ${transaction.cashierName}")
        printLine()
        
        // Items
        items.forEach { item ->
            printLeft("${item.productName}")
            printLeftRight(
                "${item.quantity} x ${formatCurrency(item.unitPrice)}",
                formatCurrency(item.subtotal)
            )
        }
        printLine()
        
        // Summary
        printLeftRight("Subtotal", formatCurrency(transaction.subtotal))
        
        if (transaction.tax > 0) {
            printLeftRight(
                "${storeSettings.taxName} (${storeSettings.taxPercentage}%)",
                formatCurrency(transaction.tax)
            )
        }
        
        if (transaction.service > 0) {
            printLeftRight(
                "${storeSettings.serviceName} (${storeSettings.servicePercentage}%)",
                formatCurrency(transaction.service)
            )
        }
        
        printLine()
        printLeftRight("TOTAL", formatCurrency(transaction.total), bold = true)
        
        // Payment info (for cash)
        if (transaction.paymentMethod == PaymentMethod.CASH) {
            printLeftRight("Tunai", formatCurrency(transaction.cashReceived))
            printLeftRight("Kembalian", formatCurrency(transaction.cashChange))
        } else {
            printLeft("Metode: ${transaction.paymentMethod.displayName()}")
        }
        
        printLine()
        
        // Footer
        if (storeSettings.receiptFooter != null) {
            printCenter(storeSettings.receiptFooter)
        }
        printCenter("Terima Kasih")
        
        // Cut paper
        feed(3)
        cut()
    }
}
```

---

### 6. Error Handling

**Possible Errors:**
1. **Cart Validation Failed** → Show error, stay on POS screen
2. **Stock Not Available** → Show error, highlight product
3. **Payment Validation Failed** → Show error in payment dialog
4. **Database Error** → Rollback transaction, show error
5. **Printer Error** → Save transaction but show printer error (optional retry)

**Error Recovery:**
- All database operations are wrapped in a transaction
- If any step fails, entire transaction is rolled back
- User can retry payment without losing data

---

### 7. Success Flow

After successful payment:
1. ✅ Close payment dialog
2. ✅ Show success dialog with transaction number
3. ✅ Print receipt (if printer connected)
4. ✅ Clear cart
5. ✅ Reset form
6. ✅ Optional: Show option to print again or view receipt

---

## State Management (ViewModel)

```kotlin
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val storeSettingsRepository: StoreSettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<PosUiEvent>()
    val uiEvent: SharedFlow<PosUiEvent> = _uiEvent.asSharedFlow()
    
    fun onCheckoutClick() {
        if (validateCart()) {
            _uiState.update { it.copy(showPaymentDialog = true) }
        }
    }
    
    fun onPaymentConfirm(
        paymentMethod: PaymentMethod,
        cashReceived: Double = 0.0
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = transactionRepository.processPayment(
                cartItems = _uiState.value.cartItems,
                paymentMethod = paymentMethod,
                cashReceived = cashReceived,
                currentUser = getCurrentUser(),
                storeSettings = getStoreSettings()
            )
            
            result.onSuccess { transactionNumber ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showPaymentDialog = false,
                        showSuccessDialog = true,
                        transactionNumber = transactionNumber,
                        cartItems = emptyList()
                    )
                }
                _uiEvent.emit(PosUiEvent.PaymentCompleted)
            }
            
            result.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
                _uiEvent.emit(PosUiEvent.ShowError(error.message ?: "Terjadi kesalahan"))
            }
        }
    }
    
    private fun validateCart(): Boolean {
        // Validation logic
        return true
    }
}
```

---

## Testing Checklist

- [ ] Validate empty cart
- [ ] Validate insufficient stock
- [ ] Calculate tax and service correctly
- [ ] Generate unique transaction numbers
- [ ] Save transaction to database
- [ ] Update stock correctly
- [ ] Handle database transaction rollback on error
- [ ] Print receipt successfully
- [ ] Handle printer connection error
- [ ] Clear cart after successful payment
- [ ] Cash payment calculation (change)
- [ ] Non-cash payment flow

---

## Future Enhancements

1. **Discount System** - Apply discounts per item or per transaction
2. **Multiple Payment Methods** - Split payment (cash + card)
3. **Customer Data** - Link transaction to customer
4. **Receipt Email/SMS** - Send digital receipt
5. **Void/Refund** - Cancel or refund transactions
6. **Tips** - Add tip functionality for service industry
7. **Queue System** - Multiple pending orders
8. **Kitchen Display** - Send orders to kitchen (for restaurants)

