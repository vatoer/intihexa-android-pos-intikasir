package id.stargan.intikasir.feature.pos.data

import id.stargan.intikasir.feature.pos.domain.TransactionRepository
import id.stargan.intikasir.data.local.dao.TransactionDao
import id.stargan.intikasir.data.local.dao.TransactionItemDao
import id.stargan.intikasir.data.local.dao.ProductDao
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.data.local.entity.TransactionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionItemDao: TransactionItemDao,
    private val productDao: ProductDao
) : TransactionRepository {

    override suspend fun createTransaction(
        cashierId: String,
        cashierName: String,
        items: List<Pair<String, Int>>,
        paymentMethod: PaymentMethod,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        cashReceived: Double,
        cashChange: Double,
        notes: String?,
        status: TransactionStatus
    ): String = withContext(Dispatchers.IO) {
        // Generate transaction number (INV-YYYYMMDD-####)
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val prefix = "INV-$datePart"
        val lastNumber = transactionDao.getLastTransactionNumber(prefix) // e.g. INV-20231115-0007
        val nextSeq = (lastNumber?.substringAfterLast('-')?.toIntOrNull() ?: 0) + 1
        val transactionNumber = "$prefix-${String.format(Locale.getDefault(), "%04d", nextSeq)}"

        val transaction = TransactionEntity(
            transactionNumber = transactionNumber,
            cashierId = cashierId,
            cashierName = cashierName,
            paymentMethod = paymentMethod,
            subtotal = subtotal,
            tax = tax,
            discount = discount,
            total = total,
            cashReceived = cashReceived,
            cashChange = cashChange,
            notes = notes,
            status = status
        )
        transactionDao.insertTransaction(transaction)

        // Insert items and update product stock
        val productMap = productDao.getProductsByIds(items.map { it.first }).associateBy { it.id }
        val itemEntities = items.map { (productId, qty) ->
            val p = productMap[productId] ?: throw IllegalArgumentException("Product not found: $productId")
            val itemDiscount = 0.0 // future: pass in per-item discount
            TransactionItemEntity(
                transactionId = transaction.id,
                productId = productId,
                productName = p.name,
                productPrice = p.price,
                productSku = p.sku,
                quantity = qty,
                unitPrice = p.price,
                discount = itemDiscount,
                subtotal = (p.price * qty) - itemDiscount
            )
        }
        transactionItemDao.insertItems(itemEntities)

        // Update stock (decrement)
        items.forEach { (productId, qty) ->
            productDao.decrementStock(productId, qty)
        }

        transaction.id
    }

    override suspend fun createDraftTransaction(
        cashierId: String,
        cashierName: String,
        items: List<Pair<String, Int>>,
        paymentMethod: PaymentMethod,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        notes: String?
    ): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val prefix = "INV-$datePart"
        val lastNumber = transactionDao.getLastTransactionNumber(prefix)
        // Reset nextSeq daily: only count for today (prefix includes date)
        val nextSeq = if (lastNumber != null && lastNumber.startsWith(prefix)) {
            (lastNumber.substringAfterLast('-').toIntOrNull() ?: 0) + 1
        } else {
            1
        }
        val transactionNumber = "$prefix-${String.format(Locale.getDefault(), "%04d", nextSeq)}"

        val transaction = TransactionEntity(
            transactionNumber = transactionNumber,
            cashierId = cashierId,
            cashierName = cashierName,
            paymentMethod = paymentMethod,
            subtotal = subtotal,
            tax = tax,
            discount = discount,
            total = total,
            cashReceived = 0.0,
            cashChange = 0.0,
            notes = notes,
            status = TransactionStatus.PENDING
        )
        transactionDao.insertTransaction(transaction)

        val productMap = productDao.getProductsByIds(items.map { it.first }).associateBy { it.id }
        val itemEntities = items.map { (productId, qty) ->
            val p = productMap[productId] ?: throw IllegalArgumentException("Product not found: $productId")
            TransactionItemEntity(
                transactionId = transaction.id,
                productId = productId,
                productName = p.name,
                productPrice = p.price,
                productSku = p.sku,
                quantity = qty,
                unitPrice = p.price,
                discount = 0.0,
                subtotal = (p.price * qty)
            )
        }
        transactionItemDao.insertItems(itemEntities)
        transaction.id
    }

    override suspend fun createEmptyDraft(
        cashierId: String,
        cashierName: String
    ): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val prefix = "TX-$datePart"
        val lastNumber = transactionDao.getLastTransactionNumber(prefix)
        val nextSeq = (lastNumber?.substringAfterLast('-')?.toIntOrNull() ?: 0) + 1
        val transactionNumber = "$prefix-${String.format(Locale.getDefault(), "%04d", nextSeq)}"

        val transaction = TransactionEntity(
            transactionNumber = transactionNumber,
            cashierId = cashierId,
            cashierName = cashierName,
            paymentMethod = PaymentMethod.CASH,
            subtotal = 0.0,
            tax = 0.0,
            discount = 0.0,
            total = 0.0,
            cashReceived = 0.0,
            cashChange = 0.0,
            notes = null,
            status = TransactionStatus.DRAFT
        )
        transactionDao.insertTransaction(transaction)
        transaction.id
    }

    override fun getTransactionById(transactionId: String): Flow<TransactionEntity?> {
        return transactionDao.getTransactionByIdFlow(transactionId)
    }

    override fun getTransactionItems(transactionId: String): Flow<List<TransactionItemEntity>> {
        return transactionItemDao.getItemsByTransactionIdFlow(transactionId)
    }

    override suspend fun updateTransactionItems(
        transactionId: String,
        items: List<Pair<String, Int>>,
        itemDiscounts: Map<String, Double>
    ) = withContext(Dispatchers.IO) {
        // Delete existing items
        transactionItemDao.deleteItemsByTransactionId(transactionId)

        if (items.isEmpty()) return@withContext

        // Insert new items
        val productMap = productDao.getProductsByIds(items.map { it.first }).associateBy { it.id }
        val itemEntities = items.map { (productId, qty) ->
            val p = productMap[productId] ?: throw IllegalArgumentException("Product not found: $productId")
            val itemDiscount = itemDiscounts[productId] ?: 0.0
            TransactionItemEntity(
                transactionId = transactionId,
                productId = productId,
                productName = p.name,
                productPrice = p.price,
                productSku = p.sku,
                quantity = qty,
                unitPrice = p.price,
                discount = itemDiscount,
                subtotal = (p.price * qty) - itemDiscount
            )
        }
        transactionItemDao.insertItems(itemEntities)
    }

    override suspend fun updateTransactionTotals(
        transactionId: String,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double
    ) = withContext(Dispatchers.IO) {
        transactionDao.updateTransactionTotals(transactionId, subtotal, tax, discount, total)
    }

    override suspend fun updateTransactionPayment(
        transactionId: String,
        paymentMethod: PaymentMethod,
        globalDiscount: Double
    ) = withContext(Dispatchers.IO) {
        transactionDao.updateTransactionPayment(transactionId, paymentMethod, globalDiscount)
    }

    override suspend fun finalizeTransaction(
        transactionId: String,
        cashReceived: Double,
        cashChange: Double,
        notes: String?
    ) = withContext(Dispatchers.IO) {
        // Update status to PAID and set cash details
        transactionDao.finalizeTransaction(transactionId, cashReceived, cashChange, notes, TransactionStatus.PAID)

        // Decrement stock for all items
        val items = transactionItemDao.getItemsByTransactionId(transactionId)
        items.forEach { item ->
            productDao.decrementStock(item.productId, item.quantity)
        }
    }

    override suspend fun completeTransaction(transactionId: String) = withContext(Dispatchers.IO) {
        transactionDao.updateTransactionStatus(transactionId, TransactionStatus.COMPLETED)
    }

    override fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long, onlyCompleted: Boolean): Flow<List<TransactionEntity>> {
        return if (onlyCompleted) transactionDao.getTransactionsByDateRange(startDate, endDate)
        else transactionDao.getTransactionsByDateRangeAllStatus(startDate, endDate)
    }

    override fun getTransactionsByDate(date: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByDate(date)

    override suspend fun softDeleteTransaction(transactionId: String) = withContext(Dispatchers.IO) {
        transactionDao.softDeleteTransaction(transactionId)
    }
}
