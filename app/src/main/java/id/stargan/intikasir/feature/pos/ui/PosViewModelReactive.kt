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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PosViewModel - In-Memory First Transaction Handling
 *
 * Perubahan cart disimpan di memori terlebih dahulu (state). Persist ke database hanya saat
 * navigasi (Cart / Pembayaran / Back) atau auto-save periode. Mengurangi flicker & I/O.
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
        val errorMessage: String? = null,

        // Unsaved changes flag
        val hasUnsavedChanges: Boolean = false
    ) {
        val totalQuantity: Int get() = transactionItems.sumOf { it.quantity }
        val hasItems: Boolean get() = transactionItems.isNotEmpty()
        val paymentMethod: PaymentMethod get() = transaction?.paymentMethod ?: PaymentMethod.CASH
        val globalDiscount: Double get() = transaction?.discount ?: 0.0
        val itemDiscountTotal: Double get() = transactionItems.sumOf { it.discount }
        val grossSubtotal: Double get() = transactionItems.sumOf { it.unitPrice * it.quantity }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        loadProducts()
        loadTaxFromSettings()
        startAutoSave()
    }

    private fun recalcTotals(state: UiState = _uiState.value): UiState {
        val gross = state.grossSubtotal
        val itemDiscount = state.itemDiscountTotal
        val netSubtotal = gross - itemDiscount
        val taxAmount = netSubtotal * state.taxRate
        val globalDiscount = state.globalDiscount
        val total = netSubtotal + taxAmount - globalDiscount
        return state.copy(
            subtotal = netSubtotal,
            tax = taxAmount,
            total = total
        )
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
                val transaction = transactionRepository.getTransactionById(transactionId).first()
                val items = transactionRepository.getTransactionItems(transactionId).first()
                _uiState.update {
                    recalcTotals(
                        it.copy(
                            transactionId = transactionId,
                            transaction = transaction,
                            transactionItems = items,
                            isSaving = false,
                            isLoading = false,
                            hasUnsavedChanges = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Gagal membuat transaksi: ${e.message}") }
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

                val transaction = transactionRepository.getTransactionById(transactionId).first()
                val items = transactionRepository.getTransactionItems(transactionId).first()
                _uiState.update {
                    recalcTotals(
                        it.copy(
                            transactionId = transactionId,
                            transaction = transaction,
                            transactionItems = items,
                            isLoading = false,
                            hasUnsavedChanges = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat transaksi: ${e.message}") }
            }
        }
    }

    /**
     * Add or increment product in cart
     * Auto-save to database
     */
    fun addOrIncrement(productId: String) {
        val current = _uiState.value
        val product = current.products.find { it.id == productId } ?: return
        val existingItem = current.transactionItems.find { it.productId == productId }
        val newQty = (existingItem?.quantity ?: 0) + 1
        if (newQty > product.stock) return

        val updatedItems = if (existingItem != null) {
            current.transactionItems.map {
                if (it.productId == productId) {
                    // keep per-unit discount the same
                    val perUnitDiscount = if (it.quantity > 0) it.discount / it.quantity else 0.0
                    val newTotalDiscount = perUnitDiscount * newQty
                    it.copy(
                        quantity = newQty,
                        discount = newTotalDiscount,
                        subtotal = (product.price * newQty) - newTotalDiscount
                    )
                } else it
            }
        } else {
            current.transactionItems + TransactionItemEntity(
                transactionId = current.transactionId!!,
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

        _uiState.update { recalcTotals(it.copy(transactionItems = updatedItems, hasUnsavedChanges = true)) }
    }

    /**
     * Set quantity for a product
     * Auto-save to database
     */
    fun setQuantity(productId: String, quantity: Int) {
        val current = _uiState.value
        val product = current.products.find { it.id == productId } ?: return
        if (quantity > product.stock) return

        val updatedItems = if (quantity == 0) {
            current.transactionItems.filter { it.productId != productId }
        } else {
            current.transactionItems.map { item ->
                if (item.productId == productId) {
                    val perUnitDiscount = if (item.quantity > 0) item.discount / item.quantity else 0.0
                    val maxPerUnitDiscount = product.price
                    val safePerUnitDiscount = perUnitDiscount.coerceIn(0.0, maxPerUnitDiscount)
                    val newTotalDiscount = safePerUnitDiscount * quantity
                    item.copy(
                        quantity = quantity,
                        discount = newTotalDiscount,
                        subtotal = (product.price * quantity) - newTotalDiscount
                    )
                } else item
            }
        }

        _uiState.update { recalcTotals(it.copy(transactionItems = updatedItems, hasUnsavedChanges = true)) }
    }

    /**
     * Set item discount
     * Auto-save to database
     */
    fun setItemDiscount(productId: String, discountPerUnit: Double) {
        val current = _uiState.value
        val item = current.transactionItems.find { it.productId == productId } ?: return
        val product = current.products.find { it.id == productId } ?: return

        val maxPerUnitDiscount = product.price
        val safePerUnitDiscount = discountPerUnit.coerceIn(0.0, maxPerUnitDiscount)
        val newTotalDiscount = safePerUnitDiscount * item.quantity

        val updatedItems = current.transactionItems.map {
            if (it.productId == productId) {
                it.copy(
                    discount = newTotalDiscount,
                    subtotal = (product.price * it.quantity) - newTotalDiscount
                )
            } else it
        }

        _uiState.update { recalcTotals(it.copy(transactionItems = updatedItems, hasUnsavedChanges = true)) }
    }

    /**
     * Clear entire cart
     */
    fun clearCart() {
        if (_uiState.value.transactionId == null) return
        _uiState.update { recalcTotals(it.copy(transactionItems = emptyList(), hasUnsavedChanges = true)) }
    }

    /**
     * Set global discount
     */
    fun setGlobalDiscount(amount: Double) {
        val safeAmount = amount.coerceAtLeast(0.0)
        _uiState.update {
            val updatedTx = it.transaction?.copy(discount = safeAmount) ?: it.transaction
            recalcTotals(it.copy(transaction = updatedTx, hasUnsavedChanges = true))
        }
    }

    /**
     * Set payment method
     */
    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.update {
            val updatedTx = it.transaction?.copy(paymentMethod = method) ?: it.transaction
            it.copy(transaction = updatedTx, hasUnsavedChanges = true)
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

    private fun startAutoSave() {
        autoSaveJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30_000)
                if (_uiState.value.hasUnsavedChanges) {
                    saveToDatabase()
                }
            }
        }
    }

    suspend fun saveToDatabase(): Boolean {
        val state = _uiState.value
        val transactionId = state.transactionId ?: return false
        return try {
            _uiState.update { it.copy(isSaving = true) }

            val itemPairs = state.transactionItems.map { it.productId to it.quantity }
            val itemDiscounts = state.transactionItems.associate { it.productId to it.discount }
            transactionRepository.updateTransactionItems(transactionId, itemPairs, itemDiscounts)

            val subtotal = state.subtotal
            val tax = state.tax
            val discount = state.globalDiscount
            val total = state.total
            transactionRepository.updateTransactionTotals(transactionId, subtotal, tax, discount, total)

            state.transaction?.let {
                transactionRepository.updateTransactionPayment(transactionId, it.paymentMethod, it.discount)
            }

            _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
            true
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = "Gagal menyimpan transaksi: ${e.message}"
                )
            }
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
