package com.opencode.ai

import com.opencode.ai.data.mock.MockAgentRepository
import com.opencode.ai.data.mock.MockDiffRepository
import com.opencode.ai.data.mock.MockMessageRepository
import com.opencode.ai.data.mock.MockModelRepository
import com.opencode.ai.data.mock.MockProjectRepository
import com.opencode.ai.data.mock.MockSessionRepository
import com.opencode.ai.data.mock.MockSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenCodeLogicTest {

    @Test
    fun testSessionCreationAndSelection() = runTest {
        val sessionRepo = MockSessionRepository()
        val initialSessions = sessionRepo.sessions.value
        assertEquals(3, initialSessions.size)

        // Create new session
        val created = sessionRepo.createNewSession("proj-1", "Feature: Support Claude 3.7 Thinking")
        assertNotNull(created)
        assertEquals("Feature: Support Claude 3.7 Thinking", created.title)

        // Check it is set as current session
        assertEquals(created.id, sessionRepo.currentSessionId.value)
        assertEquals(4, sessionRepo.sessions.value.size)

        // Switch back to session 1
        sessionRepo.selectSession("sess-1")
        assertEquals("sess-1", sessionRepo.currentSessionId.value)
    }

    @Test
    fun testModelSelection() = runTest {
        val modelRepo = MockModelRepository()
        assertEquals("anthropic/claude-3-7-sonnet", modelRepo.selectedModel.value.id)

        modelRepo.selectModel("google/gemini-2.0-flash")
        assertEquals("google/gemini-2.0-flash", modelRepo.selectedModel.value.id)
        assertEquals("Google", modelRepo.selectedModel.value.provider)
    }

    @Test
    fun testThemePreferenceUpdate() = runTest {
        val settingsRepo = MockSettingsRepository()
        assertEquals("opencode", settingsRepo.settings.value.themeName)
        assertTrue(settingsRepo.settings.value.isDark)

        settingsRepo.updateTheme("dracula", true)
        assertEquals("dracula", settingsRepo.settings.value.themeName)

        settingsRepo.updateTheme("light", false)
        assertEquals("light", settingsRepo.settings.value.themeName)
        assertFalse(settingsRepo.settings.value.isDark)
    }

    @Test
    fun testPermissionApprovalStateChange() = runTest {
        val messageRepo = MockMessageRepository()
        val messages = messageRepo.getMessagesForSession("sess-1").first()
        val msgWithPerm = messages.find { it.permissionRequest != null }
        assertNotNull(msgWithPerm)
        val permId = msgWithPerm!!.permissionRequest!!.id

        messageRepo.respondToPermission(msgWithPerm.id, permId, false)
        val updatedMessages = messageRepo.getMessagesForSession("sess-1").first()
        val updatedPerm = updatedMessages.find { it.id == msgWithPerm.id }?.permissionRequest
        assertNotNull(updatedPerm)
        assertEquals(false, updatedPerm?.isApproved)
    }
}
