package id.stargan.intikasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.stargan.intikasir.data.repository.ActivationRepository
import id.stargan.intikasir.feature.activation.navigation.activationScreen
import id.stargan.intikasir.ui.theme.IntiKasirTheme
import id.stargan.intikasir.feature.auth.navigation.AUTH_GRAPH_ROUTE
import id.stargan.intikasir.feature.auth.navigation.AuthRoutes
import id.stargan.intikasir.feature.auth.navigation.authNavGraph
import id.stargan.intikasir.feature.home.navigation.HomeRoutes
import id.stargan.intikasir.feature.home.navigation.homeNavGraph
import id.stargan.intikasir.feature.product.navigation.productNavGraph
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activationRepository: ActivationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntiKasirTheme {
                IntiKasirApp(activationRepository)
            }
        }
    }
}


@Composable
fun IntiKasirApp(activationRepository: ActivationRepository) {
    val navController = rememberNavController()
    val isActivated = remember { activationRepository.isActivated() }

    // Determine start destination based on activation status
    val startDestination = if (isActivated) AUTH_GRAPH_ROUTE else "activation"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Activation Screen (hanya jika belum aktivasi)
        activationScreen(
            onActivated = {
                navController.navigate(AUTH_GRAPH_ROUTE) {
                    popUpTo("activation") { inclusive = true }
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
