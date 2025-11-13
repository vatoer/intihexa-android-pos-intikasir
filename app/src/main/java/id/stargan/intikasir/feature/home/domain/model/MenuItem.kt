package id.stargan.intikasir.feature.home.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

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
    val items = listOf(
        MenuItem(
            id = "products",
            title = "Produk",
            icon = Icons.Filled.ShoppingBag,
            route = "products",
            description = "Kelola produk"
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
            id = "cashier",
            title = "Kasir",
            icon = Icons.Filled.PointOfSale,
            route = "cashier",
            description = "Halaman kasir"
        ),
        MenuItem(
            id = "settings",
            title = "Pengaturan",
            icon = Icons.Filled.Settings,
            route = "settings",
            description = "Pengaturan aplikasi"
        )
    )
}

