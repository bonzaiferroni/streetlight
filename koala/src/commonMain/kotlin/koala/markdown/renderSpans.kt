package koala.markdown

import koala.css.*
import koala.html.FloatRight
import koala.html.LottieClass
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
        span.maxWidthPercent?.let {
            style = "max-width: $it%;"
        }
        when (span.type) {
            ImageType.Lottie -> renderLottieImage(span)
            ImageType.Image -> renderBasicImage(span)
        }
        span {
            addModifiers(MarkdownClass.InlineImageCaption)
            +span.altText
        }
    }
}

fun FlowOrPhrasingContent.renderBasicImage(span: MarkdownImage) {
    img {
        addModifiers(BorderRadius1, MoonShadow)
        src = span.url
        alt = span.altText
    }
}

fun FlowContent.renderLottieImage(span: MarkdownImage) {
    div {
        addModifiers(LottieClass.Core)
        attributes["data-lottie"] = span.url
    }
}