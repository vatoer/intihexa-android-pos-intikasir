package id.stargan.intikasir.feature.product.ui.detail

import id.stargan.intikasir.domain.model.Product

/**
 * UI State untuk Product Detail Screen
 */
data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val isAdmin: Boolean = false
)

/**
 * UI Events untuk Product Detail Screen
 */
sealed class ProductDetailUiEvent {
    data object EditProduct : ProductDetailUiEvent()
    data object DeleteProduct : ProductDetailUiEvent()
    data object ConfirmDelete : ProductDetailUiEvent()
    data object CancelDelete : ProductDetailUiEvent()
    data object DismissError : ProductDetailUiEvent()
    data object NavigateBack : ProductDetailUiEvent()
}

