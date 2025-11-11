package id.stargan.intikasir.ui.screen.pos

import id.stargan.intikasir.domain.model.CartItem
import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.domain.model.PaymentMethod
import id.stargan.intikasir.domain.model.Product

/**
 * UI State untuk POS Screen
 */
data class PosUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val taxPercentage: Double = 0.0,
    val service: Double = 0.0,
    val servicePercentage: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val cashReceived: Double = 0.0,
    val cashChange: Double = 0.0,
    val showPaymentDialog: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val transactionNumber: String? = null,
    val error: String? = null
) {
    val cartItemCount: Int = cartItems.sumOf { it.quantity }
    val isCartEmpty: Boolean = cartItems.isEmpty()
    val canCheckout: Boolean = cartItems.isNotEmpty()
}

/**
 * UI Events untuk POS Screen
 */
sealed class PosUiEvent {
    data class ShowError(val message: String) : PosUiEvent()
    data class ShowSuccess(val transactionNumber: String) : PosUiEvent()
    object NavigateToReceipt : PosUiEvent()
    object PaymentCompleted : PosUiEvent()
}

