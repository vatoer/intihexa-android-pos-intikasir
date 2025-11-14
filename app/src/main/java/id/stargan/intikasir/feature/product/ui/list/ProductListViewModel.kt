package id.stargan.intikasir.feature.product.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.domain.model.UserRole
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.product.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getLowStockProductsUseCase: GetLowStockProductsUseCase,
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
                val flow = when {
                    _uiState.value.showLowStockOnly -> getLowStockProductsUseCase()
                    else -> getAllProductsUseCase()
                }

                flow.collect { products ->
                    val filtered = if (_uiState.value.selectedCategory != null) {
                        products.filter { it.categoryId == _uiState.value.selectedCategory?.id }
                    } else {
                        products
                    }

                    _uiState.update {
                        it.copy(
                            products = filtered,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Terjadi kesalahan"
                    )
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
            searchProductsUseCase(query).collect { products ->
                _uiState.update { it.copy(products = products) }
            }
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
