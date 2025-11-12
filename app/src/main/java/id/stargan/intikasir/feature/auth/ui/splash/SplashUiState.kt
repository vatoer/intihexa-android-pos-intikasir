package id.stargan.intikasir.feature.auth.ui.splash

/**
 * UI State untuk Splash Screen
 */
data class SplashUiState(
    val isChecking: Boolean = true,
    val authCheckComplete: Boolean = false,
    val isLoggedIn: Boolean = false
)

