package koala.markdown

import koala.html.navigation
import kotlinx.html.FlowContent
import kotlinx.html.PhrasingContent
import kotlinx.html.a
import kotlinx.html.code
import kotlinx.html.em
import kotlinx.html.strong

fun PhrasingContent.renderSpans(spans: List<MarkdownSpan>) {
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

fun PhrasingContent.renderInlineCode(span: MarkdownInlineCode) {
    code {
        +span.text
    }
}

fun PhrasingContent.renderEmphasis(span: MarkdownEmphasis) {
    em {
        +span.text
    }
}

fun PhrasingContent.renderLink(span: MarkdownLink) {
    a {
        href = span.url
        renderSpans(span.spans)
    }
}

fun PhrasingContent.renderStrong(span: MarkdownStrong) {
    strong {
        +span.text
    }
}

fun PhrasingContent.renderText(span: MarkdownText) {
    +span.text
}