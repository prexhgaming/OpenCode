package com.opencode.ai.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.ai.core.design.MonospaceCodeStyle
import com.opencode.ai.core.design.OpenCodeTheme
import com.opencode.ai.domain.model.ChatMessage
import com.opencode.ai.domain.model.MessageRole
import com.opencode.ai.domain.model.ModelInfo
import com.opencode.ai.domain.model.ToolExecution
import com.opencode.ai.domain.model.ToolStatus

@Composable
fun OpenCodeTopBar(
    sessionTitle: String,
    projectName: String,
    selectedModel: ModelInfo?,
    agentName: String,
    diffCount: Int,
    onMenuClick: () -> Unit,
    onProjectClick: () -> Unit,
    onModelClick: () -> Unit,
    onAgentClick: () -> Unit,
    onDiffClick: () -> Unit,
    onCommandPaletteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors

    Surface(
        color = colors.backgroundSecondary,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.borderNormal)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // First row: Menu, Project / Session info, Command palette, Diffs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Sessions Menu",
                            tint = colors.text
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onProjectClick() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = projectName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = sessionTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Command Palette button
                    IconButton(
                        onClick = onCommandPaletteClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Commands",
                            tint = colors.text
                        )
                    }

                    // Diff / changes badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderNormal, RoundedCornerShape(6.dp))
                            .clickable { onDiffClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Diff",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (diffCount > 0) colors.primary else colors.textMuted)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$diffCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.backgroundDarker
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Second row: Model & Agent Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Model pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.background)
                        .border(1.dp, colors.borderDim, RoundedCornerShape(6.dp))
                        .clickable { onModelClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.secondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedModel?.name ?: "Select Model",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.text
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Agent pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.background)
                        .border(1.dp, colors.borderDim, RoundedCornerShape(6.dp))
                        .clickable { onAgentClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = agentName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.accent
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(
    code: String,
    language: String = "",
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.codeBlockBg)
            .border(1.dp, colors.borderNormal, RoundedCornerShape(8.dp))
    ) {
        // Code block header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.backgroundSecondary)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = colors.textMuted
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(code))
                        copied = true
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (copied) colors.success else colors.textMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (copied) "Copied" else "Copy",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (copied) colors.success else colors.textMuted
                )
            }
        }

        // Code content with horizontal scrolling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            Text(
                text = code,
                style = MonospaceCodeStyle,
                color = colors.text
            )
        }
    }
}

@Composable
fun ToolExecutionCardView(
    tool: ToolExecution,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderDim, RoundedCornerShape(8.dp))
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tool.toolName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = colors.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tool.summary,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                when (tool.status) {
                    ToolStatus.PENDING -> {
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = colors.textMuted
                        )
                    }
                    ToolStatus.RUNNING -> {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp),
                            color = colors.secondary
                        )
                    }
                    ToolStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = colors.success,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    ToolStatus.FAILED -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failed",
                            tint = colors.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    ToolStatus.REQUIRES_PERMISSION -> {
                        Text(
                            text = "Needs Permission",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.warning
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.backgroundDarker)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Arguments:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = colors.textMuted
                )
                Text(
                    text = tool.arguments,
                    style = MonospaceCodeStyle.copy(fontSize = 11.sp),
                    color = colors.text
                )

                if (!tool.output.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Output:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = colors.textMuted
                    )
                    Text(
                        text = tool.output,
                        style = MonospaceCodeStyle.copy(fontSize = 11.sp),
                        color = colors.text
                    )
                }
            }
        }
    }
}
