package id.stargan.intikasir.feature.pos.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import id.stargan.intikasir.domain.model.StoreSettings
import java.io.File
import java.io.OutputStream
import kotlin.math.ceil

/**
 * Helper untuk mencetak logo pada thermal printer menggunakan perintah ESC/POS.
 * Versi ini paling tangguh, menggabungkan chunking, delay, dan penanganan I/O yang aman
 * untuk menangani printer dengan buffer yang sensitif.
 */
object ThermalLogoPrinter {
    private const val TAG = "ThermalLogoPrinter"

    // ESC/POS Commands
    private val ESC_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0)
    private val CMD_GS_v_0 = byteArrayOf(0x1D, 0x76, 0x30, 0x00)

    // --- Konfigurasi Optimal untuk Stabilitas ---
    // Ukuran chunk yang kecil untuk printer sensitif.
    private const val RASTER_LINE_CHUNK_SIZE = 32
    // Jeda antar chunk untuk memberi "napas" pada printer.
    private const val CHUNK_SLEEP_DELAY_MS = 30L
    // Threshold luminansi yang lebih ketat, hanya piksel yang lebih gelap yang akan dicetak.
    private const val LUMINANCE_THRESHOLD = 100

    /**
     * Mencetak logo ke printer thermal.
     * PENTING: Pastikan `OutputStream` yang diteruskan ke sini sudah dibungkus
     * dengan `java.io.BufferedOutputStream` untuk performa dan stabilitas I/O terbaik.
     */
    fun printLogo(
        context: Context,
        out: OutputStream,
        settings: StoreSettings
    ): Boolean {
        if (!settings.printLogo || settings.storeLogo.isNullOrBlank()) {
            return false // Kondisi normal, tidak perlu log.
        }

        try {
            val originalBitmap = BitmapFactory.decodeFile(settings.storeLogo) ?: run {
                Log.w(TAG, "Gagal decode file logo: ${settings.storeLogo}")
                return false
            }

            val maxPrinterWidth = if (settings.paperWidthMm >= 80) 576 else 384

            // 1. Kecilkan logo ke ukuran yang diinginkan
            val logoTargetWidth = (maxPrinterWidth * 0.45).toInt()
            val aspectRatio = originalBitmap.height.toFloat() / originalBitmap.width.toFloat()
            val logoTargetHeight = (logoTargetWidth * aspectRatio).toInt()
            val scaledLogoBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                logoTargetWidth,
                logoTargetHeight,
                true
            )

            // 2. Buat bitmap final dengan padding untuk mengatur posisi
            val finalBitmap = createBitmapWithPadding(
                logo = scaledLogoBitmap,
                totalWidth = maxPrinterWidth,
                horizontalBias = 0.6f // 0.5f=tengah, >0.5f=ke kanan
            )

            Log.d(
                TAG,
                "Mencetak Logo: Ukuran Asli=${originalBitmap.width}x${originalBitmap.height}, " +
                        "Ukuran Logo=${scaledLogoBitmap.width}x${scaledLogoBitmap.height}, " +
                        "Ukuran Final=${finalBitmap.width}x${finalBitmap.height}"
            )

            // 3. Cetak bitmap yang sudah diproses
            printBitmapRaster(out, finalBitmap)

            // Cleanup memori
            originalBitmap.recycle()
            scaledLogoBitmap.recycle()
            finalBitmap.recycle()

            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error kritis saat persiapan mencetak logo", e)
            return false
        }
    }

    /**
     * Membuat bitmap baru dengan lebar total yang ditentukan, dan menempatkan logo
     * di dalamnya sesuai dengan bias horizontal.
     */
    private fun createBitmapWithPadding(
        logo: Bitmap,
        totalWidth: Int,
        horizontalBias: Float
    ): Bitmap {
        val resultBitmap = Bitmap.createBitmap(totalWidth, logo.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.WHITE) // Latar belakang putih

        val remainingSpace = totalWidth - logo.width
        val leftPadding = (remainingSpace * horizontalBias).toInt().coerceAtLeast(0)

        canvas.drawBitmap(logo, leftPadding.toFloat(), 0f, null)

        return resultBitmap
    }

    /**
     * Mengirim data bitmap ke printer menggunakan metode chunking yang stabil.
     */
    private fun printBitmapRaster(out: OutputStream, bitmap: Bitmap) {
        // Set rata kiri, karena posisi sudah diatur di dalam bitmap itu sendiri
        out.write(ESC_ALIGN_LEFT)

        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = ceil(width.toDouble() / 8).toInt()

        var currentY = 0
        while (currentY < height) {
            val chunkHeight = (height - currentY).coerceAtMost(RASTER_LINE_CHUNK_SIZE)
            val data = ByteArray(widthBytes * chunkHeight)
            var k = 0

            // Proses konversi piksel ke byte
            for (y in currentY until currentY + chunkHeight) {
                for (xByte in 0 until widthBytes) {
                    var byteValue = 0
                    for (bit in 0 until 8) {
                        val currentX = xByte * 8 + bit
                        if (currentX < width) {
                            val pixel = bitmap.getPixel(currentX, y)
                            val luminance = (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel))
                            if (Color.alpha(pixel) > 50 && luminance < LUMINANCE_THRESHOLD) {
                                byteValue = byteValue or (1 shl (7 - bit))
                            }
                        }
                    }
                    data[k++] = byteValue.toByte()
                }
            }

            // Kirim data chunk dengan penanganan error
            try {
                // Tulis header dan data dalam satu blok ke buffer
                out.write(CMD_GS_v_0)
                out.write(widthBytes % 256)
                out.write(widthBytes / 256)
                out.write(chunkHeight % 256)
                out.write(chunkHeight / 256)
                out.write(data)

                // Paksa buffer untuk mengirim data ke socket
                out.flush()

                // Beri jeda agar printer punya waktu untuk memproses
                Thread.sleep(CHUNK_SLEEP_DELAY_MS)

            } catch (e: Exception) {
                Log.e(TAG, "Gagal menulis chunk data gambar di baris: $currentY", e)
                // Hentikan proses jika terjadi error penulisan untuk mencegah data korup
                return
            }

            currentY += chunkHeight
        }

        // Beri satu baris spasi setelah gambar untuk memisahkan dengan teks berikutnya
        out.write(byteArrayOf(0x0A))
        out.flush()
    }
}
