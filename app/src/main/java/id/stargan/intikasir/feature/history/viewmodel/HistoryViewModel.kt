package id.stargan.intikasir.feature.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.TransactionStatus
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

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        applyFilter() // default today
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.ChangeRange -> _uiState.update { it.copy(range = event.range, showFilter = true).withRangeDefaults() }
            is HistoryEvent.ChangeStartDate -> _uiState.update { it.copy(startDate = event.value) }
            is HistoryEvent.ChangeEndDate -> _uiState.update { it.copy(endDate = event.value) }
            is HistoryEvent.ChangeStatus -> _uiState.update { it.copy(selectedStatus = event.status) }
            HistoryEvent.ApplyFilter -> applyFilter()
            HistoryEvent.ToggleFilter -> _uiState.update { it.copy(showFilter = !it.showFilter) }
            is HistoryEvent.LoadDetail -> loadDetail(event.transactionId)
            is HistoryEvent.ShowDeleteConfirmation -> _uiState.update { it.copy(showDeleteDialog = true, transactionToDelete = event.transactionId) }
            HistoryEvent.DismissDeleteConfirmation -> _uiState.update { it.copy(showDeleteDialog = false, transactionToDelete = null) }
            HistoryEvent.ConfirmDelete -> confirmDelete()
            HistoryEvent.DismissToast -> _toastMessage.value = null
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
            repo.getTransactionsByDateRange(s.startDate, s.endDate, onlyCompleted = false).collect { list ->
                val filtered = if (s.selectedStatus != null) {
                    list.filter { it.status == s.selectedStatus }
                } else {
                    list
                }
                _uiState.update { it.copy(isLoading = false, transactions = filtered) }
            }
        }
    }

    private fun confirmDelete() {
        val txId = _uiState.value.transactionToDelete ?: return
        viewModelScope.launch {
            repo.softDeleteTransaction(txId)
            _uiState.update { it.copy(showDeleteDialog = false, transactionToDelete = null) }
            _toastMessage.value = "Transaksi berhasil dihapus"
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

    suspend fun loadAllTransactionItems(transactionIds: List<String>): Map<String, List<TransactionItemEntity>> {
        val itemsMap = mutableMapOf<String, List<TransactionItemEntity>>()
        transactionIds.forEach { txId ->
            val items = repo.getTransactionItems(txId).first()
            itemsMap[txId] = items
        }
        return itemsMap
    }
}

data class HistoryUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionEntity> = emptyList(),
    val showFilter: Boolean = false,
    val range: DateRange = DateRange.TODAY,
    val startDate: Long = Calendar.getInstance().let { cal -> cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); cal.timeInMillis },
    val endDate: Long = System.currentTimeMillis(),
    val selectedStatus: TransactionStatus? = null,
    val showDeleteDialog: Boolean = false,
    val transactionToDelete: String? = null
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
    data class ChangeStatus(val status: TransactionStatus?) : HistoryEvent()
    data object ApplyFilter : HistoryEvent()
    data object ToggleFilter : HistoryEvent()
    data class LoadDetail(val transactionId: String) : HistoryEvent()
    data class ShowDeleteConfirmation(val transactionId: String) : HistoryEvent()
    data object DismissDeleteConfirmation : HistoryEvent()
    data object ConfirmDelete : HistoryEvent()
    data object DismissToast : HistoryEvent()
}
