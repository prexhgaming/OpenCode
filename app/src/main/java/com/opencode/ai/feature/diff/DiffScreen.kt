package com.opencode.ai.feature.diff

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.ai.core.components.DiffViewerComponent
import com.opencode.ai.core.design.OpenCodeTheme

@Composable
fun DiffScreen(
    viewModel: DiffViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    val uiState by viewModel.uiState.collectAsState()

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text)
                        }
                        Text(
                            text = "Unstaged Changes (${uiState.fileChanges.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.text
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+${uiState.totalAdditions}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = colors.success
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "-${uiState.totalDeletions}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = colors.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = viewModel::refreshDiffs,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.textMuted)
                        }
                    }
                }
            }
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        DiffViewerComponent(
            fileChanges = uiState.fileChanges,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
