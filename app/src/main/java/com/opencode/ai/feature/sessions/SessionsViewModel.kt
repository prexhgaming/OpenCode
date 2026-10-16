package com.opencode.ai.feature.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.ai.domain.model.ProjectInfo
import com.opencode.ai.domain.model.Session
import com.opencode.ai.domain.repository.ProjectRepository
import com.opencode.ai.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SessionsUiState(
    val sessions: List<Session> = emptyList(),
    val currentSessionId: String? = null,
    val currentProject: ProjectInfo? = null,
    val searchQuery: String = ""
)

class SessionsViewModel(
    private val sessionRepository: SessionRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionsUiState())
    val uiState: StateFlow<SessionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.sessions.collectLatest { list ->
                _uiState.value = _uiState.value.copy(sessions = list)
            }
        }
        viewModelScope.launch {
            sessionRepository.currentSessionId.collectLatest { id ->
                _uiState.value = _uiState.value.copy(currentSessionId = id)
            }
        }
        viewModelScope.launch {
            projectRepository.currentProject.collectLatest { proj ->
                _uiState.value = _uiState.value.copy(currentProject = proj)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.selectSession(sessionId)
        }
    }

    fun createNewSession() {
        val projId = _uiState.value.currentProject?.id ?: "proj-1"
        viewModelScope.launch {
            sessionRepository.createNewSession(projId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
}
