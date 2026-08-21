package koala.dom

import kampfire.model.Url
import koala.css.Modifier
import koala.css.modify
import koala.markdown.MarkdownBlockImage
import koala.markdown.MarkdownBlockquote
import koala.markdown.MarkdownCodeBlock
import koala.markdown.MarkdownEmphasis
import koala.markdown.MarkdownHeading
import koala.markdown.MarkdownHorizontalRule
import koala.markdown.MarkdownInlineCode
import koala.markdown.MarkdownInlineImage
import koala.markdown.MarkdownLink
import koala.markdown.MarkdownOrderedList
import koala.markdown.MarkdownParagraph
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownTable
import koala.markdown.MarkdownText
import koala.markdown.MarkdownUnorderedList
import koala.markdown.ParsedBlock
import koala.model.MarkdownEditorStyle
import kotlinx.html.a
import kotlinx.html.br
import kotlinx.html.span

fun AppendScope.renderEditorBlock(block: ParsedBlock) {
    when (val markdown = block.markdown) {
        is MarkdownBlockImage -> renderChunk(block.chunk)
        is MarkdownBlockquote -> renderBlockquote(block.chunk, block.markdown)
        is MarkdownCodeBlock -> renderChunk(block.chunk).also { println(block.chunk) }
        is MarkdownHeading -> renderChunk(block.chunk)
        MarkdownHorizontalRule -> renderChunk(block.chunk)
        is MarkdownOrderedList -> renderChunk(block.chunk)
        is MarkdownUnorderedList -> renderChunk(block.chunk)
        is MarkdownParagraph -> renderParagraph(block.chunk, markdown)
        is MarkdownTable -> renderChunk(block.chunk)
    }
}

private fun AppendScope.renderChunk(chunk: String) {
    span {
        +chunk
    }
    if (chunk.endsWith("\n")) {
        br { }
    }
}

private fun AppendScope.renderParagraph(chunk: String, block: MarkdownParagraph) {
    if (block.spans.isEmpty()) {
        br { }
    } else {
        renderSpans(chunk, 0, block.spans)
    }
}

private fun AppendScope.renderBlockquote(chunk: String, block: MarkdownBlockquote) {
    var index = 0
    block.paragraphs.forEach { paragraph ->
        index = renderSpans(chunk, index, paragraph.spans)
        index = renderTrailingExtra(chunk, index) + 1
    }
}

private fun AppendScope.renderSpans(chunk: String, startIndex: Int, spans: List<MarkdownSpan>): Int {
    var index = startIndex
    spans.forEach { span ->
        index = renderLeadingExtra(chunk, index, span)
        index = renderSpan(chunk, index, span)
    }
    return index
}

private fun AppendScope.renderSpan(chunk: String, startIndex: Int, span: MarkdownSpan) = when (span) {
    is MarkdownEmphasis, is MarkdownStrong, is MarkdownText, is MarkdownInlineCode ->
        renderSpanContent(chunk, startIndex, span)
    is MarkdownInlineImage -> {
        var index = renderSpanContent(chunk, startIndex, span)
        index = renderExtra(chunk, index, span.urlIndex)
        renderLink(index, span.url)
    }
    is MarkdownLink -> {
        var index = renderSpanContent(chunk, startIndex, span)
        index = renderExtra(chunk, index, span.urlIndex)
        renderLink(index, span.url)
    }
}

private fun AppendScope.renderSpanContent(
    chunk: String,
    startIndex: Int,
    span: MarkdownSpan,
    mod: Modifier? = span.contentMod
) = renderSpan(chunk, startIndex, startIndex + span.text.length, mod)

private fun AppendScope.renderLink(startIndex: Int, url: Url): Int {
    // do we use the url or a substring from chunk?
    a {
        // td: make clickable with shift or something
        // href = url.value
        +url.value
    }
    return startIndex + url.value.length
}

private fun AppendScope.renderExtra(chunk: String, startIndex: Int, endIndex: Int) =
    renderSpan(chunk, startIndex, endIndex, MarkdownEditorStyle.Extra)

private fun AppendScope.renderLeadingExtra(chunk: String, startIndex: Int, span: MarkdownSpan) =
    renderExtra(chunk, startIndex, span.index)

private fun AppendScope.renderTrailingExtra(chunk: String, startIndex: Int): Int {
    val endIndex = chunk.indexOf("\n", startIndex).takeIf { it >= 0 } ?: chunk.length
    return renderExtra(chunk, startIndex, endIndex)
}

private fun AppendScope.renderSpan(chunk: String, startIndex: Int, endIndex: Int, mod: Modifier?): Int {
    if (startIndex == endIndex) return endIndex
    span(chunk.substring(startIndex, endIndex), modify(mod))
    return endIndex
}

private val MarkdownSpan.contentMod get() = when (this) {
    is MarkdownStrong -> MarkdownEditorStyle.Strong
    is MarkdownEmphasis -> MarkdownEditorStyle.Emphasis
    is MarkdownInlineCode -> MarkdownEditorStyle.InlineCode
    is MarkdownInlineImage, is MarkdownLink, is MarkdownText -> null
}

