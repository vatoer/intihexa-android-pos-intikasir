package id.stargan.intikasir.feature.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.product.domain.usecase.GetAllProductsUseCase
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.feature.settings.domain.usecase.GetStoreSettingsUseCase
import id.stargan.intikasir.feature.pos.domain.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PosViewModel - Reactive Transaction Based
 *
 * Menggunakan database sebagai single source of truth
 * Setiap perubahan cart langsung disimpan ke database
 */
@HiltViewModel
class PosViewModelReactive @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val transactionRepository: TransactionRepository,
    private val getStoreSettingsUseCase: GetStoreSettingsUseCase
) : ViewModel() {

    data class UiState(
        // Transaction data (from DB)
        val transactionId: String? = null,
        val transaction: TransactionEntity? = null,
        val transactionItems: List<TransactionItemEntity> = emptyList(),

        // Product catalog
        val products: List<id.stargan.intikasir.domain.model.Product> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,

        // Tax rate from settings
        val taxRate: Double = 0.0,

        // Computed values
        val subtotal: Double = 0.0,
        val tax: Double = 0.0,
        val total: Double = 0.0,

        // UI state
        val isSaving: Boolean = false,
        val successMessage: String? = null,
        val errorMessage: String? = null
    ) {
        val totalQuantity: Int get() = transactionItems.sumOf { it.quantity }
        val hasItems: Boolean get() = transactionItems.isNotEmpty()
        val paymentMethod: PaymentMethod get() = transaction?.paymentMethod ?: PaymentMethod.CASH
        val globalDiscount: Double get() = transaction?.discount ?: 0.0
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadTaxFromSettings()
    }

    /**
     * Initialize or load transaction
     * Called saat screen pertama kali dibuka
     */
    fun initializeTransaction(cashierId: String, cashierName: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }

                // Create empty draft transaction
                val transactionId = transactionRepository.createEmptyDraft(cashierId, cashierName)

                // Load the transaction and start observing
                loadTransaction(transactionId)

                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Gagal membuat transaksi: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load existing transaction by ID
     * Observe reactive changes from database
     */
    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(transactionId = transactionId, isLoading = true) }

                // Observe transaction changes
                transactionRepository.getTransactionById(transactionId)
                    .combine(transactionRepository.getTransactionItems(transactionId)) { transaction, items ->
                        Pair(transaction, items)
                    }
                    .collect { (transaction, items) ->
                        if (transaction != null) {
                            val subtotal = items.sumOf { it.subtotal }
                            val tax = subtotal * _uiState.value.taxRate
                            val discount = transaction.discount
                            val total = subtotal + tax - discount

                            _uiState.update {
                                it.copy(
                                    transaction = transaction,
                                    transactionItems = items,
                                    subtotal = subtotal,
                                    tax = tax,
                                    total = total,
                                    isLoading = false
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat transaksi: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Add or increment product in cart
     * Auto-save to database
     */
    fun addOrIncrement(productId: String) {
        val transactionId = _uiState.value.transactionId ?: return

        viewModelScope.launch {
            try {
                val currentItems = _uiState.value.transactionItems
                val existingItem = currentItems.find { it.productId == productId }
                val product = _uiState.value.products.find { it.id == productId } ?: return@launch

                val newQty = (existingItem?.quantity ?: 0) + 1
                if (newQty > product.stock) return@launch // Exceed stock

                // Update items map
                val updatedItems = if (existingItem != null) {
                    currentItems.map {
                        if (it.productId == productId) it.copy(quantity = newQty)
                        else it
                    }
                } else {
                    currentItems + TransactionItemEntity(
                        transactionId = transactionId,
                        productId = productId,
                        productName = product.name,
                        productPrice = product.price,
                        productSku = product.sku,
                        quantity = newQty,
                        unitPrice = product.price,
                        discount = 0.0,
                        subtotal = product.price * newQty
                    )
                }

                // Save to database
                saveCartToDatabase(transactionId, updatedItems)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menambah produk: ${e.message}") }
            }
        }
    }

    /**
     * Set quantity for a product
     * Auto-save to database
     */
    fun setQuantity(productId: String, quantity: Int) {
        val transactionId = _uiState.value.transactionId ?: return

        viewModelScope.launch {
            try {
                val currentItems = _uiState.value.transactionItems
                val product = _uiState.value.products.find { it.id == productId } ?: return@launch

                if (quantity > product.stock) return@launch

                val updatedItems = if (quantity == 0) {
                    currentItems.filter { it.productId != productId }
                } else {
                    currentItems.map { item ->
                        if (item.productId == productId) {
                            // Clamp discount so it never exceeds max price * qty
                            val maxDiscount = product.price * quantity
                            val safeDiscount = item.discount.coerceIn(0.0, maxDiscount)
                            item.copy(
                                quantity = quantity,
                                discount = safeDiscount,
                                subtotal = (product.price * quantity) - safeDiscount
                            )
                        } else item
                    }
                }

                saveCartToDatabase(transactionId, updatedItems)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengubah jumlah: ${e.message}") }
            }
        }
    }

    /**
     * Set item discount
     * Auto-save to database
     */
    fun setItemDiscount(productId: String, discountAmount: Double) {
        val transactionId = _uiState.value.transactionId ?: return

        viewModelScope.launch {
            try {
                val currentItems = _uiState.value.transactionItems
                val item = currentItems.find { it.productId == productId } ?: return@launch
                val product = _uiState.value.products.find { it.id == productId } ?: return@launch

                val maxDiscount = product.price * item.quantity
                val safeDiscount = discountAmount.coerceIn(0.0, maxDiscount)

                val updatedItems = currentItems.map {
                    if (it.productId == productId) {
                        it.copy(
                            discount = safeDiscount,
                            subtotal = (product.price * it.quantity) - safeDiscount
                        )
                    } else it
                }

                saveCartToDatabase(transactionId, updatedItems)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengatur diskon: ${e.message}") }
            }
        }
    }

    /**
     * Clear entire cart
     */
    fun clearCart() {
        val transactionId = _uiState.value.transactionId ?: return

        viewModelScope.launch {
            try {
                saveCartToDatabase(transactionId, emptyList())
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengosongkan keranjang: ${e.message}") }
            }
        }
    }

    /**
     * Set global discount
     */
    fun setGlobalDiscount(amount: Double) {
        val transactionId = _uiState.value.transactionId ?: return

        viewModelScope.launch {
            try {
                val safeAmount = amount.coerceAtLeast(0.0)
                transactionRepository.updateTransactionPayment(
                    transactionId,
                    _uiState.value.paymentMethod,
                    safeAmount
                )
                // Will auto-update via Flow observer
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengatur diskon: ${e.message}") }
            }
        }
    }

    /**
     * Set payment method
     */
    fun setPaymentMethod(method: PaymentMethod) {
        val transactionId = _uiState.value.transactionId ?: return

        viewModelScope.launch {
            try {
                transactionRepository.updateTransactionPayment(
                    transactionId,
                    method,
                    _uiState.value.globalDiscount
                )
                // Will auto-update via Flow observer
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengatur metode pembayaran: ${e.message}") }
            }
        }
    }

    /**
     * Finalize transaction (checkout)
     */
    suspend fun finalizeTransaction(cashReceived: Double?, notes: String?) {
        val transactionId = _uiState.value.transactionId ?: return

        try {
            _uiState.update { it.copy(isSaving = true) }

            val received = cashReceived ?: _uiState.value.total
            val change = (received - _uiState.value.total).coerceAtLeast(0.0)

            transactionRepository.finalizeTransaction(
                transactionId,
                received,
                change,
                notes
            )

            _uiState.update {
                it.copy(
                    isSaving = false,
                    successMessage = "Transaksi berhasil diselesaikan"
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = "Gagal menyelesaikan transaksi: ${e.message}"
                )
            }
        }
    }

    /**
     * Complete transaction (mark as COMPLETED)
     */
    fun completeTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                transactionRepository.completeTransaction(transactionId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menyelesaikan transaksi: ${e.message}") }
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Private helper methods

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getAllProductsUseCase().collect { list ->
                    _uiState.update { current ->
                        current.copy(
                            products = list.filter { it.isActive },
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadTaxFromSettings() {
        viewModelScope.launch {
            getStoreSettingsUseCase().collect { settings ->
                settings?.let {
                    if (it.taxEnabled) {
                        _uiState.update { state ->
                            state.copy(taxRate = it.taxPercentage / 100.0)
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveCartToDatabase(transactionId: String, items: List<TransactionItemEntity>) {
        // Convert to format expected by repository
        val itemPairs = items.map { it.productId to it.quantity }
        val itemDiscounts = items.associate { it.productId to it.discount }

        // Save items
        transactionRepository.updateTransactionItems(transactionId, itemPairs, itemDiscounts)

        // Calculate and save totals
        val subtotal = items.sumOf { it.subtotal }
        val tax = subtotal * _uiState.value.taxRate
        val discount = _uiState.value.globalDiscount
        val total = subtotal + tax - discount

        transactionRepository.updateTransactionTotals(transactionId, subtotal, tax, discount, total)

        // Will auto-update via Flow observer, no need to manually update state
    }
}
