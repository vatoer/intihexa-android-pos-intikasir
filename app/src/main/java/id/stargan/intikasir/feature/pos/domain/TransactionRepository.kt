package id.stargan.intikasir.feature.pos.domain

import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.data.local.entity.TransactionStatus

interface TransactionRepository {
    suspend fun createTransaction(
        cashierId: String,
        cashierName: String,
        items: List<Pair<String, Int>>, // productId to quantity
        paymentMethod: PaymentMethod,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        cashReceived: Double,
        cashChange: Double,
        notes: String?,
        status: TransactionStatus
    ): String

    suspend fun createDraftTransaction(
        cashierId: String,
        cashierName: String,
        items: List<Pair<String, Int>>, // productId to quantity
        paymentMethod: PaymentMethod,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        notes: String?
    ): String
}
