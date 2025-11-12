package id.stargan.intikasir.feature.auth.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.auth.domain.usecase.CheckAuthStatusUseCase
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
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Check if user is logged in
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            // Minimum splash screen duration
            delay(1500)

            checkAuthStatusUseCase().collect { isLoggedIn ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isChecking = false,
                        authCheckComplete = true,
                        isLoggedIn = isLoggedIn
                    )
                }
            }
        }
    }
}

