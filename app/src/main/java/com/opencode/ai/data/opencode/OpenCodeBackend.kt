package com.opencode.ai.data.opencode

import android.content.Context
import android.util.Base64
import com.opencode.ai.domain.model.*
import com.opencode.ai.domain.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

/** Adapter for the public OpenCode HTTP server API. It does not implement an agent loop. */
class OpenCodeBackend(context: Context) : SessionRepository, MessageRepository, ModelRepository,
    AgentRepository, ProjectRepository, DiffRepository, SettingsRepository {
    private val prefs = context.getSharedPreferences("opencode_connection", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    private val _settings = MutableStateFlow(AppSettings(isMockMode = false, serverUrl = prefs.getString("url", "http://127.0.0.1:4096")!!))
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    override val sessions = MutableStateFlow<List<Session>>(emptyList())
    override val currentSessionId = MutableStateFlow<String?>(null)
    override val models = MutableStateFlow<List<ModelInfo>>(emptyList())
    private val _selectedModel = MutableStateFlow(ModelInfo("", "Select a model", "OpenCode"))
    override val selectedModel: StateFlow<ModelInfo> = _selectedModel.asStateFlow()
    private val _agents = MutableStateFlow<List<AgentInfo>>(emptyList())
    override val agents: StateFlow<List<AgentInfo>> = _agents.asStateFlow()
    private val _selectedAgent = MutableStateFlow(AgentInfo("", "Default", "Backend default agent", ""))
    override val selectedAgent: StateFlow<AgentInfo> = _selectedAgent.asStateFlow()
    override val projects = MutableStateFlow<List<ProjectInfo>>(emptyList())
    private val _currentProject = MutableStateFlow<ProjectInfo?>(null)
    override val currentProject: StateFlow<ProjectInfo?> = _currentProject.asStateFlow()
    override val fileChanges = MutableStateFlow<List<FileChange>>(emptyList())
    private val messages = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var eventJob: Job? = null
    private val busySessions = mutableSetOf<String>()

    init { reconnectEvents(); scope.launch { refreshAll() } }

    private fun base(): String = _settings.value.serverUrl.trim().trimEnd('/')
    private fun path(p: String) = base() + p
    private fun request(method: String, endpoint: String, body: JsonElement? = null): String {
        val c = (URL(path(endpoint)).openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 9000; readTimeout = 30000
            setRequestProperty("Accept", "application/json")
            if (body != null) { doOutput = true; setRequestProperty("Content-Type", "application/json") }
            val user = prefs.getString("username", "") ?: ""
            val pass = prefs.getString("password", "") ?: ""
            if (user.isNotEmpty() || pass.isNotEmpty()) setRequestProperty("Authorization", "Basic " + Base64.encodeToString("$user:$pass".toByteArray(), Base64.NO_WRAP))
        }
        try {
            if (body != null) c.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw IllegalStateException("OpenCode HTTP $code${if (text.isNotBlank()) ": ${text.take(300)}" else ""}")
            return text
        } finally { c.disconnect() }
    }
    private suspend fun get(endpoint: String) = withContext(Dispatchers.IO) { request("GET", endpoint) }
    private suspend fun post(endpoint: String, body: JsonElement = buildJsonObject {}) = withContext(Dispatchers.IO) { request("POST", endpoint, body) }
    private suspend fun patch(endpoint: String, body: JsonElement) = withContext(Dispatchers.IO) { request("PATCH", endpoint, body) }
    private suspend fun delete(endpoint: String) = withContext(Dispatchers.IO) { request("DELETE", endpoint) }
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
    private fun JsonElement.obj() = this as? JsonObject ?: JsonObject(emptyMap())
    private fun JsonElement.str(k: String, d: String = "") = this.obj()[k]?.jsonPrimitive?.contentOrNull ?: d
    private fun JsonElement.long(k: String, d: Long = 0L) = this.obj()[k]?.jsonPrimitive?.longOrNull ?: d
    private fun JsonElement.arr(k: String) = this.obj()[k] as? JsonArray ?: JsonArray(emptyList())
    private fun sessionFrom(e: JsonElement): Session {
        val time = e.obj()["time"]?.obj()
        val dir = e.str("directory", _currentProject.value?.path.orEmpty())
        return Session(e.str("id"), e.str("title", "Untitled session"), dir, time?.get("created")?.jsonPrimitive?.longOrNull ?: 0L, time?.get("updated")?.jsonPrimitive?.longOrNull ?: 0L)
    }
    private fun refreshEvents() {
        eventJob?.cancel()
        eventJob = scope.launch {
            while (isActive) {
                var conn: HttpURLConnection? = null
                try {
                    conn = URL(path("/event")).openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000; conn.readTimeout = 0
                    conn.setRequestProperty("Accept", "text/event-stream")
                    val u = prefs.getString("username", "") ?: ""; val p = prefs.getString("password", "") ?: ""
                    if (u.isNotEmpty() || p.isNotEmpty()) conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString("$u:$p".toByteArray(), Base64.NO_WRAP))
                    if (conn.responseCode !in 200..299) throw IllegalStateException("SSE HTTP ${conn.responseCode}")
                    BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                        var line: String?
                        while (isActive && reader.readLine().also { line = it } != null) {
                            if (line!!.startsWith("data:")) {
                                val sid = currentSessionId.value
                                if (sid != null) launch { refreshMessages(sid); refreshDiffs() }
                            }
                        }
                    }
                } catch (_: Exception) { delay(2500) } finally { conn?.disconnect() }
            }
        }
    }
    private fun reconnectEvents() = refreshEvents()
    suspend fun refreshAll() {
        try { refreshProjects(); refreshSessions(); refreshModels(); refreshAgents(); currentSessionId.value?.let { refreshMessages(it) } } catch (_: Exception) { }
    }
    suspend fun checkConnection(): String = try { get("/global/health") } catch (e: Exception) { throw IllegalStateException("Cannot connect to OpenCode at ${base()}: ${e.message}", e) }
    private suspend fun refreshProjects() {
        val root = json.parseToJsonElement(get("/project"))
        val arr = root as? JsonArray ?: root.obj()["projects"] as? JsonArray ?: JsonArray(emptyList())
        val current = try { json.parseToJsonElement(get("/project/current")) } catch (_: Exception) { JsonNull }
        val cp = current.takeUnless { it is JsonNull }?.let { ProjectInfo(it.str("id", it.str("worktree", it.str("directory"))), it.str("name", it.str("worktree", "Current project")), it.str("worktree", it.str("directory")), true) }
        val list = arr.map { ProjectInfo(it.str("id", it.str("worktree", it.str("directory"))), it.str("name", it.str("worktree", "Project")), it.str("worktree", it.str("directory")), it.str("id") == cp?.id) }.toMutableList()
        if (cp != null && list.none { it.id == cp.id }) list.add(0, cp)
        projects.value = list; _currentProject.value = cp ?: list.firstOrNull()
    }
    private suspend fun refreshSessions() {
        val root = json.parseToJsonElement(get("/session"))
        sessions.value = (root as? JsonArray ?: JsonArray(emptyList())).map(::sessionFrom)
        if (currentSessionId.value == null) currentSessionId.value = sessions.value.firstOrNull()?.id
    }
    private suspend fun refreshModels() {
        val root = json.parseToJsonElement(get("/provider"))
        val providers = root.obj()["all"] as? JsonArray ?: JsonArray(emptyList())
        val result = mutableListOf<ModelInfo>()
        providers.forEach { provider ->
            val pid = provider.str("id")
            val ms = provider.arr("models")
            ms.forEach { m -> result += ModelInfo("$pid/${m.str("id")}", m.str("name", m.str("id")), provider.str("name", pid), m.obj()["limit"]?.obj()?.get("context")?.jsonPrimitive?.intOrNull ?: 0) }
        }
        models.value = result
        val defaults = root.obj()["default"]?.obj()
        val defaultId = defaults?.let { "${it.str("providerID")}/${it.str("modelID")}" }
        _selectedModel.value = result.firstOrNull { it.id == defaultId } ?: result.firstOrNull() ?: ModelInfo("", "No models returned", "OpenCode")
    }
    private suspend fun refreshAgents() {
        val root = json.parseToJsonElement(get("/agent"))
        val arr = root as? JsonArray ?: JsonArray(emptyList())
        _agents.value = arr.map { AgentInfo(it.str("name"), it.str("name"), it.str("description"), "", it.str("mode") == "primary") }
        _selectedAgent.value = _agents.value.firstOrNull { it.isDefault } ?: _agents.value.firstOrNull() ?: AgentInfo("", "Default", "Backend default", "")
    }
    override suspend fun selectSession(sessionId: String) { currentSessionId.value = sessionId; refreshMessages(sessionId); refreshDiffs() }
    override suspend fun createNewSession(projectId: String, title: String?): Session {
        val body = buildJsonObject { if (!title.isNullOrBlank()) put("title", title) }
        val created = sessionFrom(json.parseToJsonElement(post("/session", body)))
        sessions.value = listOf(created) + sessions.value; currentSessionId.value = created.id; messages[created.id] = MutableStateFlow(emptyList()); return created
    }
    override suspend fun renameSession(sessionId: String, newTitle: String) { val updated = sessionFrom(json.parseToJsonElement(patch("/session/$sessionId", buildJsonObject { put("title", newTitle) }))); sessions.value = sessions.value.map { if (it.id == sessionId) updated else it } }
    override suspend fun deleteSession(sessionId: String) { delete("/session/$sessionId"); sessions.value = sessions.value.filterNot { it.id == sessionId }; messages.remove(sessionId); if (currentSessionId.value == sessionId) currentSessionId.value = sessions.value.firstOrNull()?.id }
    private suspend fun refreshMessages(id: String) {
        try {
            val root = json.parseToJsonElement(get("/session/$id/message")) as? JsonArray ?: JsonArray(emptyList())
            val mapped = root.mapNotNull { item ->
                val info = item.obj()["info"] ?: return@mapNotNull null
                val role = if (info.str("role") == "user") MessageRole.USER else MessageRole.ASSISTANT
                val parts = item.arr("parts")
                val text = parts.mapNotNull { p -> when (p.str("type")) { "text" -> p.str("text"); "tool" -> "\n[${p.str("tool", "tool")}] ${p.obj()["state"]?.obj()?.get("status")?.jsonPrimitive?.contentOrNull ?: ""}"; else -> null } }.joinToString("")
                ChatMessage(info.str("id", UUID.randomUUID().toString()), id, role, text, info.obj()["time"]?.obj()?.get("created")?.jsonPrimitive?.longOrNull ?: 0L)
            }
            messages.getOrPut(id) { MutableStateFlow(emptyList()) }.value = mapped
        } catch (_: Exception) { }
    }
    override fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> = messages.getOrPut(sessionId) { MutableStateFlow(emptyList()) }.asStateFlow()
    override suspend fun sendMessage(sessionId: String, text: String, modelId: String, agentId: String): Flow<ChatMessage> = flow {
        synchronized(busySessions) { if (sessionId in busySessions) throw IllegalStateException("A response is already in progress for this session") else busySessions.add(sessionId) }
        try {
            val pieces = modelId.split('/', limit = 2)
            val body = buildJsonObject {
                put("agent", agentId)
                if (pieces.size == 2) put("model", buildJsonObject { put("providerID", pieces[0]); put("modelID", pieces[1]) })
                put("parts", buildJsonArray { add(buildJsonObject { put("type", "text"); put("text", text) }) })
            }
            emit(ChatMessage("pending-${UUID.randomUUID()}", sessionId, MessageRole.ASSISTANT, "OpenCode is processing…", isStreaming = true))
            // prompt_async returns immediately; the canonical messages arrive through the server event stream.
            post("/session/$sessionId/prompt_async", body)
            delay(350); refreshMessages(sessionId); emit(getMessagesForSession(sessionId).firstOrNull()?.lastOrNull() ?: ChatMessage("pending", sessionId, MessageRole.ASSISTANT, "OpenCode is working…", isStreaming = true))
        } finally { synchronized(busySessions) { busySessions.remove(sessionId) } }
    }
    override suspend fun cancelGeneration(sessionId: String) { post("/session/$sessionId/abort"); refreshMessages(sessionId) }
    override suspend fun respondToPermission(messageId: String, requestId: String, approved: Boolean) {
        val session = currentSessionId.value ?: return
        post("/session/$session/permissions/$requestId", buildJsonObject { put("response", if (approved) "once" else "reject") })
    }
    override suspend fun answerQuestion(messageId: String, questionId: String, answers: List<String>, freeText: String?) {
        throw UnsupportedOperationException("Question reply schema must be confirmed against this server's /doc OpenAPI specification")
    }
    override suspend fun selectModel(modelId: String) { _selectedModel.value = models.value.firstOrNull { it.id == modelId } ?: _selectedModel.value }
    override suspend fun selectAgent(agentId: String) { _selectedAgent.value = agents.value.firstOrNull { it.id == agentId } ?: _selectedAgent.value }
    override suspend fun selectProject(projectId: String) { _currentProject.value = projects.value.firstOrNull { it.id == projectId } }
    override suspend fun refreshDiffs() {
        val id = currentSessionId.value ?: return
        try {
            val arr = json.parseToJsonElement(get("/session/$id/diff")) as? JsonArray ?: JsonArray(emptyList())
            fileChanges.value = arr.map { d -> FileChange(d.str("file", d.str("path")), status = "modified", additions = d.long("additions").toInt(), deletions = d.long("deletions").toInt()) }
        } catch (_: Exception) { }
    }
    override suspend fun updateTheme(themeName: String, isDark: Boolean) { _settings.value = _settings.value.copy(themeName = themeName, isDark = isDark); prefs.edit().putString("theme", themeName).apply() }
    override suspend fun updateServerUrl(url: String) { _settings.value = _settings.value.copy(serverUrl = url.trim().trimEnd('/')); prefs.edit().putString("url", _settings.value.serverUrl).apply(); refreshEvents(); scope.launch { refreshAll() } }
    override suspend fun toggleMockMode(enabled: Boolean) { _settings.value = _settings.value.copy(isMockMode = enabled) }
    fun setBasicAuth(username: String, password: String) { prefs.edit().putString("username", username).putString("password", password).apply(); refreshEvents() }
}
