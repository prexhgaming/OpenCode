package com.opencode.ai.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.ai.domain.model.AgentInfo
import com.opencode.ai.domain.model.ChatMessage
import com.opencode.ai.domain.model.ModelInfo
import com.opencode.ai.domain.model.ProjectInfo
import com.opencode.ai.domain.model.Session
import com.opencode.ai.domain.repository.AgentRepository
import com.opencode.ai.domain.repository.DiffRepository
import com.opencode.ai.domain.repository.MessageRepository
import com.opencode.ai.domain.repository.ModelRepository
import com.opencode.ai.domain.repository.ProjectRepository
import com.opencode.ai.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatUiState(
    val currentSession: Session? = null,
    val currentProject: ProjectInfo? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val selectedModel: ModelInfo? = null,
    val selectedAgent: AgentInfo? = null,
    val availableModels: List<ModelInfo> = emptyList(),
    val availableAgents: List<AgentInfo> = emptyList(),
    val diffCount: Int = 0,
    val showModelPicker: Boolean = false,
    val showAgentPicker: Boolean = false,
    val showCommandPalette: Boolean = false,
    val inputText: String = "",
    val error: String? = null
)

class ChatViewModel(
    private val sessionRepository: SessionRepository,
    private val messageRepository: MessageRepository,
    private val modelRepository: ModelRepository,
    private val agentRepository: AgentRepository,
    private val projectRepository: ProjectRepository,
    private val diffRepository: DiffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeRepositories()
    }

    private fun observeRepositories() {
        viewModelScope.launch {
            modelRepository.selectedModel.collectLatest { model ->
                _uiState.value = _uiState.value.copy(selectedModel = model)
            }
        }

        viewModelScope.launch {
            modelRepository.models.collectLatest { models ->
                _uiState.value = _uiState.value.copy(availableModels = models)
            }
        }

        viewModelScope.launch {
            agentRepository.selectedAgent.collectLatest { agent ->
                _uiState.value = _uiState.value.copy(selectedAgent = agent)
            }
        }

        viewModelScope.launch {
            agentRepository.agents.collectLatest { agents ->
                _uiState.value = _uiState.value.copy(availableAgents = agents)
            }
        }

        viewModelScope.launch {
            projectRepository.currentProject.collectLatest { proj ->
                _uiState.value = _uiState.value.copy(currentProject = proj)
            }
        }

        viewModelScope.launch {
            diffRepository.fileChanges.collectLatest { diffs ->
                _uiState.value = _uiState.value.copy(diffCount = diffs.size)
            }
        }

        viewModelScope.launch {
            sessionRepository.currentSessionId.collectLatest { sessId ->
                val allSessions = sessionRepository.sessions.value
                val session = allSessions.firstOrNull { it.id == sessId }
                _uiState.value = _uiState.value.copy(currentSession = session)
                if (sessId != null) {
                    loadMessages(sessId)
                }
            }
        }
    }

    private fun loadMessages(sessionId: String) {
        viewModelScope.launch {
            messageRepository.getMessagesForSession(sessionId).collectLatest { msgs ->
                _uiState.value = _uiState.value.copy(
                    messages = msgs,
                    isStreaming = msgs.any { it.isStreaming }
                )
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        val sessionId = _uiState.value.currentSession?.id ?: return
        val modelId = _uiState.value.selectedModel?.id ?: "anthropic/claude-3-7-sonnet"
        val agentId = _uiState.value.selectedAgent?.id ?: "coder"

        _uiState.value = _uiState.value.copy(inputText = "", isStreaming = true)

        viewModelScope.launch {
            messageRepository.sendMessage(sessionId, text, modelId, agentId).collectLatest { msg ->
                _uiState.value = _uiState.value.copy(isStreaming = msg.isStreaming)
            }
        }
    }

    fun cancelGeneration() {
        val sessionId = _uiState.value.currentSession?.id ?: return
        viewModelScope.launch {
            messageRepository.cancelGeneration(sessionId)
            _uiState.value = _uiState.value.copy(isStreaming = false)
        }
    }

    fun respondToPermission(messageId: String, requestId: String, approved: Boolean) {
        viewModelScope.launch {
            messageRepository.respondToPermission(messageId, requestId, approved)
        }
    }

    fun answerQuestion(messageId: String, questionId: String, answers: List<String>, freeText: String?) {
        viewModelScope.launch {
            messageRepository.answerQuestion(messageId, questionId, answers, freeText)
        }
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            modelRepository.selectModel(modelId)
        }
    }

    fun selectAgent(agentId: String) {
        viewModelScope.launch {
            agentRepository.selectAgent(agentId)
        }
    }

    fun setShowModelPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showModelPicker = show)
    }

    fun setShowAgentPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAgentPicker = show)
    }

    fun setShowCommandPalette(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCommandPalette = show)
    }
}
