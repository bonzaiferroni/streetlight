package koala.html

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.utils.takeEllipsis
import koala.css.*
import koala.markdown.MarkdownBlock
import koala.markdown.MarkdownStyle
import koala.markdown.markdownBlocksOf
import koala.markdown.renderBlocks
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.markdown(
    text: Markdown,
    modifiers: ModifierSet? = null,
    limit: Int? = null,
    block: DIV.() -> Unit = {}
) = markdown(
    markdownBlocksOf(
        when (limit) {
            null -> text
            // td: better markdown limit
            else -> text.value.takeEllipsis(limit).toMarkdown()
        }
    ), modifiers, block
)

fun DIV.configureMarkdown(
    blocks: List<MarkdownBlock>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    addModifiers(modify(MarkdownStyle.Container, Prose), modifiers)
    block()
    renderBlocks(blocks)
}

fun FlowContent.markdown(
    blocks: List<MarkdownBlock>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    div {
        configureMarkdown(blocks, modifiers, block)
    }
}

fun String.stripMarkdown(maxLength: Int = Int.MAX_VALUE): String {
    val sb = StringBuilder(minOf(length, maxLength + 16))
    var i = 0
    val len = length

    fun atLineStart(): Boolean = i == 0 || this[i - 1] == '\n'
    fun peek(offset: Int): Char? = getOrNull(i + offset)

    fun append(c: Char) {
        if (sb.length < maxLength) sb.append(c)
    }

    fun append(src: CharSequence) {
        if (sb.length < maxLength) sb.append(src)
    }

    fun append(src: CharSequence, start: Int, end: Int) {
        val remaining = maxLength - sb.length
        if (remaining <= 0) return
        sb.append(src, start, minOf(end, start + remaining))
    }

    while (i < len && sb.length < maxLength) {
        val c = this[i]

        if (atLineStart()) {
            if (c == '#') {
                var h = 0
                while (i + h < len && this[i + h] == '#' && h < 6) h++
                if (i + h < len && this[i + h] == ' ') {
                    i += h + 1
                    continue
                }
            }

            if (c == '>') {
                i++
                if (peek(0) == ' ') i++
                continue
            }

            if (c == '-') {
                var j = i
                while (j < len && this[j] == '-') j++
                if (j - i >= 3 && (j >= len || this[j] == '\n')) {
                    i = j
                    continue
                }
            }

            if (c == '|') {
                while (i < len && this[i] == '|') {
                    while (i < len && this[i] != '\n') i++
                    if (i < len) i++
                }
                append("[table]\n")
                continue
            }

            if (c in "*-+" && peek(1) == ' ') {
                i += 2
                continue
            }

            if (c.isDigit()) {
                var j = i
                while (j < len && this[j].isDigit()) j++
                if (j < len && this[j] == '.' && getOrNull(j + 1) == ' ') {
                    i = j + 2
                    continue
                }
            }
        }

        if (c == '!' && peek(1) == '[') {
            val close = indexOf(']', i + 2)
            if (close != -1 && getOrNull(close + 1) == '(') {
                val end = indexOf(')', close + 2)
                if (end != -1) {
                    i = end + 1; continue
                }
            }
        }

        if (c == '[') {
            val close = indexOf(']', i + 1)
            if (close != -1 && getOrNull(close + 1) == '(') {
                val end = indexOf(')', close + 2)
                if (end != -1) {
                    append(this, i + 1, close)
                    i = end + 1
                    continue
                }
            }
        }

        if (c == '*' || c == '_') {
            var count = 0
            while (i + count < len && this[i + count] == c) count++
            if (count in 1..3) {
                val marker = substring(i, i + count)
                val closeIdx = indexOf(marker, i + count)
                if (closeIdx != -1) {
                    append(this, i + count, closeIdx)
                    i = closeIdx + count
                    continue
                }
            }
            i += count
            continue
        }

        if (c == '`') {
            var ticks = 0
            while (i + ticks < len && this[i + ticks] == '`') ticks++
            val closeIdx = indexOf("`".repeat(ticks), i + ticks)
            if (closeIdx != -1) {
                append(this, i + ticks, closeIdx)
                i = closeIdx + ticks
                continue
            }
            i += ticks
            continue
        }

        append(c)
        i++
    }

    return sb.toString().trim()
}