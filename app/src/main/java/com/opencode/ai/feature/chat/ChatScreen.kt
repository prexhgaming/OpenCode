package com.opencode.ai.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.opencode.ai.core.components.AgentPickerSheet
import com.opencode.ai.core.components.CommandPaletteDialog
import com.opencode.ai.core.components.EmptyConversationState
import com.opencode.ai.core.components.MessageBubbleView
import com.opencode.ai.core.components.MessageComposer
import com.opencode.ai.core.components.ModelPickerSheet
import com.opencode.ai.core.components.OpenCodeCommand
import com.opencode.ai.core.components.OpenCodeTopBar
import com.opencode.ai.core.design.OpenCodeTheme

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSessions: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToDiff: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content?.length) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val commands = listOf(
        OpenCodeCommand(
            id = "init",
            title = "Initialize Project",
            description = "Create or update the OpenCode.md guidelines",
            icon = Icons.Default.Build,
            action = {
                viewModel.onInputTextChanged("/init")
                viewModel.sendMessage()
            }
        ),
        OpenCodeCommand(
            id = "compact",
            title = "Compact Session",
            description = "Summarize current conversation into a concise memory",
            icon = Icons.Default.Summarize,
            action = {
                viewModel.onInputTextChanged("/compact")
                viewModel.sendMessage()
            }
        ),
        OpenCodeCommand(
            id = "diff",
            title = "View File Changes",
            description = "Inspect unstaged file modifications and unified diffs",
            icon = Icons.Default.InsertDriveFile,
            action = onNavigateToDiff
        ),
        OpenCodeCommand(
            id = "models",
            title = "Switch Model",
            description = "Select provider and model (Claude, GPT-4o, Gemini...)",
            icon = Icons.Default.Psychology,
            action = { viewModel.setShowModelPicker(true) }
        ),
        OpenCodeCommand(
            id = "agents",
            title = "Switch Agent",
            description = "Change active agent role (Coder, Task, Summarizer)",
            icon = Icons.Default.Code,
            action = { viewModel.setShowAgentPicker(true) }
        ),
        OpenCodeCommand(
            id = "sessions",
            title = "Switch Session",
            description = "Browse and open past conversation sessions",
            icon = Icons.Default.History,
            action = onNavigateToSessions
        ),
        OpenCodeCommand(
            id = "settings",
            title = "Open Settings",
            description = "Configure theme, server connection, and preferences",
            icon = Icons.Default.Settings,
            action = onNavigateToSettings
        )
    )

    Scaffold(
        topBar = {
            OpenCodeTopBar(
                sessionTitle = uiState.currentSession?.title ?: "New Session",
                projectName = uiState.currentProject?.name ?: "opencode",
                selectedModel = uiState.selectedModel,
                agentName = uiState.selectedAgent?.name ?: "Coder",
                diffCount = uiState.diffCount,
                onMenuClick = onNavigateToSessions,
                onProjectClick = onNavigateToProjects,
                onModelClick = { viewModel.setShowModelPicker(true) },
                onAgentClick = { viewModel.setShowAgentPicker(true) },
                onDiffClick = onNavigateToDiff,
                onCommandPaletteClick = { viewModel.setShowCommandPalette(true) }
            )
        },
        bottomBar = {
            MessageComposer(
                text = uiState.inputText,
                onTextChanged = viewModel::onInputTextChanged,
                onSend = viewModel::sendMessage,
                onCancel = viewModel::cancelGeneration,
                isStreaming = uiState.isStreaming,
                onCommandClick = { viewModel.setShowCommandPalette(true) },
                onAttachClick = {
                    viewModel.onInputTextChanged(uiState.inputText + " @file:internal/auth/middleware.go ")
                }
            )
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            if (uiState.messages.isEmpty()) {
                EmptyConversationState(
                    onSuggestionClick = { prompt ->
                        viewModel.onInputTextChanged(prompt)
                        viewModel.sendMessage()
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.messages) { message ->
                        MessageBubbleView(
                            message = message,
                            onPermissionResponse = viewModel::respondToPermission,
                            onAnswerQuestion = viewModel::answerQuestion
                        )
                    }
                }
            }

            // Model Picker Bottom Sheet
            if (uiState.showModelPicker) {
                ModelPickerSheet(
                    models = uiState.availableModels,
                    selectedModelId = uiState.selectedModel?.id ?: "",
                    onModelSelected = viewModel::selectModel,
                    onDismiss = { viewModel.setShowModelPicker(false) }
                )
            }

            // Agent Picker Bottom Sheet
            if (uiState.showAgentPicker) {
                AgentPickerSheet(
                    agents = uiState.availableAgents,
                    selectedAgentId = uiState.selectedAgent?.id ?: "",
                    onAgentSelected = viewModel::selectAgent,
                    onDismiss = { viewModel.setShowAgentPicker(false) }
                )
            }

            // Command Palette Dialog
            if (uiState.showCommandPalette) {
                CommandPaletteDialog(
                    commands = commands,
                    onDismiss = { viewModel.setShowCommandPalette(false) }
                )
            }
        }
    }
}
