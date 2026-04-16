package com.minhee.pockit.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhee.pockit.domain.model.PockitEntry
import com.minhee.pockit.domain.repository.PockitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EntryDetailUiState(
    val entry: PockitEntry? = null,
    val isLoading: Boolean = true,
)

sealed interface EntryDetailEvent {
    data object DeleteSuccess : EntryDetailEvent
}

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PockitRepository,
) : ViewModel() {

    private val entryId: Long = savedStateHandle["entryId"]!!

    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<EntryDetailEvent>()
    val event = _event.asSharedFlow()

    init {
        loadEntry()
    }

    fun loadEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entry = repository.getEntryById(entryId)
            _uiState.update { it.copy(entry = entry, isLoading = false) }
        }
    }

    fun deleteEntry() {
        val entry = _uiState.value.entry ?: return
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _event.emit(EntryDetailEvent.DeleteSuccess)
        }
    }
}
