package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk detail item dalam transaksi
 */
@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("transactionId"), Index("productId")]
)
data class TransactionItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val transactionId: String,
    val productId: String,

    // Product snapshot (disimpan untuk historical data)
    val productName: String,
    val productPrice: Double,
    val productSku: String? = null,

    // Quantity and calculation
    val quantity: Int,
    val unitPrice: Double, // Harga satuan saat transaksi
    val discount: Double = 0.0, // Diskon per item
    val subtotal: Double, // quantity * unitPrice - discount

    val notes: String? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis()
)

