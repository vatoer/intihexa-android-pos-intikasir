package id.stargan.intikasir.feature.product.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.feature.product.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun onEvent(event: CategoryManagementUiEvent) {
        when (event) {
            is CategoryManagementUiEvent.ShowAddDialog -> {
                _uiState.update {
                    it.copy(
                        showAddDialog = true,
                        selectedCategory = null,
                        categoryName = "",
                        categoryDescription = "",
                        categoryColor = "#6200EE",
                        categoryIcon = "📦",
                        nameError = null
                    )
                }
            }
            is CategoryManagementUiEvent.ShowEditDialog -> {
                _uiState.update {
                    it.copy(
                        showEditDialog = true,
                        selectedCategory = event.category,
                        categoryName = event.category.name,
                        categoryDescription = event.category.description ?: "",
                        categoryColor = event.category.color ?: "#6200EE",
                        categoryIcon = event.category.icon ?: "📦",
                        nameError = null
                    )
                }
            }
            is CategoryManagementUiEvent.ShowDeleteDialog -> {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = true,
                        selectedCategory = event.category
                    )
                }
            }
            is CategoryManagementUiEvent.HideDialogs -> {
                _uiState.update {
                    it.copy(
                        showAddDialog = false,
                        showEditDialog = false,
                        showDeleteDialog = false,
                        selectedCategory = null
                    )
                }
            }
            is CategoryManagementUiEvent.NameChanged -> {
                _uiState.update { it.copy(categoryName = event.name, nameError = null) }
            }
            is CategoryManagementUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(categoryDescription = event.description) }
            }
            is CategoryManagementUiEvent.ColorChanged -> {
                _uiState.update { it.copy(categoryColor = event.color) }
            }
            is CategoryManagementUiEvent.IconChanged -> {
                _uiState.update { it.copy(categoryIcon = event.icon) }
            }
            is CategoryManagementUiEvent.SaveCategory -> {
                saveCategory()
            }
            is CategoryManagementUiEvent.ConfirmDelete -> {
                deleteCategory()
            }
            is CategoryManagementUiEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
            is CategoryManagementUiEvent.DismissSuccess -> {
                _uiState.update { it.copy(successMessage = null) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getAllCategoriesUseCase().collect { categories ->
                    _uiState.update {
                        it.copy(
                            categories = categories.sortedBy { it.order },
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat kategori: ${e.message}"
                    )
                }
            }
        }
    }

    private fun saveCategory() {
        val state = _uiState.value

        // Validate
        if (state.categoryName.isBlank()) {
            _uiState.update { it.copy(nameError = "Nama kategori harus diisi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val category = Category(
                    id = state.selectedCategory?.id ?: UUID.randomUUID().toString(),
                    name = state.categoryName.trim(),
                    description = state.categoryDescription.trim().ifBlank { null },
                    color = state.categoryColor,
                    icon = state.categoryIcon,
                    order = state.selectedCategory?.order ?: state.categories.size,
                    isActive = true,
                    createdAt = state.selectedCategory?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                saveCategoryUseCase(category)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showAddDialog = false,
                        showEditDialog = false,
                        selectedCategory = null,
                        successMessage = if (state.selectedCategory != null)
                            "Kategori berhasil diperbarui"
                        else
                            "Kategori berhasil ditambahkan"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Gagal menyimpan kategori: ${e.message}"
                    )
                }
            }
        }
    }

    private fun deleteCategory() {
        val categoryId = _uiState.value.selectedCategory?.id ?: return

        viewModelScope.launch {
            try {
                deleteCategoryUseCase(categoryId)
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        selectedCategory = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Gagal menghapus kategori: ${e.message}")
                }
            }
        }
    }
}

