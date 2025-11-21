package id.stargan.intikasir.feature.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.repository.ActivationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val activationRepository: ActivationRepository
) : ViewModel() {

    private val _activationState = MutableStateFlow<ActivationState>(ActivationState.Idle)
    val activationState: StateFlow<ActivationState> = _activationState

    private val _isActivated = MutableStateFlow(false)
    val isActivated: StateFlow<Boolean> = _isActivated

    private val _deviceId = MutableStateFlow("")
    val deviceId: StateFlow<String> = _deviceId

    private val _serialNumber = MutableStateFlow("")
    val serialNumber: StateFlow<String> = _serialNumber

    private val _expiryDate = MutableStateFlow("")
    val expiryDate: StateFlow<String> = _expiryDate

    init {
        checkActivationStatus()
    }

    fun activate(serialNumber: String) {
        if (serialNumber.isBlank()) {
            _activationState.value = ActivationState.Error("Serial Number tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _activationState.value = ActivationState.Loading

            val result = activationRepository.activateDevice(serialNumber)

            _activationState.value = if (result.isSuccess) {
                checkActivationStatus()
                ActivationState.Success(result.getOrNull() ?: "Aktivasi berhasil")
            } else {
                ActivationState.Error(result.exceptionOrNull()?.message ?: "Aktivasi gagal")
            }
        }
    }

    fun checkActivationStatus() {
        _isActivated.value = activationRepository.isActivated()
        _deviceId.value = activationRepository.getDeviceId()
        _serialNumber.value = activationRepository.getSerialNumber() ?: ""

        val expiry = activationRepository.getActivationExpiry()
        _expiryDate.value = if (expiry > 0) {
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
            sdf.format(Date(expiry))
        } else {
            "Selamanya"
        }
    }

    fun deactivate() {
        activationRepository.deactivate()
        checkActivationStatus()
    }

    fun resetState() {
        _activationState.value = ActivationState.Idle
    }
}

sealed class ActivationState {
    object Idle : ActivationState()
    object Loading : ActivationState()
    data class Success(val message: String) : ActivationState()
    data class Error(val message: String) : ActivationState()
}

