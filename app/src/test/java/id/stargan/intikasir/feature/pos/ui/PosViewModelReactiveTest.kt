package id.stargan.intikasir.feature.pos.ui

import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.TransactionStatus
import id.stargan.intikasir.feature.pos.domain.TransactionRepository
import id.stargan.intikasir.feature.product.domain.usecase.GetAllProductsUseCase
import id.stargan.intikasir.feature.settings.domain.usecase.GetStoreSettingsUseCase
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import id.stargan.intikasir.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Fake ProductRepository for use case
private class FakeProductRepository : ProductRepository {
    private val productsFlow = MutableStateFlow<List<Product>>(emptyList())
    fun setProducts(list: List<Product>) { productsFlow.value = list }
    override fun getAllProducts(): Flow<List<Product>> = productsFlow
    // Unused methods can throw or return empty
    override fun getProductById(id: String): Flow<Product?> = flowOf(productsFlow.value.find { it.id == id })
    override fun searchProducts(query: String): Flow<List<Product>> = flowOf(productsFlow.value.filter { it.name.contains(query, true) })
    override suspend fun upsertProduct(product: Product) { /* no-op */ }
    override suspend fun deleteProduct(id: String) { /* no-op */ }
}

// Fake SettingsRepository
private class FakeSettingsRepository : SettingsRepository {
    private val settingsFlow = MutableStateFlow<StoreSettings?>(
        StoreSettings(
            id = "settings",
            storeName = "Toko",
            address = null,
            phone = null,
            taxEnabled = false,
            taxPercentage = 0.0,
            footerNote = null,
            logoPath = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    )
    override fun getStoreSettings(): Flow<StoreSettings?> = settingsFlow
    override suspend fun saveStoreSettings(settings: StoreSettings) { settingsFlow.value = settings }
}

// Fake TransactionRepository
private class FakeTransactionRepository : TransactionRepository {
    val transactions = mutableMapOf<String, TransactionEntity>()
    val itemsMap = mutableMapOf<String, MutableList<TransactionItemEntity>>()
    private var idCounter = 0

    override suspend fun createEmptyDraft(cashierId: String, cashierName: String): String {
        val id = "tx_${++idCounter}"
        val tx = TransactionEntity(
            id = id,
            transactionNumber = "DRAFT-$idCounter",
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
        transactions[id] = tx
        itemsMap[id] = mutableListOf()
        return id
    }

    override fun getTransactionById(transactionId: String): Flow<TransactionEntity?> = flowOf(transactions[transactionId])
    override fun getTransactionItems(transactionId: String): Flow<List<TransactionItemEntity>> = flowOf(itemsMap[transactionId] ?: emptyList())

    override suspend fun updateTransactionItems(
        transactionId: String,
        items: List<Pair<String, Int>>,
        itemDiscounts: Map<String, Double>
    ) {
        val list = mutableListOf<TransactionItemEntity>()
        items.forEach { (pid, qty) ->
            list += TransactionItemEntity(
                transactionId = transactionId,
                productId = pid,
                productName = pid,
                productPrice = 1000.0,
                productSku = "SKU-$pid",
                quantity = qty,
                unitPrice = 1000.0,
                discount = itemDiscounts[pid] ?: 0.0,
                subtotal = (1000.0 * qty) - (itemDiscounts[pid] ?: 0.0)
            )
        }
        itemsMap[transactionId] = list
    }

    override suspend fun updateTransactionTotals(transactionId: String, subtotal: Double, tax: Double, discount: Double, total: Double) {
        transactions[transactionId] = transactions[transactionId]!!.copy(subtotal = subtotal, tax = tax, discount = discount, total = total)
    }

    override suspend fun updateTransactionPayment(transactionId: String, paymentMethod: PaymentMethod, globalDiscount: Double) {
        transactions[transactionId] = transactions[transactionId]!!.copy(paymentMethod = paymentMethod, discount = globalDiscount)
    }

    override suspend fun finalizeTransaction(transactionId: String, cashReceived: Double, cashChange: Double, notes: String?) {}
    override suspend fun completeTransaction(transactionId: String) {}
    override suspend fun softDeleteTransaction(transactionId: String) {}
    override suspend fun createTransaction(cashierId: String, cashierName: String, items: List<Pair<String, Int>>, paymentMethod: PaymentMethod, subtotal: Double, tax: Double, discount: Double, total: Double, cashReceived: Double, cashChange: Double, notes: String?, status: TransactionStatus): String = ""
    override suspend fun createDraftTransaction(cashierId: String, cashierName: String, items: List<Pair<String, Int>>, paymentMethod: PaymentMethod, subtotal: Double, tax: Double, discount: Double, total: Double, notes: String?): String = ""
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long, onlyCompleted: Boolean): Flow<List<TransactionEntity>> = flowOf(emptyList())
    override fun getTransactionsByDate(date: Long): Flow<List<TransactionEntity>> = flowOf(emptyList())
    override fun getAllTransactions(): Flow<List<TransactionEntity>> = flowOf(emptyList())
}

class PosViewModelReactiveTest {
    private lateinit var productRepo: FakeProductRepository
    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var productsUseCase: GetAllProductsUseCase
    private lateinit var settingsUseCase: GetStoreSettingsUseCase
    private lateinit var transactionRepo: FakeTransactionRepository
    private lateinit var vm: PosViewModelReactive

