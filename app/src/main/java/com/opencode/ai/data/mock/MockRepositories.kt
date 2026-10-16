package com.opencode.ai.data.mock

import com.opencode.ai.domain.model.AgentInfo
import com.opencode.ai.domain.model.AppSettings
import com.opencode.ai.domain.model.ChatMessage
import com.opencode.ai.domain.model.DiffHunk
import com.opencode.ai.domain.model.DiffLine
import com.opencode.ai.domain.model.DiffLineType
import com.opencode.ai.domain.model.FileChange
import com.opencode.ai.domain.model.MessageRole
import com.opencode.ai.domain.model.ModelInfo
import com.opencode.ai.domain.model.PermissionRequest
import com.opencode.ai.domain.model.ProjectInfo
import com.opencode.ai.domain.model.Session
import com.opencode.ai.domain.model.ToolExecution
import com.opencode.ai.domain.model.ToolStatus
import com.opencode.ai.domain.model.UserQuestion
import com.opencode.ai.domain.repository.AgentRepository
import com.opencode.ai.domain.repository.DiffRepository
import com.opencode.ai.domain.repository.MessageRepository
import com.opencode.ai.domain.repository.ModelRepository
import com.opencode.ai.domain.repository.ProjectRepository
import com.opencode.ai.domain.repository.SessionRepository
import com.opencode.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID

object MockData {
    val Projects = listOf(
        ProjectInfo(
            id = "proj-1",
            name = "opencode-ai/opencode",
            path = "~/workspace/opencode",
            isCurrent = true
        ),
        ProjectInfo(
            id = "proj-2",
            name = "mobile-apps/android-client",
            path = "~/workspace/android-client",
            isCurrent = false
        ),
        ProjectInfo(
            id = "proj-3",
            name = "backend-infra/cloud-router",
            path = "~/workspace/cloud-router",
            isCurrent = false
        )
    )

    val Models = listOf(
        ModelInfo(
            id = "anthropic/claude-3-7-sonnet",
            name = "Claude 3.7 Sonnet",
            provider = "Anthropic",
            contextWindow = 200000,
            supportsTools = true,
            supportsVision = true,
            reasoningEffort = "extended"
        ),
        ModelInfo(
            id = "anthropic/claude-3-5-sonnet",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic",
            contextWindow = 200000,
            supportsTools = true,
            supportsVision = true
        ),
        ModelInfo(
            id = "openai/gpt-4o",
            name = "GPT-4o",
            provider = "OpenAI",
            contextWindow = 128000,
            supportsTools = true,
            supportsVision = true
        ),
        ModelInfo(
            id = "openai/o3-mini",
            name = "o3-mini",
            provider = "OpenAI",
            contextWindow = 200000,
            supportsTools = true,
            supportsVision = false,
            reasoningEffort = "medium"
        ),
        ModelInfo(
            id = "google/gemini-2.0-flash",
            name = "Gemini 2.0 Flash",
            provider = "Google",
            contextWindow = 1000000,
            supportsTools = true,
            supportsVision = true
        ),
        ModelInfo(
            id = "deepseek/deepseek-r1",
            name = "DeepSeek R1",
            provider = "DeepSeek",
            contextWindow = 64000,
            supportsTools = true,
            supportsVision = false
        )
    )

    val Agents = listOf(
        AgentInfo(
            id = "coder",
            name = "Coder",
            description = "Autonomous software engineer for implementing features, debugging, and executing tools.",
            defaultModelId = "anthropic/claude-3-7-sonnet",
            isDefault = true
        ),
        AgentInfo(
            id = "task",
            name = "Task Runner",
            description = "Specialized in running builds, test suites, linter checks, and shell diagnostics.",
            defaultModelId = "google/gemini-2.0-flash",
            isDefault = false
        ),
        AgentInfo(
            id = "summarizer",
            name = "Summarizer",
            description = "Compacts long sessions and creates concise summaries without losing context.",
            defaultModelId = "openai/gpt-4o",
            isDefault = false
        ),
        AgentInfo(
            id = "title",
            name = "Title Generator",
            description = "Generates high-precision, short session titles based on conversation topics.",
            defaultModelId = "google/gemini-2.0-flash",
            isDefault = false
        )
    )

