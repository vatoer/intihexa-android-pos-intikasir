package id.stargan.intikasir.feature.pos.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import id.stargan.intikasir.feature.pos.ui.PosScreen

fun NavGraphBuilder.posNavGraph(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(PosRoutes.POS) {
        PosScreen(
            onPay = { total ->
                // Navigate back atau ke receipt jika diperlukan
                navController.navigateUp()
            }
        )
    }
}

