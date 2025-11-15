package id.stargan.intikasir.feature.settings.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.local.image.ImageRepository
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.feature.settings.domain.usecase.GetStoreSettingsUseCase
import id.stargan.intikasir.feature.settings.domain.usecase.UpdateStoreLogoUseCase
import id.stargan.intikasir.feature.settings.domain.usecase.UpdateStoreSettingsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreSettingsViewModel @Inject constructor(
    private val getStoreSettingsUseCase: GetStoreSettingsUseCase,
    private val updateStoreLogoUseCase: UpdateStoreLogoUseCase,
    private val imageRepository: ImageRepository,
    private val updateStoreSettingsUseCase: UpdateStoreSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreSettingsUiState())
    val uiState: StateFlow<StoreSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun onEvent(event: StoreSettingsUiEvent) {
        when (event) {
            StoreSettingsUiEvent.LoadSettings -> loadSettings()
            is StoreSettingsUiEvent.LogoPicked -> {
                // Direct save for now, can add crop later
                viewModelScope.launch {
                    _uiState.update { it.copy(isImageProcessing = true) }
                    try {
                        val path = imageRepository.saveImage(event.uri)
                        updateStoreLogoUseCase(path)
                        // Convert file path to Uri for preview
                        val fileUri = Uri.parse("file://$path")
                        _uiState.update {
                            it.copy(
                                logoPreviewUri = fileUri,
                                isImageProcessing = false,
                                successMessage = "Logo berhasil diperbarui"
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isImageProcessing = false,
                                error = "Gagal menyimpan logo: ${e.message}"
                            )
                        }
                    }
                }
            }
            is StoreSettingsUiEvent.LogoCropped -> {
                viewModelScope.launch {
                    // Delete old logo if exists
                    _uiState.value.settings?.storeLogo?.let { oldPath ->
                        imageRepository.deleteImage(oldPath)
                    }

                    _uiState.update { it.copy(isImageProcessing = true) }
                    try {
                        val path = imageRepository.saveImage(event.uri)
                        updateStoreLogoUseCase(path)
                        // Convert file path to Uri for preview
                        val fileUri = Uri.parse("file://$path")
                        _uiState.update {
                            it.copy(
                                logoPreviewUri = fileUri,
                                isImageProcessing = false,
                                successMessage = "Logo berhasil diperbarui"
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isImageProcessing = false,
                                error = "Gagal menyimpan logo: ${e.message}"
                            )
                        }
                    }
                }
            }
            StoreSettingsUiEvent.RemoveLogo -> {
                viewModelScope.launch {
                    try {
                        _uiState.value.settings?.storeLogo?.let { path ->
                            imageRepository.deleteImage(path)
                        }
                        updateStoreLogoUseCase(null)
                        _uiState.update {
                            it.copy(
                                logoPreviewUri = null,
                                successMessage = "Logo berhasil dihapus"
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(error = "Gagal menghapus logo: ${e.message}")
                        }
                    }
                }
            }
            StoreSettingsUiEvent.PickLogo -> { /* Handled by UI */ }
            StoreSettingsUiEvent.CaptureLogo -> { /* Handled by UI */ }
            StoreSettingsUiEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
            StoreSettingsUiEvent.DismissSuccess -> {
                _uiState.update { it.copy(successMessage = null) }
            }
            is StoreSettingsUiEvent.Save -> {
                saveSettings(event.settings)
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getStoreSettingsUseCase().collect { settings ->
                    val logoUri = settings?.storeLogo?.let { Uri.parse(it) }
                    _uiState.update {
                        it.copy(
                            settings = settings,
                            logoPreviewUri = logoUri,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat pengaturan: ${e.message}"
                    )
                }
            }
        }
    }

    private fun saveSettings(updated: StoreSettings) {
        viewModelScope.launch {
            try {
                updateStoreSettingsUseCase(updated)
                _uiState.update { it.copy(successMessage = "Pengaturan tersimpan") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Gagal menyimpan pengaturan: ${e.message}") }
            }
        }
    }
}