    @Before
    fun setup() {
        productRepo = FakeProductRepository()
        settingsRepo = FakeSettingsRepository()
        productsUseCase = GetAllProductsUseCase(productRepo)
        settingsUseCase = GetStoreSettingsUseCase(settingsRepo)
        transactionRepo = FakeTransactionRepository()
        vm = PosViewModelReactive(productsUseCase, transactionRepo, settingsUseCase)
        val now = System.currentTimeMillis()
        productRepo.setProducts(
            listOf(
                Product(
                    id = "p1",
                    name = "Produk 1",
                    sku = "SKU1",
                    barcode = null,
                    categoryId = null,
                    categoryName = null,
                    description = null,
                    price = 1000.0,
                    cost = null,
                    stock = 10,
                    minStock = null,
                    lowStockThreshold = 2,
                    imageUrl = null,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
        runBlocking { vm.initializeTransaction("cashier", "Kasir") }
    }

    @Test
    fun recalcTotals_afterAddItem_noDiscount() {
        runBlocking {
            vm.addOrIncrement("p1")
            val state = vm.uiState.value
            assertEquals(1000.0, state.subtotal, 0.001)
            assertEquals(1000.0, state.total, 0.001)
        }
    }

    @Test
    fun setItemDiscount_perUnitAppliesForQuantityIncrease() {
        runBlocking {
            vm.addOrIncrement("p1") // qty 1
            vm.setItemDiscount("p1", 200.0) // per unit discount 200 => totalDiscount 200
            vm.addOrIncrement("p1") // qty 2, totalDiscount should become 400
            val item = vm.uiState.value.transactionItems.first()
            assertEquals(2, item.quantity)
            assertEquals(400.0, item.discount, 0.001)
            assertEquals((1000.0 * 2) - 400.0, item.subtotal, 0.001)
        }
    }

    @Test
    fun setQuantity_preservesPerUnitDiscount() {
        runBlocking {
            vm.addOrIncrement("p1")
            vm.setItemDiscount("p1", 100.0) // total 100 on qty 1
            vm.addOrIncrement("p1") // qty 2 -> total discount should be 200
            vm.setQuantity("p1", 3) // qty 3 -> total discount should be 300
            val item = vm.uiState.value.transactionItems.first()
            assertEquals(3, item.quantity)
            assertEquals(300.0, item.discount, 0.001)
            assertEquals((1000.0 * 3) - 300.0, item.subtotal, 0.001)
        }
    }
}
