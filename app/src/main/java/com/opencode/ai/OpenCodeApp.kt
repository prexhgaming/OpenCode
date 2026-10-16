package com.opencode.ai

import android.app.Application
import com.opencode.ai.data.opencode.OpenCodeBackend
import com.opencode.ai.domain.repository.AgentRepository
import com.opencode.ai.domain.repository.DiffRepository
import com.opencode.ai.domain.repository.MessageRepository
import com.opencode.ai.domain.repository.ModelRepository
import com.opencode.ai.domain.repository.ProjectRepository
import com.opencode.ai.domain.repository.SessionRepository
import com.opencode.ai.domain.repository.SettingsRepository

class OpenCodeApp : Application() {

    companion object {
        lateinit var instance: OpenCodeApp
            private set
    }

    private val backend: OpenCodeBackend by lazy { OpenCodeBackend(this) }
    val sessionRepository: SessionRepository by lazy { backend }
    val messageRepository: MessageRepository by lazy { backend }
    val modelRepository: ModelRepository by lazy { backend }
    val agentRepository: AgentRepository by lazy { backend }
    val projectRepository: ProjectRepository by lazy { backend }
    val diffRepository: DiffRepository by lazy { backend }
    val settingsRepository: SettingsRepository by lazy { backend }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
