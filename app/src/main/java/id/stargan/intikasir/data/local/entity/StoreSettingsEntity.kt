package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk pengaturan toko (single row)
 */
@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey
    val id: String = "store_settings", // Always single row

    // Store Information
    val storeName: String = "",
    val storeAddress: String = "",
    val storePhone: String = "",
    val storeEmail: String? = null,
    val storeLogo: String? = null,

    // Tax & Service
    val taxEnabled: Boolean = false,
    val taxPercentage: Double = 0.0, // e.g., 10.0 for 10% PPN
    val taxName: String = "PPN", // e.g., "PPN", "VAT"

    val serviceEnabled: Boolean = false,
    val servicePercentage: Double = 0.0, // e.g., 5.0 for 5% service charge
    val serviceName: String = "Servis", // e.g., "Service", "Servis"

    // Receipt Settings
    val receiptHeader: String? = null, // Custom header text
    val receiptFooter: String? = null, // Custom footer text (e.g., "Terima kasih")
    val printLogo: Boolean = false,

    // Printer Settings
    val printerName: String? = null,
    val printerAddress: String? = null, // Bluetooth MAC address
    val printerConnected: Boolean = false,
    val printFormat: String = "THERMAL", // THERMAL or A4
    val autoCut: Boolean = true,
    val useEscPosDirect: Boolean = false, // Direct Bluetooth ESC/POS printing

    // Currency
    val currencySymbol: String = "Rp",
    val currencyCode: String = "IDR",

    // Paper Size Configuration
    val paperWidthMm: Int = 58, // thermal paper width (58 or 80)
    val paperCharPerLine: Int = 32, // derived default for 58mm, 48 for 80mm

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
