package id.stargan.intikasir.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Small helper wrapper around java.time for formatting epoch millis into strings.
 * Min SDK >= 26 required; project minSdk is 29 so this is safe.
 */
object DateFormatUtils {
    fun formatEpochMillis(
        epochMillis: Long,
        pattern: String,
        locale: Locale = Locale.forLanguageTag("id-ID"),
        zone: ZoneId = ZoneId.systemDefault()
    ): String {
        val zdt = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        return zdt.format(formatter)
    }

    fun fileTimestamp(epochMillis: Long = System.currentTimeMillis()): String {
        // Use numeric-only pattern safe across locales
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.getDefault())
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(formatter)
    }
}

