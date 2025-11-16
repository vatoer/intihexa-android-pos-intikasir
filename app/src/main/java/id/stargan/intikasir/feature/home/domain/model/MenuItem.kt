package id.stargan.intikasir.feature.home.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import id.stargan.intikasir.feature.product.navigation.ProductRoutes

/**
 * Data class untuk menu item di home screen
 */
data class MenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String = ""
)

/**
 * Daftar menu items untuk home screen
 */
object MenuItems {
    private val items = listOf(
        MenuItem(
            id = "cashier",
            title = "Kasir",
            icon = Icons.Filled.PointOfSale,
            route = "cashier",
            description = "Halaman kasir"
        ),
        MenuItem(
            id = "products",
            title = "Produk",
            icon = Icons.Filled.ShoppingBag,
            route = ProductRoutes.PRODUCT_LIST,
            description = "Kelola produk"
        ),
        MenuItem(
            id = "categories",
            title = "Kategori",
            icon = Icons.Filled.Category,
            route = ProductRoutes.CATEGORY_MANAGEMENT,
            description = "Kelola kategori"
        ),
        MenuItem(
            id = "history",
            title = "Riwayat",
            icon = Icons.Filled.History,
            route = "history",
            description = "Riwayat transaksi"
        ),
        MenuItem(
            id = "expenses",
            title = "Pengeluaran",
            icon = Icons.Filled.MoneyOff,
            route = "expenses",
            description = "Kelola pengeluaran"
        ),
        MenuItem(
            id = "reports",
            title = "Laporan",
            icon = Icons.Filled.Assessment,
            route = "reports",
            description = "Laporan keuangan"
        ),
        MenuItem(
            id = "print",
            title = "Cetak Resi",
            icon = Icons.Filled.Print,
            route = "print_receipt",
            description = "Cetak ulang resi"
        ),

        MenuItem(
            id = "settings",
            title = "Pengaturan",
            icon = Icons.Filled.Settings,
            route = "settings",
            description = "Pengaturan aplikasi"
        )
    )

    fun items(isAdmin: Boolean): List<MenuItem> {
        val base = items
        val extra = mutableListOf(
            MenuItem(
                id = "profile",
                title = "Profil",
                icon = Icons.Filled.AccountCircle,
                route = "profil",
                description = "Ubah nama & PIN"
            )
        )
        if (isAdmin) {
            extra += MenuItem(
                id = "users",
                title = "Pengguna",
                icon = Icons.Filled.People,
                route = "pengguna",
                description = "Kelola pengguna"
            )
        }
        return base + extra
    }
}
