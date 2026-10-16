package com.opencode.ai.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.ai.domain.model.AppSettings
import com.opencode.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collectLatest { s ->
                _settings.value = s
            }
        }
    }

    fun setTheme(themeName: String) {
        val isDark = themeName.lowercase() != "light" && themeName.lowercase() != "opencode_light"
        viewModelScope.launch {
            settingsRepository.updateTheme(themeName, isDark)
        }
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.updateServerUrl(url)
        }
    }

    fun toggleMockMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleMockMode(enabled)
        }
    }
}
