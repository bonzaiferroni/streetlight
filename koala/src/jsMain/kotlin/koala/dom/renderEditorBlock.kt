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
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.em
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.style
import kotlin.collections.set

fun AppendScope.renderEditorBlock(block: ParsedBlock) {
    when (val markdown = block.markdown) {
        is MarkdownBlockImage -> renderChunk(block.chunk)
        is MarkdownBlockquote -> renderChunk(block.chunk)
        is MarkdownCodeBlock -> renderChunk(block.chunk)
        is MarkdownHeading -> renderChunk(block.chunk)
        MarkdownHorizontalRule -> renderChunk(block.chunk)
        is MarkdownOrderedList -> renderChunk(block.chunk)
        is MarkdownUnorderedList -> renderChunk(block.chunk)
        is MarkdownParagraph -> renderEditorParagraph(markdown)
        is MarkdownTable -> renderChunk(block.chunk)
        null -> renderChunk(block.chunk)
    }
}

fun AppendScope.renderEditorParagraph(block: MarkdownParagraph) {
    renderEditorSpans(block.spans)
}

fun AppendScope.renderChunk(chunk: String) {
    span {
        +chunk
    }
}

fun AppendScope.renderEditorSpans(spans: List<MarkdownSpan>) {
    spans.forEach { span ->
        when (span) {
            is MarkdownInlineCode -> renderInlineCode(span)
            is MarkdownInlineImage -> renderInlineImage(span)
            is MarkdownEmphasis -> renderEmphasis(span)
            is MarkdownLink -> renderLink(span)
            is MarkdownStrong -> renderStrong(span)
            is MarkdownText -> renderText(span)
        }
    }
}

fun AppendScope.renderSyntax(syntax: String) {
    span {
        addModifiers(MarkdownEditorStyle.Syntax)
        +syntax
    }
}

fun AppendScope.renderInlineCode(span: MarkdownInlineCode) {
    renderSyntax("`")
    code {
        +span.text
    }
    renderSyntax("`")
}

fun AppendScope.renderEmphasis(span: MarkdownEmphasis) {
    renderSyntax("*")
    em {
        +span.text
    }
    renderSyntax("*")
}

fun AppendScope.renderLink(span: MarkdownLink) {
    // td: render input text
    renderSyntax("[")
    renderEditorSpans(span.spans)
    renderSyntax("](")
    a {
        // td: make clickable with ctrl or otherwise
        // href = span.url
        renderMarkdownSpans(span.spans)
    }
    renderSyntax(")")
}

fun AppendScope.renderStrong(span: MarkdownStrong) {
    renderSyntax("**")
    strong {
        +span.text
    }
    renderSyntax("**")
}

fun AppendScope.renderText(span: MarkdownText) {
    span {
        +span.text
    }
}

fun AppendScope.renderInlineImage(span: MarkdownInlineImage) {
    // td: render input text
    renderSyntax("![")
    span {
        +span.altText
    }
    renderSyntax("](")
    span {
        +span.url
    }
    renderSyntax(")")
}