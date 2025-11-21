package id.stargan.intikasir.feature.activation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import id.stargan.intikasir.feature.activation.ActivationScreen

const val ACTIVATION_ROUTE = "activation"

fun NavController.navigateToActivation() {
    navigate(ACTIVATION_ROUTE) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavGraphBuilder.activationScreen(
    onActivated: () -> Unit
) {
    composable(route = ACTIVATION_ROUTE) {
        ActivationScreen(onActivated = onActivated)
    }
}

