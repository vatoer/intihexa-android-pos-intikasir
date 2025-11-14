package id.stargan.intikasir.feature.product.ui.form

import id.stargan.intikasir.domain.model.Category
import android.net.Uri

/**
 * UI State untuk Product Form (Add/Edit)
 */
data class ProductFormUiState(
    val productId: String = "",
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val categoryId: String = "",
    val description: String = "",
    val price: String = "", // formatted display
    val cost: String = "", // formatted display
    val stock: String = "",
    val minStock: String = "",
    val imageUrl: String = "", // will hold local path
    val isActive: Boolean = true,

    // New raw numeric values to avoid parsing formatted string issues
    val rawPrice: String = "",
    val rawCost: String = "",

    // New image state
    val imagePreviewUri: Uri? = null,
    val isImageProcessing: Boolean = false,

    // Validation errors
    val nameError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,

    // Data
    val categories: List<Category> = emptyList(),

    // State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val saveSuccess: Boolean = false
)

/**
 * UI Events untuk Product Form
 */
sealed class ProductFormUiEvent {
    data class NameChanged(val name: String) : ProductFormUiEvent()
    data class SkuChanged(val sku: String) : ProductFormUiEvent()
    data class BarcodeChanged(val barcode: String) : ProductFormUiEvent()
    data class CategoryChanged(val categoryId: String) : ProductFormUiEvent()
    data class DescriptionChanged(val description: String) : ProductFormUiEvent()
    data class PriceChanged(val price: String, val raw: String) : ProductFormUiEvent()
    data class CostChanged(val cost: String, val raw: String) : ProductFormUiEvent()
    data class StockChanged(val stock: String) : ProductFormUiEvent()
    data class MinStockChanged(val minStock: String) : ProductFormUiEvent()
    // Replace ImageUrlChanged with image selection/crop events
    data class ImagePicked(val uri: Uri) : ProductFormUiEvent()
    data class ImageCropped(val uri: Uri) : ProductFormUiEvent()
    data object RemoveImage : ProductFormUiEvent()
    data class ActiveChanged(val isActive: Boolean) : ProductFormUiEvent()
    data object ScanBarcode : ProductFormUiEvent()
    data class BarcodeScanned(val value: String) : ProductFormUiEvent()
    data object PickImage : ProductFormUiEvent()
    data object CaptureImage : ProductFormUiEvent()
    data object SaveProduct : ProductFormUiEvent()
    data object DismissError : ProductFormUiEvent()
    data object NavigateBack : ProductFormUiEvent()
}
