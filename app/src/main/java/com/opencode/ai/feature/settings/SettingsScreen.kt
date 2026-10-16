package com.opencode.ai.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.ai.core.design.OpenCodeTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    val settings by viewModel.settings.collectAsState()

    val availableThemes = listOf(
        "opencode" to "OpenCode Dark (Default)",
        "light" to "OpenCode Light",
        "dracula" to "Dracula",
        "tokyonight" to "Tokyo Night"
    )

    Scaffold(
        topBar = {
            Surface(
                color = colors.backgroundSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderNormal)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                }
            }
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme selection section
            SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance & Themes")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                availableThemes.forEach { (key, label) ->
                    val isSelected = settings.themeName.equals(key, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.surface else colors.backgroundSecondary)
                            .border(
                                1.dp,
                                if (isSelected) colors.primary else colors.borderDim,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setTheme(key) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) colors.primary else colors.text
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active theme",
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Backend & Server Connection
            SettingsSectionHeader(icon = Icons.Default.Router, title = "OpenCode Server")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.backgroundSecondary)
                    .border(1.dp, colors.borderDim, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Server API URL",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.background)
                        .border(1.dp, colors.borderDim, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = settings.serverUrl,
                        onValueChange = viewModel::setServerUrl,
                        textStyle = TextStyle(
                            color = colors.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Mock Prototype Mode (unavailable)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.text
                        )
                        Text(
                            text = "This build uses the real OpenCode server; it never silently falls back to simulated replies.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                            color = colors.textMuted
                        )
                    }

                    Switch(
                        checked = settings.isMockMode,
                        onCheckedChange = null,
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            checkedTrackColor = colors.primary.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // About OpenCode section
            SettingsSectionHeader(icon = Icons.Default.Info, title = "About OpenCode")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.backgroundSecondary)
                    .border(1.dp, colors.borderDim, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "OpenCode for Android",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
                Text(
                    text = "Version 1.0.0 (Native Kotlin & Jetpack Compose)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Faithful port of the open-source terminal AI coding agent. Supports multiple LLMs, autonomous tools (bash, edit, view, grep), and real-time diff inspect.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = colors.text
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String) {
    val colors = OpenCodeTheme.colors

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = colors.text
        )
    }
}
