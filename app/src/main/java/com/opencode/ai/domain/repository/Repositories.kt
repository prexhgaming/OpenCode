package com.opencode.ai.domain.repository

import com.opencode.ai.domain.model.AgentInfo
import com.opencode.ai.domain.model.AppSettings
import com.opencode.ai.domain.model.ChatMessage
import com.opencode.ai.domain.model.FileChange
import com.opencode.ai.domain.model.ModelInfo
import com.opencode.ai.domain.model.ProjectInfo
import com.opencode.ai.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val sessions: StateFlow<List<Session>>
    val currentSessionId: StateFlow<String?>
    suspend fun selectSession(sessionId: String)
    suspend fun createNewSession(projectId: String, title: String? = null): Session
    suspend fun renameSession(sessionId: String, newTitle: String)
    suspend fun deleteSession(sessionId: String)
}

interface MessageRepository {
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(sessionId: String, text: String, modelId: String, agentId: String): Flow<ChatMessage>
    suspend fun cancelGeneration(sessionId: String)
    suspend fun respondToPermission(messageId: String, requestId: String, approved: Boolean)
    suspend fun answerQuestion(messageId: String, questionId: String, answers: List<String>, freeText: String?)
}

interface ModelRepository {
    val models: StateFlow<List<ModelInfo>>
    val selectedModel: StateFlow<ModelInfo>
    suspend fun selectModel(modelId: String)
}

interface AgentRepository {
    val agents: StateFlow<List<AgentInfo>>
    val selectedAgent: StateFlow<AgentInfo>
    suspend fun selectAgent(agentId: String)
}

interface ProjectRepository {
    val projects: StateFlow<List<ProjectInfo>>
    val currentProject: StateFlow<ProjectInfo?>
    suspend fun selectProject(projectId: String)
}

interface DiffRepository {
    val fileChanges: StateFlow<List<FileChange>>
    suspend fun refreshDiffs()
}

interface SettingsRepository {
    val settings: StateFlow<AppSettings>
    suspend fun updateTheme(themeName: String, isDark: Boolean)
    suspend fun updateServerUrl(url: String)
    suspend fun toggleMockMode(enabled: Boolean)
}
