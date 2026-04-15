package koala.markdown

import koala.html.navigation
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.PhrasingContent
import kotlinx.html.a
import kotlinx.html.code
import kotlinx.html.em
import kotlinx.html.strong

fun FlowOrPhrasingContent.renderSpans(spans: List<MarkdownSpan>) {
    spans.forEach { span ->
        when (span) {
            is MarkdownInlineCode -> renderInlineCode(span)
            is MarkdownEmphasis -> renderEmphasis(span)
            is MarkdownLink -> renderLink(span)
            is MarkdownStrong -> renderStrong(span)
            is MarkdownText -> renderText(span)
        }
    }
}

fun FlowOrPhrasingContent.renderInlineCode(span: MarkdownInlineCode) {
    code {
        +span.text
    }
}

fun FlowOrPhrasingContent.renderEmphasis(span: MarkdownEmphasis) {
    em {
        +span.text
    }
}

fun FlowOrPhrasingContent.renderLink(span: MarkdownLink) {
    a {
        href = span.url
        renderSpans(span.spans)
    }
}

fun FlowOrPhrasingContent.renderStrong(span: MarkdownStrong) {
    strong {
        +span.text
    }
}

fun FlowOrPhrasingContent.renderText(span: MarkdownText) {
    +span.text
}