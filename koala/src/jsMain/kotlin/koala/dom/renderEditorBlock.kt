package koala.dom

import koala.css.BorderRadius1
import koala.css.MarginBottom2
import koala.css.MarginLeft2
import koala.css.MoonShadow
import koala.css.addModifiers
import koala.html.LottieClass
import koala.markdown.FloatRight
import koala.markdown.ImageType
import koala.markdown.MarkdownBlockImage
import koala.markdown.MarkdownBlockquote
import koala.markdown.MarkdownCodeBlock
import koala.markdown.MarkdownEmphasis
import koala.markdown.MarkdownHeading
import koala.markdown.MarkdownHorizontalRule
import koala.markdown.MarkdownImage
import koala.markdown.MarkdownInlineCode
import koala.markdown.MarkdownInlineImage
import koala.markdown.MarkdownLink
import koala.markdown.MarkdownOrderedList
import koala.markdown.MarkdownParagraph
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownStyle
import koala.markdown.MarkdownTable
import koala.markdown.MarkdownText
import koala.markdown.MarkdownUnorderedList
import koala.markdown.ParsedBlock
import koala.markdown.renderEmphasis
import koala.markdown.renderInlineCode
import koala.markdown.renderInlineImage
import koala.markdown.renderLink
import koala.markdown.renderMarkdownSpans
import koala.markdown.renderStrong
import koala.markdown.renderText
import koala.model.MarkdownEditorStyle
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.a
import kotlinx.html.br
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.em
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.style
import kotlin.collections.set

fun AppendScope.renderEditorBlock(block: ParsedBlock) {
    when (val markdown = block.markdown) {
        is MarkdownBlockImage -> renderChunk(block.chunk)
        is MarkdownBlockquote -> renderBlockquote(markdown)
        is MarkdownCodeBlock -> renderChunk(block.chunk).also { println(block.chunk) }
        is MarkdownHeading -> renderChunk(block.chunk)
        MarkdownHorizontalRule -> renderChunk(block.chunk)
        is MarkdownOrderedList -> renderChunk(block.chunk)
        is MarkdownUnorderedList -> renderChunk(block.chunk)
        is MarkdownParagraph -> renderParagraph(markdown)
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

private fun AppendScope.renderParagraph(block: MarkdownParagraph) {
    if (block.spans.isEmpty()) {
        br { }
    } else {
        renderEditorSpans(block.spans)
    }
}

fun AppendScope.renderBlockquote(block: MarkdownBlockquote) {
    block.paragraphs.forEachIndexed { index, paragraph ->
        if (index > 0) renderChunk("\n")
        renderSyntax(">")
        renderParagraph(paragraph)
    }
}