    val InitialSessions = listOf(
        Session(
            id = "sess-1",
            title = "Refactor Auth Middleware to JWT",
            projectId = "proj-1",
            messageCount = 4
        ),
        Session(
            id = "sess-2",
            title = "Implement LSP Diagnostic Highlighting",
            projectId = "proj-1",
            messageCount = 2
        ),
        Session(
            id = "sess-3",
            title = "Initialize OpenCode.md guidelines",
            projectId = "proj-1",
            messageCount = 1
        )
    )

    val InitialMessages = mutableMapOf<String, List<ChatMessage>>(
        "sess-1" to listOf(
            ChatMessage(
                id = "m1",
                sessionId = "sess-1",
                role = MessageRole.USER,
                content = "Please refactor the authentication middleware in internal/auth/middleware.go to support both API keys and JWT Bearer tokens with claims validation."
            ),
            ChatMessage(
                id = "m2",
                sessionId = "sess-1",
                role = MessageRole.ASSISTANT,
                content = "I will inspect `internal/auth/middleware.go` and implement the Dual-Authenticator pattern supporting both `Bearer <token>` and `X-API-Key` headers.",
                toolExecutions = listOf(
                    ToolExecution(
                        id = "t1",
                        toolName = "view",
                        summary = "internal/auth/middleware.go",
                        arguments = """{"path": "internal/auth/middleware.go", "start": 1, "end": 45}""",
                        output = """package auth

import (
    "net/http"
    "strings"
)

func AuthMiddleware(next http.Handler) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        key := r.Header.Get("X-API-Key")
        if key == "" {
            http.Error(w, "Unauthorized", http.StatusUnauthorized)
            return
        }
        next.ServeHTTP(w, r)
    })
}""",
                        status = ToolStatus.COMPLETED
                    ),
                    ToolExecution(
                        id = "t2",
                        toolName = "edit",
                        summary = "Update AuthMiddleware to support JWT Bearer validation",
                        arguments = """{"path": "internal/auth/middleware.go", "action": "replace"}""",
                        output = "Successfully applied 1 hunk (34 lines added, 6 lines removed).",
                        status = ToolStatus.COMPLETED
                    ),
                    ToolExecution(
                        id = "t3",
                        toolName = "bash",
                        summary = "go test ./internal/auth/...",
                        arguments = """{"command": "go test -v ./internal/auth/..."}""",
                        output = """=== RUN   TestAuthMiddleware
=== RUN   TestAuthMiddleware/APIKey_Success
=== RUN   TestAuthMiddleware/JWTBearer_Success
=== RUN   TestAuthMiddleware/InvalidToken_Rejected
--- PASS: TestAuthMiddleware (0.04s)
PASS
ok  	github.com/opencode-ai/opencode/internal/auth	0.062s""",
                        status = ToolStatus.COMPLETED
                    )
                ),
                permissionRequest = PermissionRequest(
                    id = "perm-1",
                    toolName = "bash",
                    description = "Allow running test suite on local environment?",
                    commandOrPath = "go test -v ./internal/auth/...",
                    isApproved = true
                )
            ),
            ChatMessage(
                id = "m3",
                sessionId = "sess-1",
                role = MessageRole.ASSISTANT,
                content = """### Summary of Changes

1. **JWT Parser Integration**: Added token decoding with claims verification for `exp`, `sub`, and `roles`.
2. **Fallback Compatibility**: If no `Authorization: Bearer` header is present, it gracefully checks for `X-API-Key`.
3. **Context Injection**: The verified user identity is attached to `r.Context()` under `auth.UserContextKey`.

All 3 unit tests passed cleanly! Check the **Changes** tab to review the diff.""",
                userQuestion = UserQuestion(
                    id = "q-1",
                    question = "Would you like me to commit these changes and generate a git commit message?",
                    options = listOf("Yes, generate conventional commit", "Review diff first", "No, keep uncommitted"),
                    isMultiSelect = false,
                    selectedOptions = listOf("Yes, generate conventional commit"),
                    isAnswered = true
                )
            )
        )
    )

