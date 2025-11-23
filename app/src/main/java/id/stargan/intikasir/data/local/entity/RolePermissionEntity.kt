package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Permissions per role. Single row per role (id = role name, e.g. "CASHIER").
 */
@Entity(tableName = "role_permissions")
data class RolePermissionEntity(
    @PrimaryKey
    val roleId: String,

    // Fitur Kasir
    val canCreateTransaction: Boolean = true,

    // Produk
    val canCreateProduct: Boolean = false,
    val canEditProduct: Boolean = false,
    val canDeleteProduct: Boolean = false,

    // Kategori
    val canCreateCategory: Boolean = false,
    val canEditCategory: Boolean = false,
    val canDeleteCategory: Boolean = false,

    // Riwayat
    val canDeleteTransaction: Boolean = false,

    // Pengeluaran
    val canViewExpense: Boolean = false,
    val canCreateExpense: Boolean = false,
    val canEditExpense: Boolean = false,
    val canDeleteExpense: Boolean = false,

    // Laporan
    val canViewReports: Boolean = false,

    // Pengaturan
    val canEditSettings: Boolean = false,

    val updatedAt: Long = System.currentTimeMillis()
)

