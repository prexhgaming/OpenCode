package com.opencode.ai.core.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.ai.core.design.MonospaceCodeStyle
import com.opencode.ai.core.design.OpenCodeTheme
import com.opencode.ai.domain.model.DiffHunk
import com.opencode.ai.domain.model.DiffLine
import com.opencode.ai.domain.model.DiffLineType
import com.opencode.ai.domain.model.FileChange

@Composable
fun DiffViewerComponent(
    fileChanges: List<FileChange>,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    val expandedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            fileChanges.forEach { this[it.path] = true }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(fileChanges) { fileChange ->
            val isExpanded = expandedStates[fileChange.path] ?: false

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.backgroundSecondary)
                    .border(1.dp, colors.borderNormal, RoundedCornerShape(8.dp))
            ) {
                // File Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedStates[fileChange.path] = !isExpanded }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = fileChange.path,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = colors.text
                        )
                    }

                    // Additions / Deletions pills
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (fileChange.additions > 0) {
                            Text(
                                text = "+${fileChange.additions}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = colors.success
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        if (fileChange.deletions > 0) {
                            Text(
                                text = "-${fileChange.deletions}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = colors.error
                            )
                        }
                    }
                }

                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.codeBlockBg)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        fileChange.hunks.forEach { hunk ->
                            HunkView(hunk = hunk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HunkView(hunk: DiffHunk) {
    val colors = OpenCodeTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        // Hunk Header (e.g. @@ -7,9 +7,37 @@)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = hunk.header,
                style = MonospaceCodeStyle.copy(
                    fontSize = 10.5.sp,
                    color = colors.textMuted
                )
            )
        }

        // Hunk Lines
        hunk.lines.forEach { line ->
            DiffLineRow(line = line)
        }
    }
}

@Composable
fun DiffLineRow(line: DiffLine) {
    val colors = OpenCodeTheme.colors

    val (bg, textColor, prefix) = when (line.type) {
        DiffLineType.ADDED -> Triple(colors.diffAddedBg, colors.diffAdded, "+")
        DiffLineType.DELETED -> Triple(colors.diffRemovedBg, colors.diffRemoved, "-")
        DiffLineType.CONTEXT -> Triple(colors.codeBlockBg, colors.text, " ")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Line number (Old)
        Text(
            text = (line.oldLineNumber?.toString() ?: "").padStart(4),
            style = MonospaceCodeStyle.copy(
                fontSize = 10.sp,
                color = colors.diffLineNumber
            ),
            modifier = Modifier.width(32.dp)
        )

        // Line number (New)
        Text(
            text = (line.newLineNumber?.toString() ?: "").padStart(4),
            style = MonospaceCodeStyle.copy(
                fontSize = 10.sp,
                color = colors.diffLineNumber
            ),
            modifier = Modifier.width(32.dp)
        )

        // Prefix (+ / - / space)
        Text(
            text = prefix,
            style = MonospaceCodeStyle.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            modifier = Modifier.width(16.dp)
        )

        // Content
        Text(
            text = line.content,
            style = MonospaceCodeStyle.copy(
                fontSize = 11.sp,
                color = textColor
            )
        )
    }
}
