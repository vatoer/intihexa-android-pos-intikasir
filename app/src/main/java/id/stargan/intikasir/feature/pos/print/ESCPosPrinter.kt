package id.stargan.intikasir.feature.pos.print

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.domain.model.StoreSettings
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

/**
 * Minimal ESC/POS printer helper (Bluetooth RFCOMM)
 * NOTE: This is a basic stub focusing on text output.
 */
object ESCPosPrinter {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun printReceipt(
        context: Context,
        settings: StoreSettings,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ) {
        val mac = settings.printerAddress ?: return
        val sdk31Plus = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
        if (sdk31Plus) {
            val permissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) return // Permission not granted; silently abort
        }
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val device: BluetoothDevice = adapter.getRemoteDevice(mac)
        val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        adapter.cancelDiscovery()
        try {
            socket.connect()
            socket.outputStream.use { out ->
                writeReceipt(out, settings, transaction, items)
            }
        } catch (_: Exception) {
            // swallow for now; could log
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun writeReceipt(
        out: OutputStream,
        settings: StoreSettings,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
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

        // Header
        alignCenter(); boldOn(); text((settings.storeName.ifBlank { "Toko" }).take(cpl)); boldOff()
        settings.storeAddress.takeIf { it.isNotBlank() }?.let { text(it.take(cpl)) }
        divider()

        // Items
        alignLeft()
        items.forEach { itx ->
            text(itx.productName.take(cpl))
            val qty = "${itx.quantity} x ${nf.format(itx.unitPrice).replace("Rp", "Rp ")}"
            val sub = nf.format(itx.subtotal).replace("Rp", "Rp ")
            val pad = (cpl - qty.length - sub.length).coerceAtLeast(1)
            text(qty + " ".repeat(pad) + sub)
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
        alignCenter(); text("Terima kasih")

        // Feed and optional cut
        cmd(byteArrayOf(0x1B, 0x64, 0x03)) // feed 3 lines
        if (settings.autoCut) cut()
    }

    fun printQueueTicket(
        context: Context,
        settings: StoreSettings,
        transaction: TransactionEntity
    ) {
        val mac = settings.printerAddress ?: return
        val sdk31Plus = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
        if (sdk31Plus) {
            val permissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) return
        }
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val device: BluetoothDevice = adapter.getRemoteDevice(mac)
        val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        adapter.cancelDiscovery()
        try {
            socket.connect()
            socket.outputStream.use { out ->
                writeQueueTicket(out, settings, transaction)
            }
        } catch (_: Exception) {
        } finally {
            try { socket.close() } catch (_: Exception) {}
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
