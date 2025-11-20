package id.stargan.intikasir.feature.pos.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import id.stargan.intikasir.domain.model.StoreSettings
import java.io.File
import java.io.OutputStream
import kotlin.math.min

/**
 * Helper untuk mencetak logo pada thermal printer
 * Mengatur ukuran logo: jika lebar kertas = 3a, maka logo = a x a (square, 1/3 dari lebar kertas)
 */
object ThermalLogoPrinter {
    private const val TAG = "ThermalLogoPrinter"

    /**
     * Print logo to thermal printer via ESC/POS commands
     * Logo size calculation: if paper width = 3a, then logo = a x a
     *
     * @param context Android context
     * @param out OutputStream to thermal printer
     * @param settings Store settings containing logo path and paper width
     * @return true if logo printed successfully, false otherwise
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

            val originalBitmap = BitmapFactory.decodeFile(settings.storeLogo)
            if (originalBitmap == null) {
                Log.w(TAG, "Failed to decode logo bitmap")
                return false
            }

            Log.d(TAG, "Original bitmap size: ${originalBitmap.width}x${originalBitmap.height}")

            // Calculate logo size based on paper width
            // Paper width in mm, convert to dots (thermal printers typically 8 dots per mm)
            val paperWidthMm = settings.paperWidthMm
            val dotsPerMm = 8 // Standard thermal printer resolution
            val paperWidthDots = paperWidthMm * dotsPerMm

            // Logo should be 1/3 of paper width (square)
            // Reduced max size to prevent buffer overflow: 58mm->100 dots, 80mm->150 dots
            val targetLogoSize = when {
                paperWidthMm >= 80 -> 150 // ~19mm for 80mm paper
                else -> 100 // ~12.5mm for 58mm paper
            }

            Log.d(TAG, "Paper width: ${paperWidthMm}mm = ${paperWidthDots} dots")
            Log.d(TAG, "Target logo size: ${targetLogoSize}x${targetLogoSize} dots")

            // Create square bitmap from original
            // Take the smaller dimension and crop to square first
            val minDimension = min(originalBitmap.width, originalBitmap.height)
            val xOffset = (originalBitmap.width - minDimension) / 2
            val yOffset = (originalBitmap.height - minDimension) / 2

            val squareBitmap = Bitmap.createBitmap(
                originalBitmap,
                xOffset,
                yOffset,
                minDimension,
                minDimension
            )

            Log.d(TAG, "Cropped to square: ${squareBitmap.width}x${squareBitmap.height}")

            // Now scale the square bitmap to target size
            val scaledBitmap = Bitmap.createScaledBitmap(
                squareBitmap,
                targetLogoSize,
                targetLogoSize,
                true
            )

            Log.d(TAG, "Scaled bitmap size: ${scaledBitmap.width}x${scaledBitmap.height}")

            // Print the bitmap centered
            printBitmapCentered(out, scaledBitmap)

            // Cleanup
            squareBitmap.recycle()

            // Cleanup
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
     * Print bitmap image centered using ESC * command (bit image)
     * Uses 8-dot single density mode for better compatibility and smaller buffer usage
     */
    private fun printBitmapCentered(
        out: OutputStream,
        bitmap: Bitmap
    ) {
        // Set center alignment using ESC a command
        out.write(byteArrayOf(0x1B, 0x61, 0x01)) // ESC a 1 = center

        // Convert to monochrome
        val threshold = 128
        val dots = Array(bitmap.height) { y ->
            BooleanArray(bitmap.width) { x ->
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val gray = (r + g + b) / 3
                gray < threshold // dark pixels become true
            }
        }

        Log.d(TAG, "Converted to monochrome: ${dots.size} rows x ${dots[0].size} cols")

        // Print using ESC * command in 8-dot mode (smaller strips, better compatibility)
        // Process in strips of 8 dots height (1 byte per column)
        var y = 0
        while (y < dots.size) {
            val stripHeight = min(8, dots.size - y)

            // ESC * m nL nH d1...dk
            // m = 0 (8-dot single-density, 60 DPI) - better compatibility than 24-dot
            val nL = (bitmap.width and 0xFF).toByte()
            val nH = ((bitmap.width shr 8) and 0xFF).toByte()

            out.write(byteArrayOf(0x1B, 0x2A, 0, nL, nH))

            // Send bitmap data column by column
            for (x in 0 until bitmap.width) {
                // Each column is 1 byte for 8 dots
                var byte: Byte = 0
                for (k in 0 until stripHeight) {
                    val dotY = y + k
                    if (dotY < dots.size && x < dots[dotY].size && dots[dotY][x]) {
                        val bitIndex = 7 - k
                        byte = (byte.toInt() or (1 shl bitIndex)).toByte()
                    }
                }
                out.write(byte.toInt())
            }

            // Line feed - move to next strip
            out.write(byteArrayOf(0x0A))
            y += 8
        }

        // Add extra line feed after logo for spacing
        out.write(byteArrayOf(0x0A))

        // Reset to left alignment
        out.write(byteArrayOf(0x1B, 0x61, 0x00)) // ESC a 0 = left

        // Flush output to ensure all data is sent
        out.flush()

        // Small delay to let printer process the bitmap
        try {
            Thread.sleep(100) // 100ms delay
        } catch (e: InterruptedException) {
            Log.w(TAG, "Sleep interrupted", e)
        }

        Log.d(TAG, "Bitmap printed, total strips: ${(bitmap.height + 7) / 8}")
    }
}

