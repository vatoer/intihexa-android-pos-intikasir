package id.stargan.intikasir.feature.reports.ui.utils

import id.stargan.intikasir.feature.reports.domain.model.PeriodType

fun getPeriodLabel(period: PeriodType): String {
    return when (period) {
        PeriodType.TODAY -> "Hari Ini"
        PeriodType.YESTERDAY -> "Kemarin"
        PeriodType.THIS_WEEK -> "Minggu Ini"
        PeriodType.LAST_WEEK -> "Minggu Lalu"
        PeriodType.THIS_MONTH -> "Bulan Ini"
        PeriodType.LAST_MONTH -> "Bulan Lalu"
        PeriodType.THIS_YEAR -> "Tahun Ini"
        PeriodType.CUSTOM -> "Custom"
    }
}

