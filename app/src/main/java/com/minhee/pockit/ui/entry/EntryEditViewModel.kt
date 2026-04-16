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
import java.time.LocalDateTime
import javax.inject.Inject

data class EntryEditUiState(
    val entryId: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val cumulativePnlText: String = "",
    val stockName: String = "",
    val memo: String = "",
    val selectedThemeTagIds: Set<Long> = emptySet(),
    val selectedEmotionTagIds: Set<Long> = emptySet(),
    val originalCumulativePnl: Long = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) {
    val cumulativePnl: Long?
        get() = cumulativePnlText.replace(",", "").toLongOrNull()
}

sealed interface EntryEditEvent {
    data object SaveSuccess : EntryEditEvent
}

@HiltViewModel
class EntryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PockitRepository,
) : ViewModel() {

    private val entryId: Long = savedStateHandle["entryId"]!!

    private val _uiState = MutableStateFlow(EntryEditUiState())
    val uiState: StateFlow<EntryEditUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<EntryEditEvent>()
    val event = _event.asSharedFlow()

    val themeTags: StateFlow<List<Tag>> = repository.getAllThemeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val emotionTags: StateFlow<List<Tag>> = repository.getAllEmotionTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val entry = repository.getEntryById(entryId) ?: return@launch
            _uiState.update {
                it.copy(
                    entryId = entry.id,
                    date = entry.date,
                    cumulativePnlText = entry.cumulativePnl.toString(),
                    stockName = entry.stockName ?: "",
                    memo = entry.memo ?: "",
                    selectedThemeTagIds = entry.themeTags.map { t -> t.id }.toSet(),
                    selectedEmotionTagIds = entry.emotionTags.map { t -> t.id }.toSet(),
                    originalCumulativePnl = entry.cumulativePnl,
                    createdAt = entry.createdAt,
                    isLoading = false,
                )
            }
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
        // dailyPnl은 원본 대비 변경분을 반영하지 않고, 누적값 차이로 재계산
        // 이전 기록의 누적값을 기준으로 다시 계산
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // 이전 누적손익 가져오기 (현재 엔트리 제외)
            val prevCumulative = repository.getLatestCumulativePnl()
            val daily = if (prevCumulative != null && prevCumulative != state.originalCumulativePnl) {
                cumulative - prevCumulative
            } else {
                // 이 엔트리가 가장 최근이거나 유일한 기록인 경우
                cumulative
            }

            repository.updateEntry(
                entry = PockitEntry(
                    id = state.entryId,
                    date = state.date,
                    cumulativePnl = cumulative,
                    dailyPnl = daily,
                    stockName = state.stockName.takeIf { it.isNotBlank() },
                    memo = state.memo.takeIf { it.isNotBlank() },
                    createdAt = state.createdAt,
                ),
                themeTagIds = state.selectedThemeTagIds.toList(),
                emotionTagIds = state.selectedEmotionTagIds.toList(),
            )
            _event.emit(EntryEditEvent.SaveSuccess)
        }
    }
}
