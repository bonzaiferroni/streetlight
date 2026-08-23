package koala.dom

import koala.css.Modifier
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
import koala.markdown.MarkdownListItem
import koala.markdown.MarkdownParagraph
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownStrikethrough
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownTable
import koala.markdown.MarkdownText
import koala.markdown.MarkdownUrl
import koala.markdown.ParsedBlock
import koala.model.EditorStyle
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.collections.forEach
import kotlin.sequences.forEach
import kotlin.sequences.plus

data class EditorSegment(val from: Int, val to: Int, val mod: Modifier)

fun ParsedBlock.toEditorSegments(): List<EditorSegment>? = markdown.editorSpans()?.let { spans ->
    segmentsIn(chunk, 0, chunk.length, spans)
}

fun segmentsIn(chunk: String, from: Int, to: Int, spans: Sequence<MarkdownSpan>): List<EditorSegment> = buildList {
    var index = from

    spans.forEach { span ->
        addExtra(chunk, index, span.index)
        index = span.index

        val contentEnd = index + span.text.length
        if (contentEnd > index) {
            add(EditorSegment(index, contentEnd, span.contentMod))
        }
        index = contentEnd

        if (span is MarkdownUrl) {
            addExtra(chunk, index, span.urlIndex)
            val urlEnd = span.urlIndex + span.url.value.length
            add(EditorSegment(span.urlIndex, urlEnd, EditorStyle.Url))
            index = urlEnd
        }
    }

    addExtra(chunk, index, to)
}

private fun MutableList<EditorSegment>.addExtra(chunk: String, from: Int, to: Int) {
    var start = from
    while (start < to) {
        val newline = chunk.indexOf('\n', start)
        if (newline == -1 || newline >= to) {
            add(EditorSegment(start, to, EditorStyle.Extra))
            return
        }
        if (newline > start) {
            add(EditorSegment(start, newline, EditorStyle.Extra))
        }
        var end = newline
        while (end < to && chunk[end] == '\n') end++
        add(EditorSegment(newline, end, EditorStyle.Space))
        start = end
    }
}

private fun MarkdownBlock.editorSpans(): Sequence<MarkdownSpan>? = when (this) {
    is MarkdownParagraph -> spans.asSequence()
    is MarkdownHeading -> spans.asSequence()
    is MarkdownBlockquote -> paragraphs.asSequence().flatMap { it.spans }
    is MarkdownList -> items.asSequence().flatMap { it.editorSpans() }
//    is MarkdownTable -> null
    is MarkdownTable -> (sequenceOf(header) + rows).flatMap { row ->
        row.cells.asSequence().flatMap { it.spans }
    }
    is MarkdownCodeBlock -> when {
        code.isEmpty() -> emptySequence()
        else -> sequenceOf(MarkdownInlineCode(code, codeIndex))
    }
    is MarkdownBlockImage -> sequenceOf(
        MarkdownInlineImage(altText, altTextIndex, url, urlIndex, maxWidthPercent, type)
    )
    MarkdownHorizontalRule -> emptySequence()
}

private fun MarkdownListItem.editorSpans(): Sequence<MarkdownSpan> =
    spans.asSequence() + (sublist?.editorSpans() ?: emptySequence())


fun HTMLElement.matchesEditorSegments(chunk: String, segments: List<EditorSegment>?): Boolean {
    if (segments == null) return false

    val nodes = childNodes.asList()
    var nodeIndex = 0

    segments.forEach { segment ->
        val node = nodes.getOrNull(nodeIndex++) as? HTMLElement ?: return false
        if (node.tagName != "SPAN") return false
        if (!node.isModified(segment.mod)) return false

        val text = node.textContent ?: return false
        if (text.length != segment.to - segment.from) return false
        if (!chunk.regionMatches(segment.from, text, 0, text.length)) return false
    }

    if (chunk.isEmpty() || chunk.endsWith("\n")) {
        if (nodes.getOrNull(nodeIndex++) !is HTMLBRElement) return false
    }

    return nodeIndex == nodes.size
}

private val MarkdownSpan.contentMod get() = when (this) {
    is MarkdownStrong -> EditorStyle.Strong
    is MarkdownEmphasis -> EditorStyle.Emphasis
    is MarkdownInlineCode -> EditorStyle.InlineCode
    is MarkdownStrikethrough -> EditorStyle.Strikethrough
    is MarkdownLink, is MarkdownInlineImage -> EditorStyle.LinkText
    is MarkdownText -> EditorStyle.Text
}

