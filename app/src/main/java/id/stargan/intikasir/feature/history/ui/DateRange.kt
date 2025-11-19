package id.stargan.intikasir.feature.history.ui

/**
 * Supported date range filters for History feature.
 */
enum class DateRange(val label: String) {
    TODAY("Hari ini"),
    YESTERDAY("Kemarin"),
    LAST_7_DAYS("7 hari terakhir"),
    THIS_MONTH("Bulan ini"),
    LAST_MONTH("Bulan lalu"),
    CUSTOM("Custom")
}
