package id.stargan.intikasir.feature.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import id.stargan.intikasir.feature.splash.SplashScreen

const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashScreen(
    onNavigateToActivation: () -> Unit,
    onNavigateToAuth: () -> Unit,
    isActivated: Boolean
) {
    composable(route = SPLASH_ROUTE) {
        SplashScreen(
            onNavigateToActivation = onNavigateToActivation,
            onNavigateToAuth = onNavigateToAuth,
            isActivated = isActivated
        )
    }
}

