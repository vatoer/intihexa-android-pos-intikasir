package id.stargan.intikasir.feature.activation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.repository.ActivationRepository
import id.stargan.intikasir.util.DateFormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val activationRepository: ActivationRepository
) : ViewModel() {

    private val TAG = "ActivationViewModel"

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
        Log.d(TAG, "ViewModel initialized")
        checkActivationStatus()
    }

    fun activate(serialNumber: String) {
        if (serialNumber.isBlank()) {
            _activationState.value = ActivationState.Error("Serial Number tidak boleh kosong")
            return
        }

        Log.d(TAG, "Starting activation with SN: $serialNumber")
        viewModelScope.launch {
            _activationState.value = ActivationState.Loading

            val result = activationRepository.activateDevice(serialNumber)

            _activationState.value = if (result.isSuccess) {
                Log.d(TAG, "Activation successful, re-checking status...")
                checkActivationStatus()
                Log.d(TAG, "After re-check - isActivated: ${_isActivated.value}")
                ActivationState.Success(result.getOrNull() ?: "Aktivasi berhasil")
            } else {
                Log.e(TAG, "Activation failed: ${result.exceptionOrNull()?.message}")
                ActivationState.Error(result.exceptionOrNull()?.message ?: "Aktivasi gagal")
            }
        }
    }

    fun checkActivationStatus() {
        Log.d(TAG, "Checking activation status...")
        _isActivated.value = activationRepository.isActivated()
        _deviceId.value = activationRepository.getDeviceId()
        _serialNumber.value = activationRepository.getSerialNumber() ?: ""

        val expiry = activationRepository.getActivationExpiry()
        _expiryDate.value = if (expiry > 0) {
            DateFormatUtils.formatEpochMillis(expiry, "dd MMM yyyy HH:mm")
        } else {
            "Selamanya"
        }

        Log.d(TAG, "Status check result - Activated: ${_isActivated.value}, SN: ${_serialNumber.value}, Expiry: ${_expiryDate.value}")
    }

    fun deactivate() {
        Log.d(TAG, "Deactivating...")
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
