package com.opencode.ai.feature.diff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.ai.domain.model.FileChange
import com.opencode.ai.domain.repository.DiffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DiffUiState(
    val fileChanges: List<FileChange> = emptyList(),
    val totalAdditions: Int = 0,
    val totalDeletions: Int = 0
)

class DiffViewModel(
    private val diffRepository: DiffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiffUiState())
    val uiState: StateFlow<DiffUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            diffRepository.fileChanges.collectLatest { list ->
                val add = list.sumOf { it.additions }
                val del = list.sumOf { it.deletions }
                _uiState.value = DiffUiState(
                    fileChanges = list,
                    totalAdditions = add,
                    totalDeletions = del
                )
            }
        }
    }

    fun refreshDiffs() {
        viewModelScope.launch {
            diffRepository.refreshDiffs()
        }
    }
}
