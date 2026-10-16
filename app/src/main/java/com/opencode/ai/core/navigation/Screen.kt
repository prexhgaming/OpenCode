package com.opencode.ai.core.navigation

sealed class Screen(val route: String) {
    object Chat : Screen("chat")
    object Sessions : Screen("sessions")
    object Diff : Screen("diff")
    object Settings : Screen("settings")
}
