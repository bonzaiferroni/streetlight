package koala.markdown

import koala.css.*
import koala.html.FloatRight
import koala.html.MarkdownClass
import kotlinx.html.*

fun FlowOrPhrasingContent.renderSpans(spans: List<MarkdownSpan>) {
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

fun FlowOrPhrasingContent.renderInlineImage(span: MarkdownInlineImage) {
    span {
        addModifiers(MarkdownClass.InlineImage, FloatRight, MarginLeft1, MarginBottom1)
        img {
            addModifiers(BorderRadius1, MoonShadow)
            src = span.url
            alt = span.altText
        }
        span {
            addModifiers(MarkdownClass.InlineImageCaption)
            +span.altText
        }
    }
}