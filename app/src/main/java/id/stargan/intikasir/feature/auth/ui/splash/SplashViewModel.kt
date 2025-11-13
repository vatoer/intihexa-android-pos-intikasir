package id.stargan.intikasir.feature.auth.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.auth.domain.usecase.CheckAuthStatusUseCase
import id.stargan.intikasir.feature.auth.domain.usecase.InitializeDefaultUsersUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Splash Screen
 * Checks authentication status saat app start
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val initializeDefaultUsersUseCase: InitializeDefaultUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        initializeApp()
    }

    /**
     * Initialize aplikasi:
     * 1. Inisialisasi user default jika belum ada
     * 2. Check authentication status
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Inisialisasi user default jika database masih kosong
                initializeDefaultUsersUseCase()

                // Minimum splash screen duration
                delay(1500)

                // Check authentication status
                checkAuthStatusUseCase().collect { isLoggedIn ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isChecking = false,
                            authCheckComplete = true,
                            isLoggedIn = isLoggedIn
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error, tetap lanjutkan ke auth check
                _uiState.update { currentState ->
                    currentState.copy(
                        isChecking = false,
                        authCheckComplete = true,
                        isLoggedIn = false
                    )
                }
            }
        }
    }
}

