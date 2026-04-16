package com.minhee.pockit.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhee.pockit.domain.model.PockitEntry
import com.minhee.pockit.domain.model.Tag
import com.minhee.pockit.domain.repository.PockitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class EntryAddUiState(
    val date: LocalDate = LocalDate.now(),
    val cumulativePnlText: String = "",
    val previousCumulativePnl: Long? = null,
    val stockName: String = "",
    val memo: String = "",
    val selectedThemeTagIds: Set<Long> = emptySet(),
    val selectedEmotionTagIds: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
) {
    val cumulativePnl: Long?
        get() = cumulativePnlText.replace(",", "").toLongOrNull()

    val dailyPnl: Long?
        get() {
            val cumulative = cumulativePnl ?: return null
            val prev = previousCumulativePnl ?: return cumulative
            return cumulative - prev
        }
}

sealed interface EntryAddEvent {
    data object SaveSuccess : EntryAddEvent
}

@HiltViewModel
class EntryAddViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PockitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EntryAddUiState(
            date = savedStateHandle.get<String>("date")
                ?.takeIf { it.isNotBlank() }
                ?.let { LocalDate.parse(it) }
                ?: LocalDate.now(),
        ),
    )
    val uiState: StateFlow<EntryAddUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<EntryAddEvent>()
    val event = _event.asSharedFlow()

    val themeTags: StateFlow<List<Tag>> = repository.getAllThemeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val emotionTags: StateFlow<List<Tag>> = repository.getAllEmotionTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val prev = repository.getLatestCumulativePnl()
            _uiState.update { it.copy(previousCumulativePnl = prev) }
        }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onCumulativePnlChange(text: String) {
        _uiState.update { it.copy(cumulativePnlText = text) }
    }

    fun onStockNameChange(text: String) {
        _uiState.update { it.copy(stockName = text) }
    }

    fun onMemoChange(text: String) {
        if (text.length <= 200) {
            _uiState.update { it.copy(memo = text) }
        }
    }

    fun onThemeTagToggle(tagId: Long) {
        _uiState.update { state ->
            val updated = state.selectedThemeTagIds.toMutableSet()
            if (updated.contains(tagId)) updated.remove(tagId) else updated.add(tagId)
            state.copy(selectedThemeTagIds = updated)
        }
    }

    fun onEmotionTagToggle(tagId: Long) {
        _uiState.update { state ->
            val updated = state.selectedEmotionTagIds.toMutableSet()
            if (updated.contains(tagId)) updated.remove(tagId) else updated.add(tagId)
            state.copy(selectedEmotionTagIds = updated)
        }
    }

    fun save() {
        val state = _uiState.value
        val cumulative = state.cumulativePnl ?: return
        val daily = state.dailyPnl ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.saveEntry(
                entry = PockitEntry(
                    date = state.date,
                    cumulativePnl = cumulative,
                    dailyPnl = daily,
                    stockName = state.stockName.takeIf { it.isNotBlank() },
                    memo = state.memo.takeIf { it.isNotBlank() },
                ),
                themeTagIds = state.selectedThemeTagIds.toList(),
                emotionTagIds = state.selectedEmotionTagIds.toList(),
            )
            _event.emit(EntryAddEvent.SaveSuccess)
        }
    }
}
