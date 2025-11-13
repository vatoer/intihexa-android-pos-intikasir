package id.stargan.intikasir.feature.product.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val saveProductUseCase: SaveProductUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String? = savedStateHandle.get<String>("productId")

    private val _uiState = MutableStateFlow(ProductFormUiState())
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        productId?.let { loadProduct(it) }
    }

    fun onEvent(event: ProductFormUiEvent) {
        when (event) {
            is ProductFormUiEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, nameError = null) }
            }
            is ProductFormUiEvent.SkuChanged -> {
                _uiState.update { it.copy(sku = event.sku) }
            }
            is ProductFormUiEvent.BarcodeChanged -> {
                _uiState.update { it.copy(barcode = event.barcode) }
            }
            is ProductFormUiEvent.CategoryChanged -> {
                _uiState.update { it.copy(categoryId = event.categoryId) }
            }
            is ProductFormUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is ProductFormUiEvent.PriceChanged -> {
                _uiState.update { it.copy(price = event.price, priceError = null) }
            }
            is ProductFormUiEvent.CostChanged -> {
                _uiState.update { it.copy(cost = event.cost) }
            }
            is ProductFormUiEvent.StockChanged -> {
                _uiState.update { it.copy(stock = event.stock, stockError = null) }
            }
            is ProductFormUiEvent.MinStockChanged -> {
                _uiState.update { it.copy(minStock = event.minStock) }
            }
            is ProductFormUiEvent.ImageUrlChanged -> {
                _uiState.update { it.copy(imageUrl = event.imageUrl) }
            }
            is ProductFormUiEvent.ActiveChanged -> {
                _uiState.update { it.copy(isActive = event.isActive) }
            }
            is ProductFormUiEvent.SaveProduct -> {
                saveProduct()
            }
            is ProductFormUiEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
            is ProductFormUiEvent.NavigateBack -> {
                // Handled by UI
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

    private fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditMode = true) }

            try {
                getProductByIdUseCase(productId).collect { product ->
                    if (product != null) {
                        _uiState.update {
                            it.copy(
                                productId = product.id,
                                name = product.name,
                                sku = product.sku ?: "",
                                barcode = product.barcode ?: "",
                                categoryId = product.categoryId ?: "",
                                description = product.description ?: "",
                                price = product.price.toString(),
                                cost = product.cost?.toString() ?: "",
                                stock = product.stock.toString(),
                                minStock = product.minStock.toString(),
                                imageUrl = product.imageUrl ?: "",
                                isActive = product.isActive,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Produk tidak ditemukan"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat produk: ${e.message}"
                    )
                }
            }
        }
    }

    private fun saveProduct() {
        // Validate
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val state = _uiState.value
                val product = Product(
                    id = if (state.isEditMode) state.productId else UUID.randomUUID().toString(),
                    name = state.name.trim(),
                    sku = state.sku.trim().ifBlank { null },
                    barcode = state.barcode.trim().ifBlank { null },
                    categoryId = state.categoryId.ifBlank { null },
                    categoryName = state.categories.find { it.id == state.categoryId }?.name,
                    description = state.description.trim().ifBlank { null },
                    price = state.price.toDouble(),
                    cost = state.cost.toDoubleOrNull(),
                    stock = state.stock.toInt(),
                    minStock = state.minStock.toIntOrNull() ?: 5,
                    imageUrl = state.imageUrl.trim().ifBlank { null },
                    isActive = state.isActive,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                saveProductUseCase(product)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Gagal menyimpan produk: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nama produk harus diisi") }
            isValid = false
        }

        if (state.price.isBlank() || state.price.toDoubleOrNull() == null || state.price.toDouble() <= 0) {
            _uiState.update { it.copy(priceError = "Harga harus diisi dan lebih dari 0") }
            isValid = false
        }

        if (state.stock.isBlank() || state.stock.toIntOrNull() == null || state.stock.toInt() < 0) {
            _uiState.update { it.copy(stockError = "Stok harus diisi dan tidak boleh negatif") }
            isValid = false
        }

        return isValid
    }
}

