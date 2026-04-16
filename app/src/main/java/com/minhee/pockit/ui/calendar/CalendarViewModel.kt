package com.minhee.pockit.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhee.pockit.domain.model.PockitEntry
import com.minhee.pockit.domain.repository.PockitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val entries: Map<LocalDate, List<PockitEntry>> = emptyMap(),
    val selectedDateEntries: List<PockitEntry> = emptyList(),
    val monthlyProfit: Long = 0L,
    val monthlyLoss: Long = 0L,
)

val CalendarUiState.monthlyNet: Long get() = monthlyProfit + monthlyLoss

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: PockitRepository,
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())

    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    val uiState: StateFlow<CalendarUiState> = _currentMonth
        .flatMapLatest { month ->
            val entriesFlow = repository.getEntriesByMonth(month)
            val profitFlow = repository.getMonthlyProfit(month)
            val lossFlow = repository.getMonthlyLoss(month)

            combine(
                entriesFlow,
                profitFlow,
                lossFlow,
                _selectedDate,
            ) { entries, profit, loss, selectedDate ->
                val grouped = entries.groupBy { it.date }
                CalendarUiState(
                    currentMonth = month,
                    selectedDate = selectedDate,
                    entries = grouped,
                    selectedDateEntries = selectedDate?.let { grouped[it] } ?: emptyList(),
                    monthlyProfit = profit,
                    monthlyLoss = loss,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState(),
        )

    fun onMonthChange(month: YearMonth) {
        _currentMonth.value = month
        _selectedDate.value = null
    }

    fun onPreviousMonth() {
        val prev = _currentMonth.value.minusMonths(1)
        onMonthChange(prev)
    }

    fun onNextMonth() {
        val next = _currentMonth.value.plusMonths(1)
        onMonthChange(next)
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }
}
