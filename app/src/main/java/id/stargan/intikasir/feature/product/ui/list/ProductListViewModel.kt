package id.stargan.intikasir.feature.product.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.domain.model.UserRole
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.product.domain.usecase.GetCategoriesUseCase
import id.stargan.intikasir.feature.product.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Product List Screen
 */
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        loadData()
        checkUserRole()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: ProductListUiEvent) {
        when (event) {
            is ProductListUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                loadProducts()
            }
            is ProductListUiEvent.FilterChanged -> {
                _uiState.update { it.copy(currentFilter = event.filter) }
                loadProducts()
            }
            is ProductListUiEvent.SortChanged -> {
                _uiState.update { it.copy(currentSort = event.sortBy) }
                loadProducts()
            }
            is ProductListUiEvent.ShowFilterDialog -> {
                _uiState.update { it.copy(showFilterDialog = true) }
            }
            is ProductListUiEvent.HideFilterDialog -> {
                _uiState.update { it.copy(showFilterDialog = false) }
            }
            is ProductListUiEvent.ShowSortDialog -> {
                _uiState.update { it.copy(showSortDialog = true) }
            }
            is ProductListUiEvent.HideSortDialog -> {
                _uiState.update { it.copy(showSortDialog = false) }
            }
            is ProductListUiEvent.ClearFilter -> {
                _uiState.update {
                    it.copy(
                        currentFilter = id.stargan.intikasir.feature.product.domain.model.ProductFilter(),
                        searchQuery = ""
                    )
                }
                loadProducts()
            }
            is ProductListUiEvent.Refresh -> {
                loadData()
            }
            // Navigation events handled in UI
            is ProductListUiEvent.ProductClicked -> {}
            is ProductListUiEvent.AddProductClicked -> {}
            is ProductListUiEvent.ManageCategoriesClicked -> {}
        }
    }

    /**
     * Load products and categories
     */
    private fun loadData() {
        loadProducts()
        loadCategories()
    }

    /**
     * Load products dengan filter dan sort
     */
    private fun loadProducts() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                getProductsUseCase(
                    filter = _uiState.value.currentFilter,
                    sortBy = _uiState.value.currentSort
                ).collect { products ->
                    val filteredProducts = if (_uiState.value.searchQuery.isNotEmpty()) {
                        products.filter { product ->
                            product.name.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                            product.description?.contains(_uiState.value.searchQuery, ignoreCase = true) == true
                        }
                    } else {
                        products
                    }

                    _uiState.update {
                        it.copy(
                            products = filteredProducts,
                            isLoading = false
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

    /**
     * Load categories
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                getCategoriesUseCase().collect { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
            } catch (e: Exception) {
                // Handle error silently for categories
                e.printStackTrace()
            }
        }
    }

    /**
     * Check if user is admin
     */
    private fun checkUserRole() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(isAdmin = user?.role == UserRole.ADMIN)
                }
            }
        }
    }
}

