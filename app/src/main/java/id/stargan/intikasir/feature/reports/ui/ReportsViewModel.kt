package id.stargan.intikasir.feature.reports.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.reports.domain.model.*
import id.stargan.intikasir.feature.reports.domain.usecase.GetDashboardDataUseCase
import id.stargan.intikasir.feature.reports.domain.usecase.GetProfitLossReportUseCase
import id.stargan.intikasir.feature.reports.domain.usecase.ExportReportUseCase
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
    private val exportReportUseCase: ExportReportUseCase
) : ViewModel() {

    data class UiState(
        val selectedPeriod: PeriodType = PeriodType.THIS_MONTH,
        val startDate: Long = getStartOfMonth(),
        val endDate: Long = getEndOfMonth(),

        val dashboard: ReportDashboard? = null,
        val profitLossReport: ProfitLossReport? = null,

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
                    _uiState.value.endDate
                )

                _uiState.update {
                    it.copy(
                        dashboard = dashboard,
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
    object ShowPeriodPicker : ReportsEvent()
    object HidePeriodPicker : ReportsEvent()
    object ShowExportDialog : ReportsEvent()
    object HideExportDialog : ReportsEvent()
    object Refresh : ReportsEvent()
    object DismissError : ReportsEvent()
    object DismissSuccess : ReportsEvent()
}

