package com.opencode.ai.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.ai.core.design.OpenCodeTheme

@Composable
fun MessageComposer(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    isStreaming: Boolean,
    onCommandClick: () -> Unit,
    onAttachClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors

    Surface(
        color = colors.backgroundSecondary,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.borderNormal)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Quick action chips (e.g. /init, /compact, /diff)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickCommandChip(label = "/init", onClick = { onTextChanged("/init ") })
                QuickCommandChip(label = "/compact", onClick = { onTextChanged("/compact") })
                QuickCommandChip(label = "/diff", onClick = { onTextChanged("/diff") })
                QuickCommandChip(label = "@file", onClick = onAttachClick)
            }

            // Input box row
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background)
                    .border(1.dp, if (text.isNotBlank()) colors.borderFocused else colors.borderDim, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 24.dp, max = 120.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Ask OpenCode or type / for commands...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = colors.textMuted
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        textStyle = TextStyle(
                            color = colors.text,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isStreaming) {
                    // Stop button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.error)
                            .clickable { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop generation",
                            tint = colors.backgroundDarker,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Send button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (text.isNotBlank()) colors.primary else colors.borderDim)
                            .clickable(enabled = text.isNotBlank()) { onSend() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Send message",
                            tint = if (text.isNotBlank()) colors.backgroundDarker else colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickCommandChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderDim, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = colors.textMuted
        )
    }
}
