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
    val showLowStockOnly: Boolean = false,
    val showFilterDialog: Boolean = false,
    val showSortDialog: Boolean = false,
    val currentFilter: id.stargan.intikasir.feature.product.domain.model.ProductFilter = id.stargan.intikasir.feature.product.domain.model.ProductFilter(),
    val currentSort: id.stargan.intikasir.feature.product.domain.model.ProductSortBy = id.stargan.intikasir.feature.product.domain.model.ProductSortBy.NAME_ASC,
    val isAdmin: Boolean = false
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
    data object ShowFilterDialog : ProductListUiEvent()
    data object HideFilterDialog : ProductListUiEvent()
    data object ShowSortDialog : ProductListUiEvent()
    data object HideSortDialog : ProductListUiEvent()
    data class FilterChanged(val filter: id.stargan.intikasir.feature.product.domain.model.ProductFilter) : ProductListUiEvent()
    data class SortChanged(val sort: id.stargan.intikasir.feature.product.domain.model.ProductSortBy) : ProductListUiEvent()
    data class ProductClicked(val productId: String) : ProductListUiEvent()
    data object AddProductClicked : ProductListUiEvent()
    data object ManageCategoriesClicked : ProductListUiEvent()
}
