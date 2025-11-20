package id.stargan.intikasir.feature.pos.print

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.util.BluetoothPermissionHelper
import java.io.File
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import kotlin.math.min

/**
 * Minimal ESC/POS printer helper (Bluetooth RFCOMM)
 * NOTE: This is a basic stub focusing on text output.
 */
object ESCPosPrinter {
    private const val TAG = "ESCPosPrinter"
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    sealed class PrintResult {
        object Success : PrintResult()
        data class Error(val message: String) : PrintResult()
    }

    fun printReceipt(
        context: Context,
        settings: StoreSettings,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ): PrintResult {
        try {
            val mac = settings.printerAddress
            if (mac.isNullOrBlank()) {
                Log.w(TAG, "printReceipt: No printer address configured")
                return PrintResult.Error("Printer belum dikonfigurasi")
            }

            if (!BluetoothPermissionHelper.hasBluetoothPermissions(context)) {
                Log.w(TAG, "printReceipt: Bluetooth permissions not granted")
                return PrintResult.Error("Izin Bluetooth diperlukan untuk mencetak")
            }

            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null) {
                Log.e(TAG, "printReceipt: BluetoothAdapter is null")
                return PrintResult.Error("Bluetooth tidak tersedia")
            }

            if (!adapter.isEnabled) {
                Log.w(TAG, "printReceipt: Bluetooth is disabled")
                return PrintResult.Error("Bluetooth tidak aktif")
            }

            val device: BluetoothDevice = try {
                adapter.getRemoteDevice(mac)
            } catch (e: Exception) {
                Log.e(TAG, "printReceipt: Failed to get remote device", e)
                return PrintResult.Error("Printer tidak ditemukan: ${e.message}")
            }

            val socket: BluetoothSocket = try {
                device.createRfcommSocketToServiceRecord(SPP_UUID)
            } catch (e: Exception) {
                Log.e(TAG, "printReceipt: Failed to create socket", e)
                return PrintResult.Error("Gagal membuat koneksi: ${e.message}")
            }

            // cancelDiscovery may require BLUETOOTH_SCAN on newer SDKs; check before calling
            if (BluetoothPermissionHelper.hasBluetoothScanPermission(context)) {
                try {
                    adapter.cancelDiscovery()
                } catch (e: Exception) {
                    Log.w(TAG, "printReceipt: Failed to cancel discovery", e)
                }
            }

            try {
                Log.d(TAG, "printReceipt: Connecting to printer...")
                socket.connect()
                Log.d(TAG, "printReceipt: Connected, sending data...")
                socket.outputStream.use { out ->
                    writeReceipt(out, settings, transaction, items, context)
                }
                Log.i(TAG, "printReceipt: Print successful")
                return PrintResult.Success
            } catch (e: Exception) {
                Log.e(TAG, "printReceipt: Failed to print", e)
                return PrintResult.Error("Gagal mencetak: ${e.message}")
            } finally {
                try {
                    socket.close()
                } catch (e: Exception) {
                    Log.w(TAG, "printReceipt: Failed to close socket", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "printReceipt: Unexpected error", e)
            return PrintResult.Error("Kesalahan tidak terduga: ${e.message}")
        }
    }

    private fun writeReceipt(
        out: OutputStream,
        settings: StoreSettings,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>,
        context: Context
    ) {
        val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
        val cpl = settings.paperCharPerLine
        val charset = Charset.forName("US-ASCII")

        fun cmd(bytes: ByteArray) = out.write(bytes)
        fun text(line: String, nl: Boolean = true) {
            out.write(line.toByteArray(charset))
            if (nl) out.write(byteArrayOf(0x0A)) // LF
        }
        fun divider() = text("-".repeat(cpl))
        fun alignCenter() = cmd(byteArrayOf(0x1B, 0x61, 0x01))
        fun alignLeft() = cmd(byteArrayOf(0x1B, 0x61, 0x00))
        fun boldOn() = cmd(byteArrayOf(0x1B, 0x45, 0x01))
        fun boldOff() = cmd(byteArrayOf(0x1B, 0x45, 0x00))
        fun cut() = cmd(byteArrayOf(0x1D, 0x56, 0x00)) // simple cut, may vary

        // Initialize
        cmd(byteArrayOf(0x1B, 0x40))

        // Logo (if enabled and available)
        if (settings.printLogo) {
            val logoSuccess = ThermalLogoPrinter.printLogo(context, out, settings)
            if (logoSuccess) {
                Log.d(TAG, "writeReceipt: Logo printed successfully")
            } else {
                Log.w(TAG, "writeReceipt: Logo printing failed or disabled, continuing without logo")
            }
        }

        // Header - always print regardless of logo
        alignCenter(); boldOn(); text((settings.storeName.ifBlank { "Toko" }).take(cpl)); boldOff()
        settings.storeAddress.takeIf { it.isNotBlank() }?.let { text(it.take(cpl)) }
        divider()

        // Transaction Number
        alignLeft()
        text("No: ${transaction.transactionNumber}")
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("id", "ID"))
        text("Tgl: ${dateFormat.format(java.util.Date(transaction.transactionDate))}")
        text("Kasir: ${transaction.cashierName}")
        divider()

        // Check payment status and add notice if not PAID or COMPLETED
        val isPaidOrCompleted = transaction.status == id.stargan.intikasir.data.local.entity.TransactionStatus.PAID ||
                                transaction.status == id.stargan.intikasir.data.local.entity.TransactionStatus.COMPLETED
        if (!isPaidOrCompleted) {
            alignCenter()
            boldOn()
            text("*** BELUM DIBAYAR ***")
            boldOff()
            divider()
        }

        // Items
        alignLeft()
        items.forEach { itx ->
            // Product name
            text(itx.productName.take(cpl))

            // If item has discount, show original price and discounted price per unit
            if (itx.discount > 0) {
                val originalPrice = itx.productPrice
                val discountPerUnit = itx.discount / itx.quantity
                val discountedPricePerUnit = originalPrice - discountPerUnit

                // Original price (with @ prefix to indicate strikethrough)
                val origPriceStr = "@${nf.format(originalPrice).replace("Rp", "Rp ")}/pcs"
                text("  $origPriceStr")

                // Quantity x discounted price per unit = subtotal
                val qty = "${itx.quantity} x ${nf.format(discountedPricePerUnit).replace("Rp", "Rp ")}"
                val sub = nf.format(itx.subtotal).replace("Rp", "Rp ")
                val pad = (cpl - qty.length - sub.length).coerceAtLeast(1)
                text(qty + " ".repeat(pad) + sub)

                // Total discount for all units
                val discountStr = "  Diskon: -${nf.format(itx.discount).replace("Rp", "Rp ")}"
                text(discountStr)
            } else {
                // No discount - simple format
                val qty = "${itx.quantity} x ${nf.format(itx.unitPrice).replace("Rp", "Rp ")}"
                val sub = nf.format(itx.subtotal).replace("Rp", "Rp ")
                val pad = (cpl - qty.length - sub.length).coerceAtLeast(1)
                text(qty + " ".repeat(pad) + sub)
            }
        }
        divider()

        fun totalLine(label: String, value: String) {
            val pad = (cpl - label.length - value.length).coerceAtLeast(1)
            text(label + " ".repeat(pad) + value)
        }

        totalLine("Subtotal", nf.format(transaction.subtotal).replace("Rp", "Rp "))
        if (transaction.tax > 0) totalLine("PPN", nf.format(transaction.tax).replace("Rp", "Rp "))
        if (transaction.discount > 0) totalLine("Diskon", "-" + nf.format(transaction.discount).replace("Rp", "Rp "))
        boldOn(); totalLine("TOTAL", nf.format(transaction.total).replace("Rp", "Rp ")); boldOff()

        val received = transaction.cashReceived
        if (received > 0) {
            totalLine("Tunai", nf.format(received).replace("Rp", "Rp "))
            val change = (received - transaction.total).coerceAtLeast(0.0)
            totalLine("Kembali", nf.format(change).replace("Rp", "Rp "))
        }

        divider()

        // Add payment status notice at the bottom if not paid/completed
        if (!isPaidOrCompleted) {
            alignCenter()
            boldOn()
            text("*** BELUM DIBAYAR ***")
            boldOff()
            divider()
        }

        alignCenter(); text("Terima kasih")

        // Feed and optional cut
        cmd(byteArrayOf(0x1B, 0x64, 0x03)) // feed 3 lines
        if (settings.autoCut) cut()
    }

