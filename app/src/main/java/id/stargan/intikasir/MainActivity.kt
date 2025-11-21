package id.stargan.intikasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.stargan.intikasir.data.repository.ActivationRepository
import id.stargan.intikasir.feature.activation.navigation.ACTIVATION_ROUTE
import id.stargan.intikasir.feature.activation.navigation.activationScreen
import id.stargan.intikasir.feature.splash.navigation.SPLASH_ROUTE
import id.stargan.intikasir.feature.splash.navigation.splashScreen
import id.stargan.intikasir.ui.theme.IntiKasirTheme
import id.stargan.intikasir.feature.auth.navigation.AUTH_GRAPH_ROUTE
import id.stargan.intikasir.feature.auth.navigation.AuthRoutes
import id.stargan.intikasir.feature.auth.navigation.authNavGraph
import id.stargan.intikasir.feature.home.navigation.HomeRoutes
import id.stargan.intikasir.feature.home.navigation.homeNavGraph
import id.stargan.intikasir.feature.product.navigation.productNavGraph
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activationRepository: ActivationRepository

    private val isActivatedState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initial activation check
        checkActivation()

        // Re-check activation when app resumes
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                checkActivation()
            }
        }

        setContent {
            IntiKasirTheme {
                IntiKasirApp(
                    activationRepository = activationRepository,
                    isActivated = isActivatedState.value,
                    onActivationChanged = { checkActivation() }
                )
            }
        }
    }

    private fun checkActivation() {
        isActivatedState.value = activationRepository.isActivated()
    }
}


@Composable
fun IntiKasirApp(
    activationRepository: ActivationRepository,
    isActivated: Boolean,
    onActivationChanged: () -> Unit
) {
    val navController = rememberNavController()

    // Always start with splash screen
    val startDestination = SPLASH_ROUTE

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - Check activation and navigate
        splashScreen(
            onNavigateToActivation = {
                navController.navigate(ACTIVATION_ROUTE) {
                    popUpTo(SPLASH_ROUTE) { inclusive = true }
                }
            },
            onNavigateToAuth = {
                navController.navigate(AUTH_GRAPH_ROUTE) {
                    popUpTo(SPLASH_ROUTE) { inclusive = true }
                }
            },
            isActivated = isActivated
        )

        // Activation Screen
        activationScreen(
            onActivated = {
                // Notify activation changed
                onActivationChanged()
                // Navigate to auth
                navController.navigate(AUTH_GRAPH_ROUTE) {
                    popUpTo(ACTIVATION_ROUTE) { inclusive = true }
                }
            }
        )

        // Auth Navigation Graph
        authNavGraph(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(HomeRoutes.HOME) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            }
        )

        // Home Navigation Graph
        homeNavGraph(
            navController = navController,
            onLogout = {
                // Navigate ke Login Screen, clear semua back stack
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        // Product Navigation Graph
        productNavGraph(
            navController = navController,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}
