package id.stargan.intikasir.feature.pos.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import id.stargan.intikasir.domain.model.StoreSettings
import java.io.File
import java.io.OutputStream
import kotlin.math.ceil

/**
 * Helper untuk mencetak logo pada thermal printer menggunakan perintah ESC/POS
 * Updated: Menggunakan Raster Bit Image (GS v 0) dengan metode chunking untuk mencegah buffer overflow.
 */
object ThermalLogoPrinter {
    private const val TAG = "ThermalLogoPrinter"

    // ESC/POS Commands
    private val ESC_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 1)
    private val ESC_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0)
    private val CMD_GS_v_0 = byteArrayOf(0x1D, 0x76, 0x30, 0x00) // Raster bit image mode normal

    // --- PERUBAHAN DI SINI ---
    // Ukuran chunk yang lebih kecil untuk printer yang sangat sensitif
    private const val RASTER_LINE_CHUNK_SIZE = 32 // <-- Diturunkan dari 64 menjadi 32
    // Jeda waktu antar chunk dalam milidetik untuk memberi "napas" pada printer
    private const val CHUNK_SLEEP_DELAY_MS = 20L

    /**
     * Print logo to thermal printer
     * (Fungsi printLogo tetap sama, tidak perlu diubah)
     */
    fun printLogo(
        context: Context,
        out: OutputStream,
        settings: StoreSettings
    ): Boolean {
        if (!settings.printLogo || settings.storeLogo.isNullOrBlank()) {
            Log.d(TAG, "Logo printing disabled or logo path empty")
            return false
        }

        try {
            val logoFile = File(settings.storeLogo)
            if (!logoFile.exists()) {
                Log.w(TAG, "Logo file does not exist: ${settings.storeLogo}")
                return false
            }

            val originalBitmap = BitmapFactory.decodeFile(settings.storeLogo) ?: run {
                Log.w(TAG, "Failed to decode logo bitmap")
                return false
            }

            val maxPrinterWidth = if (settings.paperWidthMm >= 80) 576 else 384
            val targetWidth = (maxPrinterWidth * 0.60).toInt()

            val aspectRatio = originalBitmap.height.toFloat() / originalBitmap.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

            Log.d(TAG, "Printing Logo: ${scaledBitmap.width}x${scaledBitmap.height} on paper width $maxPrinterWidth")

            printBitmapRaster(out, scaledBitmap)

            scaledBitmap.recycle()
            originalBitmap.recycle()

            Log.d(TAG, "Logo printed successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error printing logo", e)
            return false
        }
    }

    /**
     * Print bitmap image centered using GS v 0 (Raster Bit Image) dengan metode chunking yang lebih agresif.
     */
    private fun printBitmapRaster(out: OutputStream, bitmap: Bitmap) {
        try {
            out.write(ESC_ALIGN_CENTER)

            val width = bitmap.width
            val height = bitmap.height
            val widthBytes = ceil(width.toDouble() / 8).toInt()

            var currentY = 0
            while (currentY < height) {
                val chunkHeight = (height - currentY).coerceAtMost(RASTER_LINE_CHUNK_SIZE)

                out.write(CMD_GS_v_0)
                out.write(widthBytes % 256)
                out.write(widthBytes / 256)
                out.write(chunkHeight % 256)
                out.write(chunkHeight / 256)

                val data = ByteArray(widthBytes * chunkHeight)
                var k = 0
                for (y in currentY until currentY + chunkHeight) {
                    for (xByte in 0 until widthBytes) {
                        var byteValue = 0
                        for (bit in 0 until 8) {
                            val currentX = xByte * 8 + bit
                            if (currentX < width) {
                                val pixel = bitmap.getPixel(currentX, y)
                                val r = Color.red(pixel)
                                val g = Color.green(pixel)
                                val b = Color.blue(pixel)
                                val luminance = (0.299 * r + 0.587 * g + 0.114 * b)

                                if (Color.alpha(pixel) > 50 && luminance < 128) {
                                    byteValue = byteValue or (1 shl (7 - bit))
                                }
                            }
                        }
                        data[k++] = byteValue.toByte()
                    }
                }

                out.write(data)
                out.flush()

                // --- PERUBAHAN DI SINI ---
                // Tambahkan jeda singkat antar chunk
                Thread.sleep(CHUNK_SLEEP_DELAY_MS)

                currentY += chunkHeight
            }

            out.write(ESC_ALIGN_LEFT)
            out.flush()

        } catch (e: Exception) {
            Log.e(TAG, "Error inside printBitmapRaster", e)
        }
    }
}
