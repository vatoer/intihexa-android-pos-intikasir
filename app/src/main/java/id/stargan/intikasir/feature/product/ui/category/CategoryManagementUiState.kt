package id.stargan.intikasir.feature.product.ui.category

import id.stargan.intikasir.domain.model.Category

/**
 * UI State untuk Category Management
 */
data class CategoryManagementUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedCategory: Category? = null,

    // Form fields
    val categoryName: String = "",
    val categoryDescription: String = "",
    val categoryColor: String = "#6200EE",
    val categoryIcon: String = "📦",

    // Validation
    val nameError: String? = null
)

/**
 * UI Events untuk Category Management
 */
sealed class CategoryManagementUiEvent {
    data object ShowAddDialog : CategoryManagementUiEvent()
    data class ShowEditDialog(val category: Category) : CategoryManagementUiEvent()
    data class ShowDeleteDialog(val category: Category) : CategoryManagementUiEvent()
    data object HideDialogs : CategoryManagementUiEvent()

    data class NameChanged(val name: String) : CategoryManagementUiEvent()
    data class DescriptionChanged(val description: String) : CategoryManagementUiEvent()
    data class ColorChanged(val color: String) : CategoryManagementUiEvent()
    data class IconChanged(val icon: String) : CategoryManagementUiEvent()

    data object SaveCategory : CategoryManagementUiEvent()
    data object ConfirmDelete : CategoryManagementUiEvent()
    data object DismissError : CategoryManagementUiEvent()
    data object DismissSuccess : CategoryManagementUiEvent()
}

