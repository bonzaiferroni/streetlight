package koala.dom

import koala.css.addModifiers
import koala.markdown.MarkdownEmphasis
import koala.markdown.MarkdownInlineCode
import koala.markdown.MarkdownInlineImage
import koala.markdown.MarkdownLink
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownText
import koala.markdown.renderMarkdownSpans
import koala.model.MarkdownEditorStyle
import kotlinx.html.a
import kotlinx.html.code
import kotlinx.html.em
import kotlinx.html.span
import kotlinx.html.strong


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