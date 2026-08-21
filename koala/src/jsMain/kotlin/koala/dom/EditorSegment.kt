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
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownTable
import koala.markdown.MarkdownText
import koala.markdown.MarkdownUrl
import koala.markdown.ParsedBlock
import koala.model.EditorStyle
import kotlin.sequences.forEach
import kotlin.sequences.plus

class EditorSegment(val from: Int, val to: Int, val mod: Modifier)

fun ParsedBlock.editorSegments(): List<EditorSegment> = buildList {
    var index = 0

    markdown.editorSpans().forEach { span ->
        if (span.index > index) {
            add(EditorSegment(index, span.index, EditorStyle.Extra))
        }
        index = span.index

        val contentEnd = index + span.contentLength
        if (contentEnd > index) {
            add(EditorSegment(index, contentEnd, span.contentMod))
        }
        index = contentEnd

        val urlLength = span.urlLength
        if (urlLength > 0) {
            val urlStart = (span as? MarkdownUrl)?.urlIndex ?: -1
            if (urlStart > index) {
                add(EditorSegment(index, urlStart, EditorStyle.Extra))
            }
            add(EditorSegment(urlStart, urlStart + urlLength, EditorStyle.Url))
            index = urlStart + urlLength
        }
    }

    if (chunk.length > index) {
        add(EditorSegment(index, chunk.length, EditorStyle.Extra))
    }
}

private fun MarkdownBlock.editorSpans(): Sequence<MarkdownSpan> = when (this) {
    is MarkdownParagraph -> spans.asSequence()
    is MarkdownHeading -> spans.asSequence()
    is MarkdownBlockquote -> paragraphs.asSequence().flatMap { it.spans }
    is MarkdownList -> items.asSequence().flatMap { it.editorSpans() }
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

private val MarkdownSpan.contentLength get() = when (this) {
    is MarkdownInlineImage -> altText.length
    is MarkdownText -> text.length
    is MarkdownEmphasis -> text.length
    is MarkdownStrong -> text.length
    is MarkdownInlineCode -> text.length
    is MarkdownLink -> text.length
}

private val MarkdownSpan.urlLength get() = when (this) {
    is MarkdownLink -> url.value.length
    is MarkdownInlineImage -> url.value.length
    else -> 0
}

private val MarkdownSpan.contentMod get() = when (this) {
    is MarkdownStrong -> EditorStyle.Strong
    is MarkdownEmphasis -> EditorStyle.Emphasis
    is MarkdownInlineCode -> EditorStyle.InlineCode
    is MarkdownLink, is MarkdownInlineImage -> EditorStyle.LinkText
    is MarkdownText -> EditorStyle.Text
}