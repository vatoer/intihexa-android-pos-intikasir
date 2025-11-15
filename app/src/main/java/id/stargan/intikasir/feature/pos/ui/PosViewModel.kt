package id.stargan.intikasir.feature.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.product.domain.usecase.GetAllProductsUseCase
import id.stargan.intikasir.feature.pos.domain.model.CartItem
import id.stargan.intikasir.feature.pos.domain.model.toCartItem
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.data.local.entity.TransactionStatus
import id.stargan.intikasir.feature.settings.domain.usecase.GetStoreSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import id.stargan.intikasir.feature.pos.domain.TransactionRepository

@HiltViewModel
class PosViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val transactionRepository: TransactionRepository,
    private val getStoreSettingsUseCase: GetStoreSettingsUseCase
) : ViewModel() {

    data class UiState(
        // Primary data
        val products: List<id.stargan.intikasir.domain.model.Product> = emptyList(),
        val cart: Map<String, CartItem> = emptyMap(),
        val searchQuery: String = "",
        val categoryId: String? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        // Pricing
        val taxRate: Double = 0.0,
        val discountGlobal: Double = 0.0,
        val paymentMethod: PaymentMethod = PaymentMethod.CASH,
        // Save state
        val isSaving: Boolean = false,
        val lastSavedTransactionId: String? = null,
        val paymentError: String? = null,
        val successMessage: String? = null,
        // Transaction details for receipt
        val lastTransactionNumber: String? = null,
        val lastCashReceived: Double = 0.0,
        val lastCashChange: Double = 0.0,
        val lastPaymentMethod: PaymentMethod = PaymentMethod.CASH
    ) {
        val cartItems: List<CartItem> get() = cart.values.toList()
        val totalQuantity: Int get() = cartItems.sumOf { it.quantity }
        val subtotal: Double get() = cartItems.sumOf { it.subtotal }
        val tax: Double get() = subtotal * taxRate
        val total: Double get() = subtotal + tax - discountGlobal

        override fun toString(): String = "cartItems=${cart.size} subtotal=$subtotal discountGlobal=$discountGlobal taxRate=$taxRate total=$total"
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadProducts()
        loadTaxFromSettings()
    }

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
                            state.copy(taxRate = it.taxPercentage / 100.0) // Convert percentage to rate
                        }
                    }
                }
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addOrIncrement(productId: String) {
        val product = _uiState.value.products.find { it.id == productId } ?: return
        _uiState.update { state ->
            val existing = state.cart[productId]
            val nextQty = (existing?.quantity ?: 0) + 1
            if (nextQty > product.stock) return@update state // exceed stock
            state.copy(cart = state.cart + (productId to product.toCartItem(nextQty)))
        }
    }

    fun setQuantity(productId: String, quantity: Int) {
        if (quantity < 0) { // delete sentinel
            remove(productId); return
        }
        val product = _uiState.value.products.find { it.id == productId } ?: return
        if (quantity > product.stock) return
        _uiState.update { state ->
            if (quantity == 0) state.copy(cart = state.cart - productId)
            else state.copy(cart = state.cart + (productId to product.toCartItem(quantity)))
        }
    }

    fun decrement(productId: String) {
        val existing = _uiState.value.cart[productId] ?: return
        val newQty = existing.quantity - 1
        setQuantity(productId, newQty)
    }

    fun remove(productId: String) {
        _uiState.update { it.copy(cart = it.cart - productId) }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = emptyMap()) }
    }

    fun setGlobalDiscount(amount: Double) {
        _uiState.update { it.copy(discountGlobal = amount.coerceAtLeast(0.0)) }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun setItemDiscount(productId: String, discountAmount: Double) {
        val existing = _uiState.value.cart[productId] ?: return
        val product = _uiState.value.products.find { it.id == productId } ?: return
        val maxDiscount = product.price * existing.quantity
        val safeDiscount = discountAmount.coerceIn(0.0, maxDiscount)

        _uiState.update { state ->
            state.copy(cart = state.cart + (productId to product.toCartItem(existing.quantity, safeDiscount)))
        }
    }

    fun clearPaymentError() {
        _uiState.update { it.copy(paymentError = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    suspend fun finalizeTransaction(cashierId: String, cashierName: String, cashReceived: Double? = null, notes: String? = null) {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return

        // Validate cash payment
        if (state.paymentMethod == PaymentMethod.CASH) {
            val received = cashReceived ?: 0.0
            val total = state.subtotal + (state.subtotal * state.taxRate) - state.discountGlobal
            if (received < total) {
                _uiState.update { it.copy(paymentError = "Uang yang diterima kurang dari total pembayaran") }
                return
            }
        }

        _uiState.update { it.copy(isSaving = true, paymentError = null) }
        try {
            val subtotal = state.subtotal
            val discount = state.discountGlobal.coerceAtMost(subtotal)
            val taxableBase = subtotal - discount
            val tax = taxableBase * state.taxRate
            val total = taxableBase + tax
            val received = if (state.paymentMethod == PaymentMethod.CASH) (cashReceived ?: total) else total
            val change = if (state.paymentMethod == PaymentMethod.CASH) (received - total).coerceAtLeast(0.0) else 0.0

            val transactionId = transactionRepository.createTransaction(
                cashierId = cashierId,
                cashierName = cashierName,
                items = state.cartItems.map { it.productId to it.quantity },
                paymentMethod = state.paymentMethod,
                subtotal = subtotal,
                tax = tax,
                discount = discount,
                total = total,
                cashReceived = received,
                cashChange = change,
                notes = notes,
                status = TransactionStatus.COMPLETED
            )

            // Generate transaction number for display
            val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            val datePart = dateFormat.format(java.util.Date())
            val transactionNumber = "INV-$datePart-${transactionId.takeLast(4)}"

            _uiState.update {
                it.copy(
                    cart = emptyMap(),
                    isSaving = false,
                    lastSavedTransactionId = transactionId,
                    lastTransactionNumber = transactionNumber,
                    lastCashReceived = received,
                    lastCashChange = change,
                    lastPaymentMethod = state.paymentMethod,
                    successMessage = "Transaksi berhasil disimpan"
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false, paymentError = e.message ?: "Gagal menyimpan transaksi") }
        }
    }

    suspend fun saveDraftTransaction(
        cashierId: String,
        cashierName: String,
        notes: String? = null,
        clearCart: Boolean = true
    ) {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return
        _uiState.update { it.copy(isSaving = true, paymentError = null) }
        try {
            val subtotal = state.subtotal
            val discount = state.discountGlobal.coerceAtMost(subtotal)
            val taxableBase = subtotal - discount
            val tax = taxableBase * state.taxRate
            val total = taxableBase + tax

            val draftId = transactionRepository.createDraftTransaction(
                cashierId = cashierId,
                cashierName = cashierName,
                items = state.cartItems.map { it.productId to it.quantity },
                paymentMethod = state.paymentMethod,
                subtotal = subtotal,
                tax = tax,
                discount = discount,
                total = total,
                notes = notes
            )
            _uiState.update {
                it.copy(
                    cart = if (clearCart) emptyMap() else it.cart,
                    isSaving = false,
                    lastSavedTransactionId = draftId,
                    successMessage = if (clearCart) "Draft transaksi berhasil disimpan" else null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false, paymentError = e.message ?: "Gagal menyimpan draft") }
        }
    }
}