    val FileChanges = listOf(
        FileChange(
            path = "internal/auth/middleware.go",
            status = "modified",
            additions = 34,
            deletions = 6,
            hunks = listOf(
                DiffHunk(
                    header = "@@ -7,9 +7,37 @@ import (",
                    lines = listOf(
                        DiffLine(DiffLineType.CONTEXT, 7, 7, "    \"net/http\""),
                        DiffLine(DiffLineType.CONTEXT, 8, 8, "    \"strings\""),
                        DiffLine(DiffLineType.ADDED, null, 9, "    \"github.com/golang-jwt/jwt/v5\""),
                        DiffLine(DiffLineType.ADDED, null, 10, "    \"context\""),
                        DiffLine(DiffLineType.CONTEXT, 9, 11, ")"),
                        DiffLine(DiffLineType.DELETED, 11, null, "func AuthMiddleware(next http.Handler) http.Handler {"),
                        DiffLine(DiffLineType.DELETED, 12, null, "    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {"),
                        DiffLine(DiffLineType.DELETED, 13, null, "        key := r.Header.Get(\"X-API-Key\")"),
                        DiffLine(DiffLineType.ADDED, null, 12, "func AuthMiddleware(validator TokenValidator, next http.Handler) http.Handler {"),
                        DiffLine(DiffLineType.ADDED, null, 13, "    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {"),
                        DiffLine(DiffLineType.ADDED, null, 14, "        authHeader := r.Header.Get(\"Authorization\")"),
                        DiffLine(DiffLineType.ADDED, null, 15, "        if strings.HasPrefix(authHeader, \"Bearer \") {"),
                        DiffLine(DiffLineType.ADDED, null, 16, "            token := strings.TrimPrefix(authHeader, \"Bearer \")"),
                        DiffLine(DiffLineType.ADDED, null, 17, "            claims, err := validator.ValidateJWT(token)"),
                        DiffLine(DiffLineType.ADDED, null, 18, "            if err != nil {"),
                        DiffLine(DiffLineType.ADDED, null, 19, "                http.Error(w, \"Invalid token: \"+err.Error(), http.StatusUnauthorized)"),
                        DiffLine(DiffLineType.ADDED, null, 20, "                return"),
                        DiffLine(DiffLineType.ADDED, null, 21, "            }"),
                        DiffLine(DiffLineType.ADDED, null, 22, "            ctx := context.WithValue(r.Context(), UserContextKey, claims)"),
                        DiffLine(DiffLineType.ADDED, null, 23, "            next.ServeHTTP(w, r.WithContext(ctx))"),
                        DiffLine(DiffLineType.ADDED, null, 24, "            return"),
                        DiffLine(DiffLineType.ADDED, null, 25, "        }"),
                        DiffLine(DiffLineType.CONTEXT, 14, 26, "        key := r.Header.Get(\"X-API-Key\")")
                    )
                )
            )
        ),
        FileChange(
            path = "internal/auth/middleware_test.go",
            status = "added",
            additions = 52,
            deletions = 0,
            hunks = listOf(
                DiffHunk(
                    header = "@@ -0,0 +1,52 @@",
                    lines = listOf(
                        DiffLine(DiffLineType.ADDED, null, 1, "package auth_test"),
                        DiffLine(DiffLineType.ADDED, null, 2, ""),
                        DiffLine(DiffLineType.ADDED, null, 3, "import ("),
                        DiffLine(DiffLineType.ADDED, null, 4, "    \"testing\""),
                        DiffLine(DiffLineType.ADDED, null, 5, "    \"net/http\""),
                        DiffLine(DiffLineType.ADDED, null, 6, "    \"net/http/httptest\""),
                        DiffLine(DiffLineType.ADDED, null, 7, ")"),
                        DiffLine(DiffLineType.ADDED, null, 8, ""),
                        DiffLine(DiffLineType.ADDED, null, 9, "func TestAuthMiddleware(t *testing.T) {"),
                        DiffLine(DiffLineType.ADDED, null, 10, "    // Test cases for Bearer & API-Key"),
                        DiffLine(DiffLineType.ADDED, null, 11, "}")
                    )
                )
            )
        )
    )
}

