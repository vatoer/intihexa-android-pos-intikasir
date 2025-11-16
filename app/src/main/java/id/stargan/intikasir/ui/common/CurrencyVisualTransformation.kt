package id.stargan.intikasir.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Visual transformation untuk format Rupiah dengan thousand separator
 * Input: "15000" -> Display: "15.000"
 */
class CurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Format dengan thousand separator
        val formatted = formatWithThousandSeparator(originalText)

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = CurrencyOffsetMapping(originalText, formatted)
        )
    }

    private fun formatWithThousandSeparator(input: String): String {
        // Remove non-digit characters
        val digitsOnly = input.filter { it.isDigit() }

        if (digitsOnly.isEmpty()) return ""

        // Convert to number and format
        val number = digitsOnly.toLongOrNull() ?: return digitsOnly

        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
        }
        val formatter = DecimalFormat("#,###", symbols)

        return formatter.format(number)
    }

    /**
     * Offset mapping untuk menjaga posisi cursor tetap konsisten
     * saat text berubah karena formatting
     */
    private class CurrencyOffsetMapping(
        private val original: String,
        private val formatted: String
    ) : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            if (offset == 0) return 0
            if (offset >= original.length) return formatted.length

            // Count digits before cursor in original
            val digitsBefore = original.substring(0, offset).count { it.isDigit() }

            // Find position in formatted text that has same number of digits
            var digitsCount = 0
            for (i in formatted.indices) {
                if (formatted[i].isDigit()) {
                    digitsCount++
                    if (digitsCount == digitsBefore) {
                        return i + 1
                    }
                }
            }

            return formatted.length
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset == 0) return 0
            if (offset >= formatted.length) return original.length

            // Count digits before cursor in formatted
            val digitsBefore = formatted.substring(0, offset).count { it.isDigit() }

            // Find position in original text
            var digitsCount = 0
            for (i in original.indices) {
                if (original[i].isDigit()) {
                    digitsCount++
                    if (digitsCount == digitsBefore) {
                        return i + 1
                    }
                }
            }

            return original.length
        }
    }
}

/**
 * Helper function untuk format display rupiah (sudah dengan prefix Rp)
 */
fun formatRupiah(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("#,###", symbols)
    return "Rp ${formatter.format(amount)}"
}

/**
 * Helper function untuk format display rupiah (tanpa prefix, untuk input field)
 */
fun formatRupiahNumber(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
    }
    val formatter = DecimalFormat("#,###", symbols)
    return formatter.format(amount)
}

/**
 * Parse formatted rupiah string ke Double
 * Input: "15.000" atau "15000" -> Output: 15000.0
 */
fun parseRupiah(formattedText: String): Double {
    val digitsOnly = formattedText.filter { it.isDigit() }
    return digitsOnly.toDoubleOrNull() ?: 0.0
}

