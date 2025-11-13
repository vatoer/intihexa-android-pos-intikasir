package id.stargan.intikasir.feature.product.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import id.stargan.intikasir.feature.product.ui.category.CategoryManagementScreen
import id.stargan.intikasir.feature.product.ui.form.ProductFormScreen
import id.stargan.intikasir.feature.product.ui.list.ProductListScreen

/**
 * Product navigation graph
 * Contains all product-related screens
 */
fun NavGraphBuilder.productNavGraph(
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    navigation(
        startDestination = ProductRoutes.PRODUCT_LIST,
        route = PRODUCT_GRAPH_ROUTE
    ) {
        // Product List Screen
        composable(route = ProductRoutes.PRODUCT_LIST) {
            ProductListScreen(
                onProductClick = { productId ->
                    navController.navigate(ProductRoutes.productFormWithId(productId))
                },
                onAddProductClick = {
                    navController.navigate(ProductRoutes.PRODUCT_FORM)
                },
                onManageCategoriesClick = {
                    navController.navigate(ProductRoutes.CATEGORY_MANAGEMENT)
                },
                onBackClick = onNavigateBack
            )
        }

        // Product Form Screen (Add)
        composable(route = ProductRoutes.PRODUCT_FORM) {
            ProductFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Product Form Screen (Edit)
        composable(
            route = ProductRoutes.PRODUCT_FORM_WITH_ID,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) {
            ProductFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Category Management Screen
        composable(route = ProductRoutes.CATEGORY_MANAGEMENT) {
            CategoryManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