class MockSessionRepository : SessionRepository {
    private val _sessions = MutableStateFlow(MockData.InitialSessions)
    override val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>("sess-1")
    override val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    override suspend fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    override suspend fun createNewSession(projectId: String, title: String?): Session {
        val newSession = Session(
            id = "sess-" + UUID.randomUUID().toString().take(6),
            title = title ?: "New Session #${_sessions.value.size + 1}",
            projectId = projectId,
            messageCount = 0
        )
        _sessions.value = listOf(newSession) + _sessions.value
        _currentSessionId.value = newSession.id
        return newSession
    }

    override suspend fun renameSession(sessionId: String, newTitle: String) {
        _sessions.value = _sessions.value.map {
            if (it.id == sessionId) it.copy(title = newTitle, updatedAt = System.currentTimeMillis()) else it
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        if (_currentSessionId.value == sessionId) {
            _currentSessionId.value = _sessions.value.firstOrNull()?.id
        }
    }
}

class MockMessageRepository : MessageRepository {
    private val messagesBySession = MutableStateFlow(MockData.InitialMessages.toMutableMap())
    private var activeJob: Job? = null

    override fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> = flow {
        emit(messagesBySession.value[sessionId] ?: emptyList())
        messagesBySession.collect { map ->
            emit(map[sessionId] ?: emptyList())
        }
    }

    override suspend fun sendMessage(
        sessionId: String,
        text: String,
        modelId: String,
        agentId: String
    ): Flow<ChatMessage> = flow {
        val userMsg = ChatMessage(
            id = "user-" + UUID.randomUUID().toString().take(6),
            sessionId = sessionId,
            role = MessageRole.USER,
            content = text
        )

        val currentList = messagesBySession.value[sessionId]?.toMutableList() ?: mutableListOf()
        currentList.add(userMsg)
        val updatedMap = messagesBySession.value.toMutableMap()
        updatedMap[sessionId] = currentList
        messagesBySession.value = updatedMap

        val assistantMsgId = "asst-" + UUID.randomUUID().toString().take(6)
        var assistantMsg = ChatMessage(
            id = assistantMsgId,
            sessionId = sessionId,
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true
        )

        currentList.add(assistantMsg)
        updatedMap[sessionId] = currentList
        messagesBySession.value = updatedMap

        val streamParts = listOf(
            "Analyzing request with $agentId agent using $modelId...\n\n",
            "Inspecting repository structure and active changes...\n\n",
            "```kotlin\n// OpenCode Android native implementation\nval status = \"running\"\n```\n\n",
            "Executed tool `view` and verified component consistency.",
            "\n\nReady for next step!"
        )

        val tool = ToolExecution(
            id = "tool-" + UUID.randomUUID().toString().take(4),
            toolName = "view",
            summary = "src/App.kt",
            arguments = """{"file": "src/App.kt"}""",
            output = "// Checked 1 file",
            status = ToolStatus.COMPLETED
        )

        for (part in streamParts) {
            delay(250)
            assistantMsg = assistantMsg.copy(
                content = assistantMsg.content + part,
                toolExecutions = listOf(tool)
            )
            val list = messagesBySession.value[sessionId]?.toMutableList() ?: mutableListOf()
            val idx = list.indexOfFirst { it.id == assistantMsgId }
            if (idx != -1) {
                list[idx] = assistantMsg
                val newMap = messagesBySession.value.toMutableMap()
                newMap[sessionId] = list
                messagesBySession.value = newMap
            }
            emit(assistantMsg)
        }

        assistantMsg = assistantMsg.copy(isStreaming = false)
        val finalList = messagesBySession.value[sessionId]?.toMutableList() ?: mutableListOf()
        val finalIdx = finalList.indexOfFirst { it.id == assistantMsgId }
        if (finalIdx != -1) {
            finalList[finalIdx] = assistantMsg
            val finalMap = messagesBySession.value.toMutableMap()
            finalMap[sessionId] = finalList
            messagesBySession.value = finalMap
        }
        emit(assistantMsg)
    }

