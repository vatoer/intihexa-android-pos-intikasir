package id.stargan.intikasir.feature.settings.ui

import android.net.Uri
import id.stargan.intikasir.domain.model.StoreSettings

data class StoreSettingsUiState(
    val settings: StoreSettings? = null,
    val isLoading: Boolean = true,
    val logoPreviewUri: Uri? = null,
    val isImageProcessing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class StoreSettingsUiEvent {
    data object LoadSettings : StoreSettingsUiEvent()
    data class LogoPicked(val uri: Uri) : StoreSettingsUiEvent()
    data class LogoCropped(val uri: Uri) : StoreSettingsUiEvent()
    data object RemoveLogo : StoreSettingsUiEvent()
    data object PickLogo : StoreSettingsUiEvent()
    data object CaptureLogo : StoreSettingsUiEvent()
    data object DismissError : StoreSettingsUiEvent()
    data object DismissSuccess : StoreSettingsUiEvent()
    data class Save(val settings: StoreSettings) : StoreSettingsUiEvent()
}
