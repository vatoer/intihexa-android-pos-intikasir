package id.stargan.intikasir.feature.product.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.domain.model.UserRole
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.product.domain.usecase.DeleteProductUseCase
import id.stargan.intikasir.feature.product.domain.usecase.GetProductByIdUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle.get<String>("productId"))

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        checkUserRole()
        loadProduct()
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

    fun onEvent(event: ProductDetailUiEvent) {
        when (event) {
            is ProductDetailUiEvent.EditProduct -> {
                // Navigation handled in UI
            }
            is ProductDetailUiEvent.DeleteProduct -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }
            is ProductDetailUiEvent.ConfirmDelete -> {
                deleteProduct()
            }
            is ProductDetailUiEvent.CancelDelete -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
            }
            is ProductDetailUiEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
            is ProductDetailUiEvent.NavigateBack -> {
                // Navigation handled in UI
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getProductByIdUseCase(productId).collect { product ->
                    _uiState.update {
                        it.copy(
                            product = product,
                            isLoading = false,
                            error = if (product == null) "Produk tidak ditemukan" else null
                        )
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

    private fun deleteProduct() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDeleting = true,
                    showDeleteDialog = false
                )
            }

            try {
                deleteProductUseCase(productId)

                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        error = "Gagal menghapus produk: ${e.message}"
                    )
                }
            }
        }
    }
}

