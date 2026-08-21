package koala.dom

import koala.css.addModifiers
import koala.css.modify
import koala.markdown.MarkdownEmphasis
import koala.markdown.MarkdownInlineCode
import koala.markdown.MarkdownInlineImage
import koala.markdown.MarkdownLink
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownText
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

fun AppendScope.renderExtra(/* args */) {
    span("my content", modify(MarkdownEditorStyle.Extra))
}

fun AppendScope.renderExtra(syntax: String) {
    span {
        addModifiers(MarkdownEditorStyle.Extra)
        +syntax
    }
}

fun AppendScope.renderInlineCode(span: MarkdownInlineCode) {
    renderExtra("`")
    code {
        +span.text
    }
    renderExtra("`")
}

fun AppendScope.renderEmphasis(span: MarkdownEmphasis) {
    renderExtra("*")
    em {
        +span.text
    }
    renderExtra("*")
}

fun AppendScope.renderLink(span: MarkdownLink) {
    // td: render input text
    renderExtra("[")
    span {
        +span.text
    }
    renderExtra("](")
    a {
        // td: make clickable with ctrl or otherwise
        // href = span.url
        +span.url.value
    }
    renderExtra(")")
}

fun AppendScope.renderStrong(span: MarkdownStrong) {
    renderExtra("**")
    strong {
        +span.text
    }
    renderExtra("**")
}

fun AppendScope.renderText(span: MarkdownText) {
    span {
        +span.text
    }
}

fun AppendScope.renderInlineImage(span: MarkdownInlineImage) {
    // td: render input text
    renderExtra("![")
    span {
        +span.altText
    }
    renderExtra("](")
    span {
        +span.url.value
    }
    renderExtra(")")
}