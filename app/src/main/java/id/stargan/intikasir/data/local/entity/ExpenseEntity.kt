package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    val paymentMethod: PaymentMethod,
    val receiptPhoto: String? = null, // Optional file path
    val createdBy: String, // User ID
    val createdByName: String, // User name for display
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

enum class ExpenseCategory {
    SUPPLIES,      // Perlengkapan
    UTILITIES,     // Listrik, Air, dll
    RENT,          // Sewa tempat
    SALARY,        // Gaji
    MARKETING,     // Promosi
    MAINTENANCE,   // Perbaikan
    TRANSPORT,     // Transportasi
    MISC           // Lain-lain
}

