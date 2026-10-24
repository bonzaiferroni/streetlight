package streetlight.compose.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import kampfire.api.Markdown
import koala.markdown.MarkdownBlock
import koala.markdown.MarkdownBlockImage
import koala.markdown.MarkdownBlockquote
import koala.markdown.MarkdownCodeBlock
import koala.markdown.MarkdownEmphasis
import koala.markdown.MarkdownHeading
import koala.markdown.MarkdownHorizontalRule
import koala.markdown.MarkdownInlineCode
import koala.markdown.MarkdownInlineImage
import koala.markdown.MarkdownLink
import koala.markdown.MarkdownList
import koala.markdown.MarkdownOrderedList
import koala.markdown.MarkdownParagraph
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownStrikethrough
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownTable
import koala.markdown.MarkdownText
import koala.markdown.MarkdownUnorderedList
import koala.markdown.markdownBlocksOf

/**
 * [markdown] as one run of styled text, for a summary that reads as prose: each block on its own line, its headings at
 * the text's size a step heavier, its links in [linkColor]. Images and rules are left out. The cousin of the web's
 * `renderMarkdownBlocks`, as a feed row's summary styles it.
 */
fun annotatedMarkdownOf(markdown: Markdown, linkColor: Color): AnnotatedString = buildAnnotatedString {
    var isFirst = true
    markdownBlocksOf(markdown).map { it.markdown }.forEach { block ->
        if (block is MarkdownHorizontalRule || block is MarkdownBlockImage) return@forEach
        if (!isFirst) append('\n')
        isFirst = false
        appendBlock(block, linkColor)
    }
}

private fun AnnotatedString.Builder.appendBlock(block: MarkdownBlock, linkColor: Color) {
    when (block) {
        is MarkdownParagraph -> appendSpans(block.spans, linkColor)
        is MarkdownHeading -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { appendSpans(block.spans, linkColor) }
        is MarkdownBlockquote -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            block.paragraphs.forEachIndexed { index, paragraph ->
                if (index > 0) append('\n')
                appendSpans(paragraph.spans, linkColor)
            }
            block.citation?.let { citation -> append("\n— $citation") }
            Unit
        }
        is MarkdownList -> appendList(block, linkColor, depth = 0)
        is MarkdownCodeBlock -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(block.code.trimEnd()) }
        is MarkdownTable -> (listOf(block.header) + block.rows).forEachIndexed { index, row ->
            if (index > 0) append('\n')
            row.cells.forEachIndexed { cellIndex, cell ->
                if (cellIndex > 0) append("  ·  ")
                appendSpans(cell.spans, linkColor)
            }
        }
        is MarkdownHorizontalRule, is MarkdownBlockImage -> Unit
    }
}

/** The items of [list], each on its own line behind its bullet or number, a sublist indented under its item. */
private fun AnnotatedString.Builder.appendList(list: MarkdownList, linkColor: Color, depth: Int) {
    list.items.forEachIndexed { index, item ->
        if (index > 0) append('\n')
        append("    ".repeat(depth))
        when (list) {
            is MarkdownOrderedList -> append("${list.startNumber + index}. ")
            is MarkdownUnorderedList -> if (list.marker != '_') append("• ") else Unit
        }
        appendSpans(item.spans, linkColor)
        item.sublist?.let {
            append('\n')
            appendList(it, linkColor, depth + 1)
        }
    }
}

private fun AnnotatedString.Builder.appendSpans(spans: List<MarkdownSpan>, linkColor: Color) {
    spans.forEach { span ->
        when (span) {
            is MarkdownText -> append(span.text)
            is MarkdownEmphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(span.text) }
            is MarkdownStrong -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(span.text) }
            is MarkdownInlineCode -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(span.text) }
            is MarkdownStrikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(span.text) }
            is MarkdownLink -> withStyle(SpanStyle(color = linkColor)) { append(span.text) }
            is MarkdownInlineImage -> Unit
        }
    }
}