    override suspend fun cancelGeneration(sessionId: String) {
        activeJob?.cancel(CancellationException("Generation canceled by user"))
        val list = messagesBySession.value[sessionId]?.toMutableList() ?: return
        val lastIdx = list.indexOfLast { it.isStreaming }
        if (lastIdx != -1) {
            list[lastIdx] = list[lastIdx].copy(isStreaming = false)
            val updated = messagesBySession.value.toMutableMap()
            updated[sessionId] = list
            messagesBySession.value = updated
        }
    }

    override suspend fun respondToPermission(messageId: String, requestId: String, approved: Boolean) {
        val map = messagesBySession.value.toMutableMap()
        for ((sessId, list) in map) {
            val idx = list.indexOfFirst { it.id == messageId }
            if (idx != -1) {
                val msg = list[idx]
                if (msg.permissionRequest?.id == requestId) {
                    val updatedMsg = msg.copy(
                        permissionRequest = msg.permissionRequest.copy(isApproved = approved)
                    )
                    val newList = list.toMutableList()
                    newList[idx] = updatedMsg
                    map[sessId] = newList
                    messagesBySession.value = map
                    break
                }
            }
        }
    }

    override suspend fun answerQuestion(
        messageId: String,
        questionId: String,
        answers: List<String>,
        freeText: String?
    ) {
        val map = messagesBySession.value.toMutableMap()
        for ((sessId, list) in map) {
            val idx = list.indexOfFirst { it.id == messageId }
            if (idx != -1) {
                val msg = list[idx]
                if (msg.userQuestion?.id == questionId) {
                    val updatedMsg = msg.copy(
                        userQuestion = msg.userQuestion.copy(
                            selectedOptions = answers,
                            freeTextAnswer = freeText,
                            isAnswered = true
                        )
                    )
                    val newList = list.toMutableList()
                    newList[idx] = updatedMsg
                    map[sessId] = newList
                    messagesBySession.value = map
                    break
                }
            }
        }
    }
}

class MockModelRepository : ModelRepository {
    private val _models = MutableStateFlow(MockData.Models)
    override val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    private val _selectedModel = MutableStateFlow(MockData.Models.first())
    override val selectedModel: StateFlow<ModelInfo> = _selectedModel.asStateFlow()

    override suspend fun selectModel(modelId: String) {
        val model = _models.value.firstOrNull { it.id == modelId } ?: return
        _selectedModel.value = model
    }
}

class MockAgentRepository : AgentRepository {
    private val _agents = MutableStateFlow(MockData.Agents)
    override val agents: StateFlow<List<AgentInfo>> = _agents.asStateFlow()

    private val _selectedAgent = MutableStateFlow(MockData.Agents.first())
    override val selectedAgent: StateFlow<AgentInfo> = _selectedAgent.asStateFlow()

    override suspend fun selectAgent(agentId: String) {
        val agent = _agents.value.firstOrNull { it.id == agentId } ?: return
        _selectedAgent.value = agent
    }
}

class MockProjectRepository : ProjectRepository {
    private val _projects = MutableStateFlow(MockData.Projects)
    override val projects: StateFlow<List<ProjectInfo>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectInfo?>(MockData.Projects.first())
    override val currentProject: StateFlow<ProjectInfo?> = _currentProject.asStateFlow()

    override suspend fun selectProject(projectId: String) {
        _projects.value = _projects.value.map {
            it.copy(isCurrent = it.id == projectId)
        }
        _currentProject.value = _projects.value.firstOrNull { it.id == projectId }
    }
}

class MockDiffRepository : DiffRepository {
    private val _fileChanges = MutableStateFlow(MockData.FileChanges)
    override val fileChanges: StateFlow<List<FileChange>> = _fileChanges.asStateFlow()

    override suspend fun refreshDiffs() {
        _fileChanges.value = MockData.FileChanges
    }
}

class MockSettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override suspend fun updateTheme(themeName: String, isDark: Boolean) {
        _settings.value = _settings.value.copy(themeName = themeName, isDark = isDark)
    }

    override suspend fun updateServerUrl(url: String) {
        _settings.value = _settings.value.copy(serverUrl = url)
    }

    override suspend fun toggleMockMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(isMockMode = enabled)
    }
}
