package id.stargan.intikasir.feature.expense.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.data.local.entity.ExpenseCategory
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.expense.domain.ExpenseRepository
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DateRange(val label: String)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    data class UiState(
        val expenses: List<ExpenseEntity> = emptyList(),
        val selectedDate: Long = System.currentTimeMillis(),
        val selectedCategory: ExpenseCategory? = null,
        val dateRange: ExpenseDateRange = ExpenseDateRange.TODAY,
        val startDate: Long = getStartOfDay(System.currentTimeMillis()),
        val endDate: Long = getEndOfDay(System.currentTimeMillis()),
        val showFilter: Boolean = false,
        val dailyTotal: Double = 0.0,
        val categorySummary: Map<ExpenseCategory, Double> = emptyMap(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var currentUserId: String? = null
    private var currentUserName: String? = null

    init {
        // Get current user session
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                currentUserId = user?.id
                currentUserName = user?.name
            }
        }
        loadExpenses()
    }

    fun onEvent(event: ExpenseEvent) {
        when (event) {
            is ExpenseEvent.SelectDate -> {
                _uiState.update { it.copy(selectedDate = event.date) }
                loadExpenses()
            }
            is ExpenseEvent.SelectCategory -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                loadExpenses()
            }
            is ExpenseEvent.ClearCategoryFilter -> {
                _uiState.update { it.copy(selectedCategory = null) }
                loadExpenses()
            }
            is ExpenseEvent.ChangeDateRange -> {
                val (start, end) = calculateDateRange(event.range)
                _uiState.update {
                    it.copy(
                        dateRange = event.range,
                        startDate = start,
                        endDate = end
                    )
                }
                loadExpenses()
            }
            is ExpenseEvent.ChangeStartDate -> {
                _uiState.update { it.copy(startDate = event.date) }
            }
            is ExpenseEvent.ChangeEndDate -> {
                _uiState.update { it.copy(endDate = event.date) }
            }
            is ExpenseEvent.ToggleFilter -> {
                _uiState.update { it.copy(showFilter = !it.showFilter) }
            }
            is ExpenseEvent.ApplyFilter -> {
                _uiState.update { it.copy(showFilter = false) }
                loadExpenses()
            }
            is ExpenseEvent.CreateExpense -> createExpense(event.expense)
            is ExpenseEvent.UpdateExpense -> updateExpense(event.expense)
            is ExpenseEvent.DeleteExpense -> deleteExpense(event.expenseId)
            is ExpenseEvent.DismissToast -> _toastMessage.value = null
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val startDate = _uiState.value.startDate
                val endDate = _uiState.value.endDate
                val category = _uiState.value.selectedCategory

                // Load expenses based on filter
                val expensesFlow = if (category != null) {
                    repository.getExpensesByCategory(category)
                } else {
                    repository.getExpensesByDateRange(startDate, endDate)
                }

                expensesFlow.collect { allExpenses ->
                    // Filter by date range and category if needed
                    val filteredExpenses = allExpenses.filter { expense ->
                        expense.date >= startDate && expense.date <= endDate &&
                        (category == null || expense.category == category)
                    }

                    // Calculate total
                    val total = repository.getTotalExpenses(startDate, endDate)

                    // Calculate category summary from filtered expenses
                    val summary = filteredExpenses
                        .groupBy { it.category }
                        .mapValues { (_, list) -> list.sumOf { it.amount } }

                    _uiState.update {
                        it.copy(
                            expenses = filteredExpenses,
                            dailyTotal = total,
                            categorySummary = summary,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat data"
                    )
                }
            }
        }
    }

    private fun calculateDateRange(range: ExpenseDateRange): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endOfToday = getEndOfDay(calendar.timeInMillis)

        return when (range) {
            ExpenseDateRange.TODAY -> {
                val start = getStartOfDay(calendar.timeInMillis)
                start to endOfToday
            }
            ExpenseDateRange.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val start = getStartOfDay(calendar.timeInMillis)
                val end = getEndOfDay(calendar.timeInMillis)
                start to end
            }
            ExpenseDateRange.LAST_7_DAYS -> {
                calendar.add(Calendar.DAY_OF_MONTH, -6)
                val start = getStartOfDay(calendar.timeInMillis)
                start to endOfToday
            }
            ExpenseDateRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = getStartOfDay(calendar.timeInMillis)
                start to endOfToday
            }
            ExpenseDateRange.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = getStartOfDay(calendar.timeInMillis)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = getEndOfDay(calendar.timeInMillis)
                start to end
            }
            ExpenseDateRange.CUSTOM -> {
                _uiState.value.startDate to _uiState.value.endDate
            }
        }
    }

    private fun createExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                // Check if user is logged in
                if (currentUserId == null || currentUserName == null) {
                    _toastMessage.value = "Sesi user tidak ditemukan, silakan login ulang"
                    return@launch
                }

                val newExpense = expense.copy(
                    createdBy = currentUserId!!,
                    createdByName = currentUserName!!
                )

                repository.createExpense(newExpense)
                _toastMessage.value = "Pengeluaran berhasil ditambahkan"
            } catch (e: Exception) {
                _toastMessage.value = "Gagal menambahkan pengeluaran: ${e.message}"
            }
        }
    }

    private fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
                _toastMessage.value = "Pengeluaran berhasil diperbarui"
            } catch (e: Exception) {
                _toastMessage.value = "Gagal memperbarui pengeluaran: ${e.message}"
            }
        }
    }

    private fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expenseId)
                _toastMessage.value = "Pengeluaran berhasil dihapus"
            } catch (e: Exception) {
                _toastMessage.value = "Gagal menghapus pengeluaran: ${e.message}"
            }
        }
    }

    private fun getEndOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    companion object {
        private fun getStartOfDay(timestamp: Long): Long {
            return Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        private fun getEndOfDay(timestamp: Long): Long {
            return Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        }
    }
}

enum class ExpenseDateRange(val label: String) {
    TODAY("Hari ini"),
    YESTERDAY("Kemarin"),
    LAST_7_DAYS("7 hari terakhir"),
    THIS_MONTH("Bulan ini"),
    LAST_MONTH("Bulan lalu"),
    CUSTOM("Custom")
}

sealed class ExpenseEvent {
    data class SelectDate(val date: Long) : ExpenseEvent()
    data class SelectCategory(val category: ExpenseCategory) : ExpenseEvent()
    object ClearCategoryFilter : ExpenseEvent()
    data class ChangeDateRange(val range: ExpenseDateRange) : ExpenseEvent()
    data class ChangeStartDate(val date: Long) : ExpenseEvent()
    data class ChangeEndDate(val date: Long) : ExpenseEvent()
    object ToggleFilter : ExpenseEvent()
    object ApplyFilter : ExpenseEvent()
    data class CreateExpense(val expense: ExpenseEntity) : ExpenseEvent()
    data class UpdateExpense(val expense: ExpenseEntity) : ExpenseEvent()
    data class DeleteExpense(val expenseId: String) : ExpenseEvent()
    object DismissToast : ExpenseEvent()
}

