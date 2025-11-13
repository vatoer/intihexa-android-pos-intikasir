package id.stargan.intikasir.feature.product.ui.list

import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.model.ProductFilter
import id.stargan.intikasir.feature.product.domain.model.ProductSortBy

/**
 * UI State untuk Product List Screen
 */
data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val currentFilter: ProductFilter = ProductFilter(),
    val currentSort: ProductSortBy = ProductSortBy.NAME_ASC,
    val showFilterDialog: Boolean = false,
    val showSortDialog: Boolean = false,
    val isAdmin: Boolean = false
)

/**
 * UI Events untuk Product List Screen
 */
sealed class ProductListUiEvent {
    data class SearchQueryChanged(val query: String) : ProductListUiEvent()
    data class FilterChanged(val filter: ProductFilter) : ProductListUiEvent()
    data class SortChanged(val sortBy: ProductSortBy) : ProductListUiEvent()
    data class ProductClicked(val productId: String) : ProductListUiEvent()
    data object AddProductClicked : ProductListUiEvent()
    data object ManageCategoriesClicked : ProductListUiEvent()
    data object ShowFilterDialog : ProductListUiEvent()
    data object HideFilterDialog : ProductListUiEvent()
    data object ShowSortDialog : ProductListUiEvent()
    data object HideSortDialog : ProductListUiEvent()
    data object ClearFilter : ProductListUiEvent()
    data object Refresh : ProductListUiEvent()
}

