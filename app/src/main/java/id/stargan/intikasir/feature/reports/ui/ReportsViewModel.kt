package id.stargan.intikasir.feature.reports.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.reports.domain.model.PeriodType
import id.stargan.intikasir.feature.reports.domain.model.ReportDashboard
import id.stargan.intikasir.feature.reports.domain.model.ProfitLossReport
import id.stargan.intikasir.feature.reports.domain.model.WorstProductsReport
import id.stargan.intikasir.feature.reports.domain.usecase.ExportReportUseCase
import id.stargan.intikasir.feature.reports.domain.usecase.GetDashboardDataUseCase
import id.stargan.intikasir.feature.reports.domain.usecase.GetProfitLossReportUseCase
import id.stargan.intikasir.feature.reports.domain.usecase.GetWorstSellingProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getProfitLossReportUseCase: GetProfitLossReportUseCase,
    private val exportReportUseCase: ExportReportUseCase,
    private val getWorstSellingProductsUseCase: GetWorstSellingProductsUseCase
) : ViewModel() {

    data class UiState(
        val selectedPeriod: PeriodType = PeriodType.THIS_MONTH,
        val startDate: Long = getStartOfMonth(),
        val endDate: Long = getEndOfMonth(),

        val dashboard: ReportDashboard? = null,
        val profitLossReport: ProfitLossReport? = null,
        val worstProductsReport: WorstProductsReport? = null,
        val dashboardRevenueChange: Double? = null,
        val cashierIdFilter: String? = null,

        val selectedTab: ReportTab = ReportTab.DASHBOARD,

        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,

        val showPeriodPicker: Boolean = false,
        val showExportDialog: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: ReportsEvent) {
        when (event) {
            is ReportsEvent.SetCashierFilter -> {
                _uiState.update { it.copy(cashierIdFilter = event.cashierId) }
                loadData()
            }
            is ReportsEvent.SelectPeriod -> selectPeriod(event.period)
            is ReportsEvent.SelectCustomPeriod -> selectCustomPeriod(event.startDate, event.endDate)
            is ReportsEvent.SelectTab -> selectTab(event.tab)
            is ReportsEvent.ShowPeriodPicker -> _uiState.update { it.copy(showPeriodPicker = true) }
            is ReportsEvent.HidePeriodPicker -> _uiState.update { it.copy(showPeriodPicker = false) }
            is ReportsEvent.ShowExportDialog -> _uiState.update { it.copy(showExportDialog = true) }
            is ReportsEvent.HideExportDialog -> _uiState.update { it.copy(showExportDialog = false) }
            is ReportsEvent.Refresh -> loadData()
            is ReportsEvent.DismissError -> _uiState.update { it.copy(error = null) }
            is ReportsEvent.DismissSuccess -> _uiState.update { it.copy(successMessage = null) }
            is ReportsEvent.LoadWorstProducts -> loadWorstProducts(event.lowThreshold)
        }
    }

    private fun selectPeriod(period: PeriodType) {
        val (start, end) = getPeriodRange(period)
        _uiState.update {
            it.copy(
                selectedPeriod = period,
                startDate = start,
                endDate = end,
                showPeriodPicker = false
            )
        }
        loadData()
    }

    private fun selectCustomPeriod(startDate: Long, endDate: Long) {
        _uiState.update {
            it.copy(
                selectedPeriod = PeriodType.CUSTOM,
                startDate = startDate,
                endDate = endDate,
                showPeriodPicker = false
            )
        }
        loadData()
    }

    private fun selectTab(tab: ReportTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == ReportTab.PROFIT_LOSS && _uiState.value.profitLossReport == null) {
            loadProfitLoss()
        }
    }

    private fun loadData() {
        when (_uiState.value.selectedTab) {
            ReportTab.DASHBOARD -> loadDashboard()
            ReportTab.PROFIT_LOSS -> loadProfitLoss()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val dashboard = getDashboardDataUseCase(
                    _uiState.value.startDate,
                    _uiState.value.endDate,
                    _uiState.value.cashierIdFilter
                )

                // Compute previous period range by using the same length and shifting before startDate
                val (prevStart, prevEnd) = computePreviousRange(_uiState.value.startDate, _uiState.value.endDate)
                var prevDashboard: ReportDashboard? = null
                try {
                    prevDashboard = getDashboardDataUseCase(prevStart, prevEnd, _uiState.value.cashierIdFilter)
                } catch (_: Exception) {
                    // If previous period fetch fails, leave prevDashboard as null and continue
                }

                val revenueChange = if (prevDashboard != null) {
                    dashboard.totalRevenue - prevDashboard.totalRevenue
                } else {
                    null
                }

                _uiState.update {
                    it.copy(
                        dashboard = dashboard,
                        dashboardRevenueChange = revenueChange,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat dashboard: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadProfitLoss() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val report = getProfitLossReportUseCase(
                    _uiState.value.startDate,
                    _uiState.value.endDate
                )

                _uiState.update {
                    it.copy(
                        profitLossReport = report,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat laporan laba rugi: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadWorstProducts(lowThreshold: Int = 5) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val report = getWorstSellingProductsUseCase(
                    _uiState.value.startDate,
                    _uiState.value.endDate,
                    _uiState.value.cashierIdFilter,
                    lowThreshold
                )
                _uiState.update { it.copy(worstProductsReport = report, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Gagal memuat worst products: ${e.message}") }
            }
        }
    }

    private fun computePreviousRange(startDate: Long, endDate: Long): Pair<Long, Long> {
        val length = endDate - startDate + 1
        val prevEnd = startDate - 1
        val prevStart = prevEnd - (length - 1)
        return Pair(prevStart, prevEnd)
    }

    // Export helpers (suspend) - UI can call these from a coroutine and then share file
    suspend fun exportDashboardSummary(context: Context): java.io.File {
        val dashboard = _uiState.value.dashboard ?: getDashboardDataUseCase(
            _uiState.value.startDate,
            _uiState.value.endDate,
            _uiState.value.cashierIdFilter
        )
        return exportReportUseCase.exportDashboardSummary(context, dashboard)
    }

    suspend fun exportWorstProducts(context: Context): java.io.File {
        val worstReport = _uiState.value.worstProductsReport ?: getWorstSellingProductsUseCase(
            _uiState.value.startDate,
            _uiState.value.endDate,
            _uiState.value.cashierIdFilter,
            0
        )
        return exportReportUseCase.exportWorstProducts(context, worstReport, _uiState.value.startDate, _uiState.value.endDate)
    }

    suspend fun exportDashboardSummaryXlsx(context: Context): java.io.File {
        val dashboard = _uiState.value.dashboard ?: getDashboardDataUseCase(
            _uiState.value.startDate,
            _uiState.value.endDate,
            _uiState.value.cashierIdFilter
        )
        return exportReportUseCase.exportDashboardSummaryXlsx(context, dashboard)
    }

    suspend fun exportWorstProductsXlsx(context: Context): java.io.File {
        val worstReport = _uiState.value.worstProductsReport ?: getWorstSellingProductsUseCase(
            _uiState.value.startDate,
            _uiState.value.endDate,
            _uiState.value.cashierIdFilter,
            0
        )
        return exportReportUseCase.exportWorstProductsXlsx(context, worstReport, _uiState.value.startDate, _uiState.value.endDate)
    }

    companion object {
        private fun getStartOfMonth(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        private fun getEndOfMonth(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.timeInMillis
        }

        private fun getPeriodRange(period: PeriodType): Pair<Long, Long> {
            val calendar = Calendar.getInstance()

            return when (period) {
                PeriodType.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis

                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val end = calendar.timeInMillis

                    Pair(start, end)
                }
                PeriodType.YESTERDAY -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis

                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val end = calendar.timeInMillis

                    Pair(start, end)
                }
                PeriodType.THIS_WEEK -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis

                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    calendar.add(Calendar.MILLISECOND, -1)
                    val end = calendar.timeInMillis

                    Pair(start, end)
                }
                PeriodType.LAST_WEEK -> {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis

                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    calendar.add(Calendar.MILLISECOND, -1)
                    val end = calendar.timeInMillis

                    Pair(start, end)
                }
                PeriodType.THIS_MONTH -> {
                    Pair(getStartOfMonth(), getEndOfMonth())
                }
                PeriodType.LAST_MONTH -> {
                    calendar.add(Calendar.MONTH, -1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis

                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val end = calendar.timeInMillis

                    Pair(start, end)
                }
                PeriodType.THIS_YEAR -> {
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis

                    calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                    calendar.set(Calendar.DAY_OF_MONTH, 31)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val end = calendar.timeInMillis

                    Pair(start, end)
                }
                PeriodType.CUSTOM -> Pair(getStartOfMonth(), getEndOfMonth()) // Default
            }
        }
    }
}

enum class ReportTab {
    DASHBOARD,
    PROFIT_LOSS
}

sealed class ReportsEvent {
    data class SelectPeriod(val period: PeriodType) : ReportsEvent()
    data class SelectCustomPeriod(val startDate: Long, val endDate: Long) : ReportsEvent()
    data class SelectTab(val tab: ReportTab) : ReportsEvent()
    data class LoadWorstProducts(val lowThreshold: Int = 5) : ReportsEvent()
    object ShowPeriodPicker : ReportsEvent()
    object HidePeriodPicker : ReportsEvent()
    object ShowExportDialog : ReportsEvent()
    object HideExportDialog : ReportsEvent()
    object Refresh : ReportsEvent()
    object DismissError : ReportsEvent()
    object DismissSuccess : ReportsEvent()
    data class SetCashierFilter(val cashierId: String?) : ReportsEvent()
}
