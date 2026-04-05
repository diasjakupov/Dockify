package io.diasjakupov.dockify.features.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders markdown-formatted text with support for:
 * - **bold**, *italic*, ***bold italic***
 * - `inline code`
 * - Headers (# ## ###)
 * - Bullet lists (- item, * item)
 * - Numbered lists (1. item)
 * - Code blocks (``` ... ```)
 */
@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val codeBackground = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val codeColor = MaterialTheme.colorScheme.onSurface

    val blocks = remember(text) { parseBlocks(text) }

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            if (index > 0 && block !is Block.Empty) {
                Spacer(Modifier.height(4.dp))
            }
            when (block) {
                is Block.Header -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.titleMedium
                        2 -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.labelLarge
                    }
                    Text(
                        text = parseInline(block.content, color, codeBackground, codeColor),
                        style = style,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                is Block.ListItem -> {
                    Row {
                        Text(
                            text = block.bullet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = color
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = parseInline(block.content, color, codeBackground, codeColor),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
                is Block.CodeBlock -> {
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = codeColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(codeBackground)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
                is Block.Paragraph -> {
                    Text(
                        text = parseInline(block.content, color, codeBackground, codeColor),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
                is Block.Empty -> {}
            }
        }
    }
}

private sealed interface Block {
    data class Header(val level: Int, val content: String) : Block
    data class ListItem(val bullet: String, val content: String) : Block
    data class CodeBlock(val content: String) : Block
    data class Paragraph(val content: String) : Block
    data object Empty : Block
}

private fun parseBlocks(text: String): List<Block> {
    val lines = text.lines()
    val blocks = mutableListOf<Block>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        when {
            // Code block
            trimmed.startsWith("```") -> {
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(Block.CodeBlock(codeLines.joinToString("\n")))
                i++ // skip closing ```
            }
            // Header
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(3)
                val content = trimmed.drop(level).trimStart()
                blocks.add(Block.Header(level, content))
                i++
            }
            // Bullet list
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                blocks.add(Block.ListItem("\u2022", trimmed.drop(2)))
                i++
            }
            // Numbered list
            trimmed.matches(Regex("""^\d+\.\s.*""")) -> {
                val num = trimmed.substringBefore(".")
                val content = trimmed.substringAfter(". ", "")
                blocks.add(Block.ListItem("$num.", content))
                i++
            }
            // Empty line
            trimmed.isEmpty() -> {
                blocks.add(Block.Empty)
                i++
            }
            // Regular paragraph
            else -> {
                val paraLines = mutableListOf(line)
                i++
                while (i < lines.size) {
                    val next = lines[i].trim()
                    if (next.isEmpty() || next.startsWith("#") || next.startsWith("- ") ||
                        next.startsWith("* ") || next.startsWith("```") ||
                        next.matches(Regex("""^\d+\.\s.*"""))
                    ) break
                    paraLines.add(lines[i])
                    i++
                }
                blocks.add(Block.Paragraph(paraLines.joinToString(" ")))
            }
        }
    }
    return blocks
}

private fun parseInline(
    text: String,
    defaultColor: Color,
    codeBackground: Color,
    codeColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length

        while (i < len) {
            when {
                // Bold + Italic ***text***
                i + 2 < len && text[i] == '*' && text[i + 1] == '*' && text[i + 2] == '*' -> {
                    val end = text.indexOf("***", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = defaultColor)) {
                            append(text.substring(i + 3, end))
                        }
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Bold **text**
                i + 1 < len && text[i] == '*' && text[i + 1] == '*' -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Italic *text*
                text[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline code `text`
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBackground,
                            color = codeColor,
                            fontSize = 13.sp
                        )) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = defaultColor)) {
                        append(text[i])
                    }
                    i++
                }
            }
        }
    }
}
