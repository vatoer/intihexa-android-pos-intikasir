package id.stargan.intikasir.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Helper untuk menyimpan logo dalam format yang siap print ke thermal printer
 * Logo di-convert sekali saat save, tidak perlu convert ulang setiap print
 */
object ThermalLogoHelper {
    private const val TAG = "ThermalLogoHelper"
    private const val THERMAL_LOGO_FILENAME = "thermal_logo.bin"
    private const val ORIGINAL_LOGO_FILENAME = "store_logo.jpg"

    /**
     * Save original logo dan generate thermal-ready bitmap
     * @return Path to original logo file
     */
    fun saveLogoAndGenerateThermal(
        context: Context,
        originalBitmap: Bitmap,
        paperWidthChars: Int = 32
    ): String? {
        try {
            val filesDir = context.filesDir

            // Save original logo (for preview)
            val originalFile = File(filesDir, ORIGINAL_LOGO_FILENAME)
            FileOutputStream(originalFile).use { out ->
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // Generate thermal-ready bitmap
            generateThermalBitmap(context, originalBitmap, paperWidthChars)

            Log.d(TAG, "Logo saved: ${originalFile.absolutePath}")
            return originalFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save logo", e)
            return null
        }
    }

    /**
     * Generate thermal-ready bitmap dari original
     * Simpan dalam format binary siap print
     */
    private fun generateThermalBitmap(
        context: Context,
        originalBitmap: Bitmap,
        paperWidthChars: Int
    ) {
        try {
            // Limit logo size to reasonable dimensions for receipt
            // Max width: 80 dots (about 10mm for 58mm printer, 13mm for 80mm printer)
            // Max height: 80 dots (to not take too much space on receipt)
            val maxDots = min(80, paperWidthChars * 8) // Reasonable max
            val maxHeight = 80 // Max height in dots

            val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()

            // Calculate target dimensions
            var targetWidth = min(maxDots, originalBitmap.width)
            var targetHeight = (targetWidth / ratio).toInt().coerceAtLeast(1)

            // If height exceeds max, recalculate based on height
            if (targetHeight > maxHeight) {
                targetHeight = maxHeight
                targetWidth = (targetHeight * ratio).toInt().coerceAtLeast(1)
            }

            Log.d(TAG, "Logo scaling: ${originalBitmap.width}x${originalBitmap.height} -> ${targetWidth}x${targetHeight}")

            val scaled = if (originalBitmap.width != targetWidth || originalBitmap.height != targetHeight) {
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            // Convert to monochrome dots array
            val threshold = 128
            val dots = Array(scaled.height) { y ->
                BooleanArray(scaled.width) { x ->
                    val pixel = scaled.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    val gray = (r + g + b) / 3
                    gray < threshold
                }
            }

            // Save thermal data to file
            val thermalFile = File(context.filesDir, THERMAL_LOGO_FILENAME)
            FileOutputStream(thermalFile).use { out ->
                // Write metadata
                out.write(byteArrayOf(
                    (scaled.width and 0xFF).toByte(),
                    ((scaled.width shr 8) and 0xFF).toByte(),
                    (scaled.height and 0xFF).toByte(),
                    ((scaled.height shr 8) and 0xFF).toByte()
                ))

                // Write dot data in ESC/POS format (strips of 24 dots)
                var y = 0
                while (y < dots.size) {
                    val stripHeight = min(24, dots.size - y)

                    // Write strip data column by column
                    for (x in 0 until scaled.width) {
                        val bytes = ByteArray(3)
                        for (k in 0 until stripHeight) {
                            val dotY = y + k
                            if (dotY < dots.size && x < dots[dotY].size && dots[dotY][x]) {
                                val byteIndex = k / 8
                                val bitIndex = 7 - (k % 8)
                                bytes[byteIndex] = (bytes[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                            }
                        }
                        out.write(bytes)
                    }
                    y += 24
                }
            }

            if (scaled != originalBitmap) {
                scaled.recycle()
            }

            Log.d(TAG, "Thermal bitmap generated: ${thermalFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate thermal bitmap", e)
        }
    }

    /**
     * Check if thermal logo exists
     */
    fun hasThermalLogo(context: Context): Boolean {
        val file = File(context.filesDir, THERMAL_LOGO_FILENAME)
        return file.exists() && file.length() > 4 // metadata minimal 4 bytes
    }

    /**
     * Print pre-generated thermal logo
     * Tinggal baca file dan kirim ke printer, tidak perlu convert lagi
     */
    fun printThermalLogo(context: Context, outputStream: java.io.OutputStream): Boolean {
        try {
            val thermalFile = File(context.filesDir, THERMAL_LOGO_FILENAME)
            if (!thermalFile.exists()) {
                Log.w(TAG, "Thermal logo file not found at: ${thermalFile.absolutePath}")
                return false
            }

            val data = thermalFile.readBytes()
            if (data.size < 4) {
                Log.w(TAG, "Invalid thermal logo file, size: ${data.size}")
                return false
            }

            // Read metadata
            val width = (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)
            val height = (data[2].toInt() and 0xFF) or ((data[3].toInt() and 0xFF) shl 8)

            Log.d(TAG, "Printing thermal logo: ${width}x${height}")

            // Center align for logo
            outputStream.write(byteArrayOf(0x1B, 0x61, 0x01))
            outputStream.flush()

            // Print strips of 24 dots
            var offset = 4
            var y = 0
            while (y < height) {
                val stripHeight = min(24, height - y)

                // ESC * command
                val nL = (width and 0xFF).toByte()
                val nH = ((width shr 8) and 0xFF).toByte()
                outputStream.write(byteArrayOf(0x1B, 0x2A, 33, nL, nH))

                // Write strip data
                val bytesPerColumn = 3
                val stripSize = width * bytesPerColumn
                if (offset + stripSize <= data.size) {
                    outputStream.write(data, offset, stripSize)
                    offset += stripSize
                } else {
                    Log.w(TAG, "Strip data out of bounds at offset $offset")
                }

                // Line feed
                outputStream.write(byteArrayOf(0x0A))
                y += 24
            }

            // IMPORTANT: Reset to left align after logo
            outputStream.write(byteArrayOf(0x1B, 0x61, 0x00))
            outputStream.flush()

            Log.d(TAG, "Thermal logo printed successfully, alignment reset to left")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print thermal logo", e)
            // On error, ensure we reset alignment
            try {
                outputStream.write(byteArrayOf(0x1B, 0x61, 0x00))
                outputStream.flush()
            } catch (ignored: Exception) {}
            return false
        }
    }

    /**
     * Get original logo file path (for preview)
     */
    fun getOriginalLogoPath(context: Context): String? {
        val file = File(context.filesDir, ORIGINAL_LOGO_FILENAME)
        return if (file.exists()) file.absolutePath else null
    }

    /**
     * Delete logo files
     */
    fun deleteLogo(context: Context) {
        try {
            File(context.filesDir, ORIGINAL_LOGO_FILENAME).delete()
            File(context.filesDir, THERMAL_LOGO_FILENAME).delete()
            Log.d(TAG, "Logo files deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete logo files", e)
        }
    }
}

