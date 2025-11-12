package id.stargan.intikasir.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import id.stargan.intikasir.feature.auth.ui.login.LoginScreen
import id.stargan.intikasir.feature.auth.ui.splash.SplashScreen

/**
 * Auth navigation graph
 * Define all auth-related screens and navigation
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        startDestination = AuthRoutes.SPLASH,
        route = AUTH_GRAPH_ROUTE
    ) {
        // Splash Screen
        composable(route = AuthRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    onAuthSuccess()
                }
            )
        }

        // Login Screen
        composable(route = AuthRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    onAuthSuccess()
                }
            )
        }

        // Future: Register Screen (untuk admin create user baru)
        // composable(route = AuthRoutes.REGISTER) {
        //     RegisterScreen(...)
        // }

        // Future: Forgot PIN Screen
        // composable(route = AuthRoutes.FORGOT_PIN) {
        //     ForgotPinScreen(...)
        // }
    }
}

