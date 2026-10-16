package com.opencode.ai.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.opencode.ai.feature.chat.ChatScreen
import com.opencode.ai.feature.chat.ChatViewModel
import com.opencode.ai.feature.diff.DiffScreen
import com.opencode.ai.feature.diff.DiffViewModel
import com.opencode.ai.feature.sessions.SessionsScreen
import com.opencode.ai.feature.sessions.SessionsViewModel
import com.opencode.ai.feature.settings.SettingsScreen
import com.opencode.ai.feature.settings.SettingsViewModel

@Composable
fun OpenCodeNavGraph(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    sessionsViewModel: SessionsViewModel,
    diffViewModel: DiffViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route,
        modifier = modifier
    ) {
        composable(Screen.Chat.route) {
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSessions = { navController.navigate(Screen.Sessions.route) },
                onNavigateToProjects = { navController.navigate(Screen.Sessions.route) },
                onNavigateToDiff = { navController.navigate(Screen.Diff.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Sessions.route) {
            SessionsScreen(
                viewModel = sessionsViewModel,
                onSessionSelected = {
                    navController.popBackStack(Screen.Chat.route, false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Diff.route) {
            DiffScreen(
                viewModel = diffViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
