package com.opencode.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.opencode.ai.core.design.OpenCodeAppTheme
import com.opencode.ai.core.design.OpenCodeTheme
import com.opencode.ai.core.navigation.OpenCodeNavGraph
import com.opencode.ai.feature.chat.ChatViewModel
import com.opencode.ai.feature.diff.DiffViewModel
import com.opencode.ai.feature.sessions.SessionsViewModel
import com.opencode.ai.feature.settings.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as OpenCodeApp

        setContent {
            val settingsViewModel = viewModel<SettingsViewModel> {
                SettingsViewModel(app.settingsRepository)
            }
            val settings by settingsViewModel.settings.collectAsState()

            OpenCodeAppTheme(
                themeName = settings.themeName,
                darkTheme = settings.isDark
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = OpenCodeTheme.colors.background
                ) {
                    val navController = rememberNavController()

                    val chatViewModel = viewModel<ChatViewModel> {
                        ChatViewModel(
                            sessionRepository = app.sessionRepository,
                            messageRepository = app.messageRepository,
                            modelRepository = app.modelRepository,
                            agentRepository = app.agentRepository,
                            projectRepository = app.projectRepository,
                            diffRepository = app.diffRepository
                        )
                    }

                    val sessionsViewModel = viewModel<SessionsViewModel> {
                        SessionsViewModel(
                            sessionRepository = app.sessionRepository,
                            projectRepository = app.projectRepository
                        )
                    }

                    val diffViewModel = viewModel<DiffViewModel> {
                        DiffViewModel(
                            diffRepository = app.diffRepository
                        )
                    }

                    OpenCodeNavGraph(
                        navController = navController,
                        chatViewModel = chatViewModel,
                        sessionsViewModel = sessionsViewModel,
                        diffViewModel = diffViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