    fun printQueueTicket(
        context: Context,
        settings: StoreSettings,
        transaction: TransactionEntity
    ): PrintResult {
        try {
            val mac = settings.printerAddress
            if (mac.isNullOrBlank()) {
                Log.w(TAG, "printQueueTicket: No printer address configured")
                return PrintResult.Error("Printer belum dikonfigurasi")
            }

            if (!BluetoothPermissionHelper.hasBluetoothPermissions(context)) {
                Log.w(TAG, "printQueueTicket: Bluetooth permissions not granted")
                return PrintResult.Error("Izin Bluetooth diperlukan untuk mencetak")
            }

            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null) {
                Log.e(TAG, "printQueueTicket: BluetoothAdapter is null")
                return PrintResult.Error("Bluetooth tidak tersedia")
            }

            if (!adapter.isEnabled) {
                Log.w(TAG, "printQueueTicket: Bluetooth is disabled")
                return PrintResult.Error("Bluetooth tidak aktif")
            }

            val device: BluetoothDevice = try {
                adapter.getRemoteDevice(mac)
            } catch (e: Exception) {
                Log.e(TAG, "printQueueTicket: Failed to get remote device", e)
                return PrintResult.Error("Printer tidak ditemukan: ${e.message}")
            }

            val socket: BluetoothSocket = try {
                device.createRfcommSocketToServiceRecord(SPP_UUID)
            } catch (e: Exception) {
                Log.e(TAG, "printQueueTicket: Failed to create socket", e)
                return PrintResult.Error("Gagal membuat koneksi: ${e.message}")
            }

            if (BluetoothPermissionHelper.hasBluetoothScanPermission(context)) {
                try {
                    adapter.cancelDiscovery()
                } catch (e: Exception) {
                    Log.w(TAG, "printQueueTicket: Failed to cancel discovery", e)
                }
            }

            try {
                Log.d(TAG, "printQueueTicket: Connecting to printer...")
                socket.connect()
                Log.d(TAG, "printQueueTicket: Connected, sending data...")
                socket.outputStream.use { out ->
                    writeQueueTicket(out, settings, transaction)
                }
                Log.i(TAG, "printQueueTicket: Print successful")
                return PrintResult.Success
            } catch (e: Exception) {
                Log.e(TAG, "printQueueTicket: Failed to print", e)
                return PrintResult.Error("Gagal mencetak antrian: ${e.message}")
            } finally {
                try {
                    socket.close()
                } catch (e: Exception) {
                    Log.w(TAG, "printQueueTicket: Failed to close socket", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "printQueueTicket: Unexpected error", e)
            return PrintResult.Error("Kesalahan tidak terduga: ${e.message}")
        }
    }

    private fun writeQueueTicket(
        out: OutputStream,
        settings: StoreSettings,
        transaction: TransactionEntity
    ) {
        val cpl = settings.paperCharPerLine
        val charset = Charset.forName("US-ASCII")
        val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())

        fun cmd(bytes: ByteArray) = out.write(bytes)
        fun text(line: String = "", nl: Boolean = true) {
            out.write(line.toByteArray(charset))
            if (nl) out.write(byteArrayOf(0x0A))
        }
        fun alignCenter() = cmd(byteArrayOf(0x1B, 0x61, 0x01))
        fun alignLeft() = cmd(byteArrayOf(0x1B, 0x61, 0x00))
        fun boldOn() = cmd(byteArrayOf(0x1B, 0x45, 0x01))
        fun boldOff() = cmd(byteArrayOf(0x1B, 0x45, 0x00))
        fun dblOn() = cmd(byteArrayOf(0x1D, 0x21, 0x11)) // double height & width
        fun dblOff() = cmd(byteArrayOf(0x1D, 0x21, 0x00))
        fun divider() = text("-".repeat(cpl))
        fun feed(n: Int) = cmd(byteArrayOf(0x1B, 0x64, n.toByte()))
        fun cut() = cmd(byteArrayOf(0x1D, 0x56, 0x00))

        // init
        cmd(byteArrayOf(0x1B, 0x40))

        // Header
        alignCenter(); boldOn(); text("NOMOR ANTRIAN"); boldOff()
        val queueNo = transaction.transactionNumber.substringAfterLast('-').takeLast(4).padStart(4, '0')
        dblOn(); text(queueNo); dblOff()
        text() // blank line

        // Details
        alignLeft()
        text("Transaksi : ${transaction.transactionNumber}")
        val date = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID")).format(java.util.Date(transaction.transactionDate))
        text("Waktu     : $date")
        text("Total     : ${nf.format(transaction.total).replace("Rp", "Rp ")}")

        text(); divider(); alignCenter(); text("Terima kasih"); feed(3)
        if (settings.autoCut) cut()
    }
}
