package id.stargan.intikasir.feature.product.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadProducts()
    }

    fun onEvent(event: ProductListUiEvent) {
        when (event) {
            is ProductListUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                searchProducts(event.query)
            }
            is ProductListUiEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                loadProducts()
            }
            is ProductListUiEvent.ToggleLowStockFilter -> {
                _uiState.update { it.copy(showLowStockOnly = !it.showLowStockOnly) }
                loadProducts()
            }
            is ProductListUiEvent.DeleteProduct -> {
                deleteProduct(event.productId)
            }
            is ProductListUiEvent.RefreshProducts -> {
                loadProducts()
            }
            is ProductListUiEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
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

