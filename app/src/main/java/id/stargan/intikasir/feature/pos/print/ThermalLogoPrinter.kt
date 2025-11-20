package id.stargan.intikasir.feature.pos.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.get
import androidx.core.graphics.scale
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

            // Logo should be proportional to paper width
            // For receipt, logo should be small and neat: ~1/4 to 1/5 of paper width
            val targetLogoSize = when {
                paperWidthMm >= 80 -> 96  // ~12mm for 80mm paper (1/5 of width)
                else -> 64 // ~8mm for 58mm paper (1/5 of width)
            }

            Log.d(TAG, "Paper width: ${paperWidthMm}mm = ${paperWidthDots} dots")
            Log.d(TAG, "Target logo size: ${targetLogoSize}x${targetLogoSize} dots")

            // Create square bitmap from original
            // Find the smaller dimension and crop from center to get square
            val cropSize = min(originalBitmap.width, originalBitmap.height)
            val xOffset = (originalBitmap.width - cropSize) / 2
            val yOffset = (originalBitmap.height - cropSize) / 2

            val squareBitmap = Bitmap.createBitmap(
                originalBitmap,
                xOffset,
                yOffset,
                cropSize,
                cropSize
            )

            Log.d(TAG, "Cropped to square: ${squareBitmap.width}x${squareBitmap.height}")

            // Verify it's actually square
            if (squareBitmap.width != squareBitmap.height) {
                Log.e(TAG, "ERROR: Bitmap is not square after cropping!")
            }

            // Now scale the square bitmap to target size
            val scaledBitmap = squareBitmap.scale(
                targetLogoSize,
                targetLogoSize,
                filter = true
            )

            Log.d(TAG, "Scaled bitmap size: ${scaledBitmap.width}x${scaledBitmap.height}")

            // Verify scaled is also square
            if (scaledBitmap.width != scaledBitmap.height) {
                Log.e(TAG, "ERROR: Scaled bitmap is not square!")
            }

            // Print the bitmap centered
            printBitmapCentered(out, scaledBitmap, paperWidthDots)

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
     * Print bitmap image centered using ESC/POS commands
     * Uses 24-dot double-density mode for correct 1:1 aspect ratio
     */
    private fun printBitmapCentered(
        out: OutputStream,
        bitmap: Bitmap,
        paperWidthDots: Int
    ) {
        // Convert to monochrome
        val threshold = 128
        val dots = Array(bitmap.height) { y ->
            BooleanArray(bitmap.width) { x ->
                val pixel = bitmap[x, y]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val gray = (r + g + b) / 3
                gray < threshold // dark pixels become true
            }
        }

        Log.d(TAG, "Converted to monochrome: ${dots.size} rows x ${dots[0].size} cols")

        // Calculate left margin for centering (in dots, then convert to mm for left margin command)
        val leftMarginDots = ((paperWidthDots - bitmap.width) / 2).coerceAtLeast(0)
        val leftMarginMm = (leftMarginDots / 8).coerceAtLeast(0) // 8 dots per mm

        Log.d(TAG, "Paper width: $paperWidthDots dots")
        Log.d(TAG, "Bitmap width: ${bitmap.width} dots")
        Log.d(TAG, "Left margin: $leftMarginDots dots (~${leftMarginMm}mm)")

        // Set left margin for centering
        if (leftMarginMm > 0) {
            // GS L nL nH - Set left margin
            val nL = (leftMarginMm and 0xFF).toByte()
            val nH = ((leftMarginMm shr 8) and 0xFF).toByte()
            out.write(byteArrayOf(0x1D, 0x4C, nL, nH))
        }

        // Print using ESC * command in 24-dot double-density mode for correct aspect ratio
        // Process in strips of 24 dots height (3 bytes per column)
        var y = 0
        while (y < dots.size) {
            val stripHeight = min(24, dots.size - y)

            // ESC * m nL nH d1...dk
            // m = 33 (24-dot double-density, 200x200 DPI) - gives 1:1 aspect ratio
            val nL = (bitmap.width and 0xFF).toByte()
            val nH = ((bitmap.width shr 8) and 0xFF).toByte()

            out.write(byteArrayOf(0x1B, 0x2A, 33, nL, nH))

            // Send bitmap data column by column
            for (x in 0 until bitmap.width) {
                // Each column is 3 bytes for 24 dots
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

            // Line feed - move to next strip
            out.write(byteArrayOf(0x0A))
            y += 24
        }

        // Reset left margin to 0
        out.write(byteArrayOf(0x1D, 0x4C, 0x00, 0x00))

        // NO extra line feed - let ESCPosPrinter handle spacing

        // Flush output to ensure all data is sent
        out.flush()

        // Small delay to let printer process the bitmap
        try {
            Thread.sleep(50)
        } catch (e: InterruptedException) {
            Log.w(TAG, "Sleep interrupted", e)
        }

        Log.d(TAG, "Bitmap printed, total strips: ${(bitmap.height + 23) / 24}")
    }
}

