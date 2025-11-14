package id.stargan.intikasir.domain.model

/**
 * Domain model untuk Store Settings
 */
data class StoreSettings(
    val id: String = "store_settings",
    val storeName: String = "",
    val storeAddress: String = "",
    val storePhone: String = "",
    val storeEmail: String? = null,
    val storeLogo: String? = null,
    val taxEnabled: Boolean = false,
    val taxPercentage: Double = 0.0,
    val taxName: String = "PPN",
    val serviceEnabled: Boolean = false,
    val servicePercentage: Double = 0.0,
    val serviceName: String = "Servis",
    val receiptHeader: String? = null,
    val receiptFooter: String? = null,
    val printLogo: Boolean = false,
    val printerName: String? = null,
    val printerAddress: String? = null,
    val printerConnected: Boolean = false,
    val currencySymbol: String = "Rp",
    val currencyCode: String = "IDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

