package id.stargan.intikasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import id.stargan.intikasir.ui.theme.IntiKasirTheme
import id.stargan.intikasir.feature.auth.navigation.AUTH_GRAPH_ROUTE
import id.stargan.intikasir.feature.auth.navigation.AuthRoutes
import id.stargan.intikasir.feature.auth.navigation.authNavGraph
import id.stargan.intikasir.feature.home.navigation.HomeRoutes
import id.stargan.intikasir.feature.home.navigation.homeNavGraph
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntiKasirTheme {
//                PosScreen(
//                    modifier = Modifier.fillMaxSize()
//                )
                IntiKasirApp()
            }
        }
    }
}


@Composable
fun IntiKasirApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AUTH_GRAPH_ROUTE
    ) {
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
    }
}
