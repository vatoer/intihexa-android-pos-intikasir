package id.stargan.intikasir.feature.product.ui.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.local.image.ImageRepository
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val saveProductUseCase: SaveProductUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val imageRepository: ImageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String? = savedStateHandle.get<String>("productId")

    private val _uiState = MutableStateFlow(ProductFormUiState())
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        productId?.let { loadProduct(it) }
    }

    private fun formatThousand(value: String): String {
        val clean = value.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val number = clean.toLong()
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,###", symbols)
        return df.format(number)
    }

    fun onEvent(event: ProductFormUiEvent) {
        when (event) {
            is ProductFormUiEvent.NameChanged -> _uiState.update { it.copy(name = event.name, nameError = null) }
            is ProductFormUiEvent.SkuChanged -> _uiState.update { it.copy(sku = event.sku) }
            is ProductFormUiEvent.BarcodeChanged -> _uiState.update { it.copy(barcode = event.barcode) }
            is ProductFormUiEvent.BarcodeScanned -> _uiState.update { it.copy(barcode = event.value) }
            is ProductFormUiEvent.CategoryChanged -> _uiState.update { it.copy(categoryId = event.categoryId) }
            is ProductFormUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is ProductFormUiEvent.PriceChanged -> {
                _uiState.update { it.copy(price = event.price, rawPrice = event.raw, priceError = null) }
            }
            is ProductFormUiEvent.CostChanged -> {
                _uiState.update { it.copy(cost = event.cost, rawCost = event.raw) }
            }
            is ProductFormUiEvent.StockChanged -> _uiState.update { it.copy(stock = event.stock, stockError = null) }
            is ProductFormUiEvent.MinStockChanged -> _uiState.update { it.copy(minStock = event.minStock) }
            is ProductFormUiEvent.ImagePicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isImageProcessing = true) }
                    val path = imageRepository.saveImage(event.uri)
                    // Convert file path to Uri for preview
                    val fileUri = Uri.parse("file://$path")
                    _uiState.update { it.copy(imagePreviewUri = fileUri, imageUrl = path, isImageProcessing = false) }
                }
            }
            is ProductFormUiEvent.ImageCropped -> {
                viewModelScope.launch {
                    _uiState.value.imageUrl.takeIf { it.isNotBlank() }?.let { old -> imageRepository.deleteImage(old) }
                    _uiState.update { it.copy(isImageProcessing = true) }
                    val path = imageRepository.saveImage(event.uri)
                    // Convert file path to Uri for preview
                    val fileUri = Uri.parse("file://$path")
                    _uiState.update { it.copy(imagePreviewUri = fileUri, imageUrl = path, isImageProcessing = false) }
                }
            }
            ProductFormUiEvent.RemoveImage -> {
                viewModelScope.launch { _uiState.value.imageUrl.takeIf { it.isNotBlank() }?.let { imageRepository.deleteImage(it) } }
                _uiState.update { it.copy(imagePreviewUri = null, imageUrl = "") }
            }
            is ProductFormUiEvent.ActiveChanged -> _uiState.update { it.copy(isActive = event.isActive) }
            ProductFormUiEvent.ScanBarcode -> { /* Trigger UI side effect */ }
            ProductFormUiEvent.PickImage -> { /* Trigger UI side effect */ }
            ProductFormUiEvent.CaptureImage -> { /* Trigger UI side effect */ }
            ProductFormUiEvent.SaveProduct -> saveProduct()
            ProductFormUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
            ProductFormUiEvent.NavigateBack -> { /* handled by UI */ }
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
                                price = formatThousand(product.price.toLong().toString()),
                                rawPrice = product.price.toLong().toString(),
                                cost = product.cost?.let { c -> formatThousand(c.toLong().toString()) } ?: "",
                                rawCost = product.cost?.toLong()?.toString() ?: "",
                                stock = product.stock.toString(),
                                minStock = product.minStock.toString(),
                                imageUrl = product.imageUrl ?: "",
                                imagePreviewUri = product.imageUrl?.let { Uri.parse(it) },
                                isActive = product.isActive,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Produk tidak ditemukan") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Gagal memuat produk: ${e.message}") }
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
                val priceDouble = state.rawPrice.toDoubleOrNull() ?: 0.0
                val costDouble = state.rawCost.toDoubleOrNull()
                val product = Product(
                    id = if (state.isEditMode) state.productId else UUID.randomUUID().toString(),
                    name = state.name.trim(),
                    sku = state.sku.trim().ifBlank { null },
                    barcode = state.barcode.trim().ifBlank { null },
                    categoryId = state.categoryId.ifBlank { null },
                    categoryName = state.categories.find { it.id == state.categoryId }?.name,
                    description = state.description.trim().ifBlank { null },
                    price = priceDouble,
                    cost = costDouble,
                    stock = state.stock.toInt(),
                    minStock = state.minStock.toIntOrNull() ?: 5,
                    lowStockThreshold = state.minStock.toIntOrNull() ?: 10,
                    imageUrl = state.imageUrl.ifBlank { state.imagePreviewUri?.toString() },
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
        var valid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nama produk harus diisi") }
            valid = false
        }

        val priceVal = state.rawPrice.toDoubleOrNull()
        if (priceVal == null || priceVal <= 0.0) {
            _uiState.update { it.copy(priceError = "Harga harus valid dan > 0") }
            valid = false
        }

        val stockVal = state.stock.toIntOrNull()
        if (stockVal == null || stockVal < 0) {
            _uiState.update { it.copy(stockError = "Stok harus valid dan >= 0") }
            valid = false
        }

        return valid
    }
}
