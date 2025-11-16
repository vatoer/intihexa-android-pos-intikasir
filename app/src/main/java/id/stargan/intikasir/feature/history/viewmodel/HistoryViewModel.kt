package id.stargan.intikasir.feature.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.feature.pos.domain.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import id.stargan.intikasir.feature.history.ui.DateRange

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(HistoryDetailUiState())
    val detailUiState: StateFlow<HistoryDetailUiState> = _detailUiState.asStateFlow()

    init {
        applyFilter() // default today
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.ChangeRange -> _uiState.update { it.copy(range = event.range, showFilter = true).withRangeDefaults() }
            is HistoryEvent.ChangeStartDate -> _uiState.update { it.copy(startDate = event.value) }
            is HistoryEvent.ChangeEndDate -> _uiState.update { it.copy(endDate = event.value) }
            HistoryEvent.ApplyFilter -> applyFilter()
            HistoryEvent.ToggleFilter -> _uiState.update { it.copy(showFilter = !it.showFilter) }
            is HistoryEvent.LoadDetail -> loadDetail(event.transactionId)
        }
    }

    private fun HistoryUiState.withRangeDefaults(): HistoryUiState {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis
        val start: Long = when (range) {
            DateRange.TODAY -> startOfDay(end)
            DateRange.YESTERDAY -> startOfDay(end - 24*60*60*1000).also { cal.timeInMillis = it }
            DateRange.LAST_7_DAYS -> end - 6*24*60*60*1000
            DateRange.THIS_MONTH -> startOfMonth(end)
            DateRange.LAST_MONTH -> startOfLastMonth(end)
            DateRange.CUSTOM -> startDate
        }
        val newEnd = when (range) {
            DateRange.YESTERDAY -> endOfDay(start)
            else -> end
        }
        return copy(startDate = start, endDate = newEnd)
    }

    private fun startOfDay(time: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = time; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
    private fun endOfDay(time: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = time; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
        return cal.timeInMillis
    }
    private fun startOfMonth(time: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = time; set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
    private fun startOfLastMonth(time: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = time; add(Calendar.MONTH, -1); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }

    private fun applyFilter() {
        val s = _uiState.value
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repo.getTransactionsByDateRange(s.startDate, s.endDate, onlyCompleted = true).collect { list ->
                _uiState.update { it.copy(isLoading = false, transactions = list) }
            }
        }
    }

    private fun loadDetail(transactionId: String) {
        _detailUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            combine(
                repo.getTransactionById(transactionId),
                repo.getTransactionItems(transactionId)
            ) { tx, items -> tx to items }
                .collect { (tx, items) ->
                    _detailUiState.update { it.copy(isLoading = false, transaction = tx, items = items) }
                }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch { repo.softDeleteTransaction(transactionId) }
    }
}

data class HistoryUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionEntity> = emptyList(),
    val showFilter: Boolean = false,
    val range: DateRange = DateRange.TODAY,
    val startDate: Long = Calendar.getInstance().let { cal -> cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); cal.timeInMillis },
    val endDate: Long = System.currentTimeMillis()
)

data class HistoryDetailUiState(
    val isLoading: Boolean = false,
    val transaction: TransactionEntity? = null,
    val items: List<TransactionItemEntity> = emptyList()
)

sealed class HistoryEvent {
    data class ChangeRange(val range: DateRange) : HistoryEvent()
    data class ChangeStartDate(val value: Long) : HistoryEvent()
    data class ChangeEndDate(val value: Long) : HistoryEvent()
    data object ApplyFilter : HistoryEvent()
    data object ToggleFilter : HistoryEvent()
    data class LoadDetail(val transactionId: String) : HistoryEvent()
}
