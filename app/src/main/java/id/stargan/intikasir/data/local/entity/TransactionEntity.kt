package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk transaksi
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["cashierId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("cashierId"), Index("transactionDate"), Index("status")]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val transactionNumber: String, // Format: INV-YYYYMMDD-XXXX
    val transactionDate: Long = System.currentTimeMillis(),

    // User/Cashier
    val cashierId: String,
    val cashierName: String,

    // Payment
    val paymentMethod: PaymentMethod,
    val subtotal: Double,
    val tax: Double = 0.0, // Pajak (PPN)
    val service: Double = 0.0, // Biaya layanan
    val discount: Double = 0.0,
    val total: Double,

    // Cash payment details
    val cashReceived: Double = 0.0,
    val cashChange: Double = 0.0,

    // Status
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val notes: String? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)

enum class PaymentMethod {
    CASH,      // Tunai
    QRIS,      // QRIS
    CARD,      // Kartu Debit/Kredit
    TRANSFER   // Transfer Bank
}

enum class TransactionStatus {
    DRAFT,       // Draft belum disimpan (untuk cart persistence)
    PENDING,     // Pesanan dibuat, belum dibayar
    PAID,        // Sudah dibayar, belum diproses
    PROCESSING,  // Sedang diproses
    COMPLETED,   // Selesai
    CANCELLED,   // Dibatalkan
    REFUNDED     // Dikembalikan
}

