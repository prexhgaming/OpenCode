package com.opencode.ai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.ai.core.design.MonospaceCodeStyle
import com.opencode.ai.core.design.OpenCodeTheme
import com.opencode.ai.domain.model.ChatMessage
import com.opencode.ai.domain.model.MessageRole
import com.opencode.ai.domain.model.PermissionRequest
import com.opencode.ai.domain.model.UserQuestion

@Composable
fun MessageBubbleView(
    message: ChatMessage,
    onPermissionResponse: (messageId: String, requestId: String, approved: Boolean) -> Unit,
    onAnswerQuestion: (messageId: String, questionId: String, answers: List<String>, freeText: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    val isUser = message.role == MessageRole.USER
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Message Header: Role badge & timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isUser) colors.secondary else colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = colors.backgroundDarker,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (isUser) "You" else "OpenCode",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = if (isUser) colors.secondary else colors.primary
                )

                if (message.isStreaming) {
                    Spacer(modifier = Modifier.width(6.dp))
                    CircularProgressIndicator(
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(10.dp),
                        color = colors.primary
                    )
                }
            }

            // Copy message action
            Icon(
                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = "Copy message",
                tint = if (copied) colors.success else colors.textMuted,
                modifier = Modifier
                    .size(14.dp)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(message.content))
                        copied = true
                    }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Message Content Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isUser) colors.surface else colors.backgroundSecondary)
                .border(1.dp, if (isUser) colors.borderDim else colors.borderNormal, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                if (message.content.isNotBlank()) {
                    MarkdownContentRenderer(content = message.content)
                }

                // Render any Tool Executions
                if (message.toolExecutions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        message.toolExecutions.forEach { tool ->
                            ToolExecutionCardView(tool = tool)
                        }
                    }
                }

                // Render Permission Request if any
                message.permissionRequest?.let { perm ->
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionRequestCard(
                        request = perm,
                        onResponse = { approved ->
                            onPermissionResponse(message.id, perm.id, approved)
                        }
                    )
                }

                // Render User Question if any
                message.userQuestion?.let { question ->
                    Spacer(modifier = Modifier.height(8.dp))
                    UserQuestionCardView(
                        question = question,
                        onAnswer = { selected, freeText ->
                            onAnswerQuestion(message.id, question.id, selected, freeText)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MarkdownContentRenderer(content: String, modifier: Modifier = Modifier) {
    val colors = OpenCodeTheme.colors
    // Simple markdown code block extractor
    val parts = content.split("```")

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                val firstLineBreak = part.indexOf('\n')
                val language = if (firstLineBreak != -1) part.substring(0, firstLineBreak).trim() else ""
                val code = if (firstLineBreak != -1) part.substring(firstLineBreak + 1) else part
                CodeBlockView(code = code.trimEnd(), language = language)
            } else {
                // Text part
                if (part.isNotBlank()) {
                    Text(
                        text = part.trim(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        ),
                        color = colors.text
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestCard(
    request: PermissionRequest,
    onResponse: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.backgroundDarker)
            .border(1.dp, colors.warning, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Permission Required",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = colors.warning
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = request.description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = colors.text
        )

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surface)
                .padding(6.dp)
        ) {
            Text(
                text = request.commandOrPath,
                style = MonospaceCodeStyle.copy(fontSize = 11.sp),
                color = colors.textEmphasized
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (request.isApproved == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { onResponse(false) },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Reject", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onResponse(true) },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.backgroundDarker
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Approve", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                }
            }
        } else {
            Text(
                text = if (request.isApproved) "✓ Approved by user" else "✕ Rejected by user",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = if (request.isApproved) colors.success else colors.error
            )
        }
    }
}

@Composable
fun UserQuestionCardView(
    question: UserQuestion,
    onAnswer: (List<String>, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OpenCodeTheme.colors
    var selectedOptions by remember { mutableStateOf(question.selectedOptions) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.backgroundDarker)
            .border(1.dp, colors.info, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = colors.info,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Question from Assistant",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = colors.info
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = question.question,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = colors.text
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Options list
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            question.options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surface)
                        .border(
                            1.dp,
                            if (isSelected) colors.primary else colors.borderDim,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = !question.isAnswered) {
                            selectedOptions = if (question.isMultiSelect) {
                                if (isSelected) selectedOptions - option else selectedOptions + option
                            } else {
                                listOf(option)
                            }
                            if (!question.isMultiSelect) {
                                onAnswer(listOf(option), null)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) colors.primary else colors.text
                    )
                }
            }
        }

        if (question.isAnswered) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "✓ Submitted: ${question.selectedOptions.joinToString()}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = colors.success
            )
        }
    }
}
