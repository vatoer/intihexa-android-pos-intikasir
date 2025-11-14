package id.stargan.intikasir.feature.product.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.domain.model.UserRole
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.product.domain.usecase.*
import id.stargan.intikasir.feature.product.domain.model.ProductFilter
import id.stargan.intikasir.feature.product.domain.model.ProductSortBy
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        checkUserRole()
        loadCategories()
        loadProducts()
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(isAdmin = user?.role == UserRole.ADMIN)
                }
            }
        }
    }

    fun onEvent(event: ProductListUiEvent) {
        when {
            event is ProductListUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                searchProducts(event.query)
            }
            event is ProductListUiEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                loadProducts()
            }
            event === ProductListUiEvent.ToggleLowStockFilter -> {
                _uiState.update { it.copy(showLowStockOnly = !_uiState.value.showLowStockOnly) }
                loadProducts()
            }
            event is ProductListUiEvent.DeleteProduct -> {
                deleteProduct(event.productId)
            }
            event === ProductListUiEvent.RefreshProducts -> {
                loadProducts()
            }
            event === ProductListUiEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
            event is ProductListUiEvent.FilterChanged -> {
                _uiState.update { it.copy(currentFilter = event.filter) }
                loadProducts()
            }
            event is ProductListUiEvent.SortChanged -> {
                _uiState.update { it.copy(currentSort = event.sort) }
                loadProducts()
            }
            event is ProductListUiEvent.ProductClicked -> {
                // Navigation handled in UI
            }
            event === ProductListUiEvent.AddProductClicked -> {
                // Navigation handled in UI
            }
            event === ProductListUiEvent.ManageCategoriesClicked -> {
                // Navigation handled in UI
            }
            event === ProductListUiEvent.ShowFilterDialog -> {
                _uiState.update { it.copy(showFilterDialog = true) }
            }
            event === ProductListUiEvent.HideFilterDialog -> {
                _uiState.update { it.copy(showFilterDialog = false) }
            }
            event === ProductListUiEvent.ShowSortDialog -> {
                _uiState.update { it.copy(showSortDialog = true) }
            }
            event === ProductListUiEvent.HideSortDialog -> {
                _uiState.update { it.copy(showSortDialog = false) }
            }
            else -> {}
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getAllCategoriesUseCase().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Base flow: all products (we will apply low-stock via filter object now)
                getAllProductsUseCase().collect { products ->
                    val filter = _uiState.value.currentFilter
                    val sort = _uiState.value.currentSort

                    var working = products

                    // Category
                    filter.categoryId?.let { catId ->
                        working = working.filter { it.categoryId == catId }
                    }

                    // In stock only
                    if (filter.inStockOnly) {
                        working = working.filter { it.stock > 0 }
                    }

                    // Low stock only
                    if (filter.lowStockOnly) {
                        working = working.filter { it.isLowStock }
                    }

                    // Active only
                    if (filter.activeOnly) {
                        working = working.filter { it.isActive }
                    }

                    // Price range
                    filter.minPrice?.let { minP ->
                        working = working.filter { it.price >= minP }
                    }
                    filter.maxPrice?.let { maxP ->
                        working = working.filter { it.price <= maxP }
                    }

                    // Apply search query if present (combine with previous logic)
                    val query = _uiState.value.searchQuery.trim()
                    if (query.isNotEmpty()) {
                        val qLower = query.lowercase()
                        working = working.filter {
                            it.name.lowercase().contains(qLower) ||
                                (it.sku?.lowercase()?.contains(qLower) == true) ||
                                (it.barcode?.lowercase()?.contains(qLower) == true)
                        }
                    }

                    // Apply sorting
                    working = when (sort) {
                        ProductSortBy.NAME_ASC -> working.sortedBy { it.name.lowercase() }
                        ProductSortBy.NAME_DESC -> working.sortedByDescending { it.name.lowercase() }
                        ProductSortBy.PRICE_ASC -> working.sortedBy { it.price }
                        ProductSortBy.PRICE_DESC -> working.sortedByDescending { it.price }
                        ProductSortBy.STOCK_ASC -> working.sortedBy { it.stock }
                        ProductSortBy.STOCK_DESC -> working.sortedByDescending { it.stock }
                        ProductSortBy.NEWEST -> working.sortedByDescending { it.createdAt }
                        ProductSortBy.OLDEST -> working.sortedBy { it.createdAt }
                    }

                    _uiState.update {
                        it.copy(
                            products = working,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Terjadi kesalahan")
                }
            }
        }
    }

    private fun searchProducts(query: String) {
        if (query.isBlank()) {
            loadProducts()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            loadProducts()
        }
    }

    private fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                deleteProductUseCase(productId)
                // Products will auto-refresh via Flow
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Gagal menghapus produk: ${e.message}")
                }
            }
        }
    }
}
