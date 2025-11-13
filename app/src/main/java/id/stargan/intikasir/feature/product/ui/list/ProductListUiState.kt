package id.stargan.intikasir.feature.product.ui.list

import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.domain.model.Product

/**
 * UI State untuk Product List Screen
 */
data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLowStockOnly: Boolean = false
)

/**
 * UI Events untuk Product List Screen
 */
sealed class ProductListUiEvent {
    data class SearchQueryChanged(val query: String) : ProductListUiEvent()
    data class CategorySelected(val category: Category?) : ProductListUiEvent()
    data object ToggleLowStockFilter : ProductListUiEvent()
    data class DeleteProduct(val productId: String) : ProductListUiEvent()
    data object RefreshProducts : ProductListUiEvent()
    data object DismissError : ProductListUiEvent()
}

