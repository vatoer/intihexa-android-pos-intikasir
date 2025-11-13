package id.stargan.intikasir.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.auth.domain.usecase.LogoutUseCase
import id.stargan.intikasir.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Home Screen
 * Handles user info dan logout functionality
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    init {
        loadCurrentUser()
    }

    /**
     * Load current logged in user
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _currentUser.value = user
            }
        }
    }

    /**
     * Logout user
     * Clears session dan navigate ke login
     */
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoggingOut.value = true
                logoutUseCase()
                onLogoutComplete()
            } catch (e: Exception) {
                // Handle error jika perlu
                e.printStackTrace()
            } finally {
                _isLoggingOut.value = false
            }
        }
    }
}

