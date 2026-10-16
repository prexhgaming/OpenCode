package com.opencode.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectInfo(
    val id: String,
    val name: String,
    val path: String,
    val isCurrent: Boolean = false
)

@Serializable
data class Session(
    val id: String,
    val title: String,
    val projectId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)

@Serializable
data class ModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val contextWindow: Int = 128000,
    val supportsTools: Boolean = true,
    val supportsVision: Boolean = true,
    val reasoningEffort: String? = null
)

@Serializable
data class AgentInfo(
    val id: String,
    val name: String,
    val description: String,
    val defaultModelId: String,
    val isDefault: Boolean = false
)

enum class ToolStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    REQUIRES_PERMISSION
}

@Serializable
data class ToolExecution(
    val id: String,
    val toolName: String,
    val summary: String,
    val arguments: String,
    val output: String? = null,
    val status: ToolStatus = ToolStatus.COMPLETED,
    val executionTimeMs: Long = 120
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

@Serializable
data class PermissionRequest(
    val id: String,
    val toolName: String,
    val description: String,
    val commandOrPath: String,
    val isApproved: Boolean? = null
)

@Serializable
data class UserQuestion(
    val id: String,
    val question: String,
    val options: List<String> = emptyList(),
    val isMultiSelect: Boolean = false,
    val selectedOptions: List<String> = emptyList(),
    val freeTextAnswer: String? = null,
    val isAnswered: Boolean = false
)

@Serializable
data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolExecutions: List<ToolExecution> = emptyList(),
    val permissionRequest: PermissionRequest? = null,
    val userQuestion: UserQuestion? = null,
    val isStreaming: Boolean = false
)

@Serializable
data class DiffHunk(
    val header: String,
    val lines: List<DiffLine>
)

@Serializable
data class DiffLine(
    val type: DiffLineType,
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
    val content: String
)

enum class DiffLineType {
    CONTEXT,
    ADDED,
    DELETED
}

@Serializable
data class FileChange(
    val path: String,
    val oldPath: String? = null,
    val status: String = "modified",
    val additions: Int = 0,
    val deletions: Int = 0,
    val hunks: List<DiffHunk> = emptyList()
)

@Serializable
data class AppSettings(
    val themeName: String = "opencode",
    val isDark: Boolean = true,
    val defaultModelId: String = "anthropic/claude-3-7-sonnet",
    val defaultAgentId: String = "coder",
    val autoScrollChat: Boolean = true,
    val showLineNumbers: Boolean = true,
    val serverUrl: String = "http://127.0.0.1:4096",
    val isMockMode: Boolean = false
)
