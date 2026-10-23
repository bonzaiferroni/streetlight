@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")

package koala.markdown

import koala.modifier.*
import koala.Svg
import koala.component.LottieClass
import koala.component.icon
import kotlinx.html.*

/** Renders [spans] as inline HTML. Inline images are left out, as [renderFirstInlineImage] places them. */
fun FlowOrPhrasingContent.renderMarkdownSpans(spans: List<MarkdownSpan>) {
    spans.forEach { span ->
        when (span) {
            is MarkdownInlineCode -> renderInlineCode(span)
            is MarkdownInlineImage -> { } // renderInlineImage(span)
            is MarkdownEmphasis -> renderEmphasis(span)
            is MarkdownLink -> renderLink(span)
            is MarkdownStrong -> renderStrong(span)
            is MarkdownStrikethrough -> renderStrikethrough(span)
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

/** Renders [span] as a link when its url is safe to follow, and as plain text otherwise. */
fun FlowOrPhrasingContent.renderLink(span: MarkdownLink) {
    if (!isSafeMarkdownUrl(span.url.value)) {
        +span.text
        return
    }
    a {
        href = span.url.value
        +span.text
    }
}

fun FlowOrPhrasingContent.renderStrong(span: MarkdownStrong) {
    strong {
        +span.text
    }
}

fun FlowOrPhrasingContent.renderStrikethrough(span: MarkdownStrikethrough) {
    s {
        +span.text
    }
}

fun FlowOrPhrasingContent.renderText(span: MarkdownText) {
    +span.text
}

/** Renders the first inline image among [spans], floated to the right. */
fun FlowOrPhrasingContent.renderFirstInlineImage(spans: List<MarkdownSpan>) {
    spans.firstNotNullOfOrNull { it as? MarkdownInlineImage }?.let {
        renderInlineImage(it)
    }
}

/** Renders [span] floated to the right, captioned with its alt text. */
fun FlowOrPhrasingContent.renderInlineImage(span: MarkdownInlineImage) {
    span {
        addModifiers(MarkdownStyle.InlineImage, FloatRight, MarginLeft(2), MarginBottom(2))
        span.width?.let { addModifiers(MaxWidth(it)) }
        when (span.type) {
            ImageType.Lottie -> renderLottieImage(span)
            ImageType.Image -> renderBasicImage(span)
            ImageType.Svg -> renderSvgImage(span)
        }
        span {
            addModifiers(MarkdownStyle.InlineImageCaption)
            +span.altText
        }
    }
}

/** Renders [span] as an image. */
fun FlowOrPhrasingContent.renderBasicImage(span: MarkdownImage) {
    img {
        addModifiers(BorderRadius1, MoonShadow)
        src = span.url.value
        alt = span.altText
    }
}

/** Renders [span] as a Lottie animation, only when it names one of the site's own. */
fun FlowContent.renderLottieImage(span: MarkdownImage) {
    if (!isSiteLottiePath(span.url.value)) return
    div {
        addModifiers(LottieClass.Core)
        attributes["data-lottie"] = span.url.value
    }
}

/** Renders [span] as an icon in the current color. */
fun FlowContent.renderSvgImage(span: MarkdownImage) {
    icon(Svg(span.url), Width(span.width ?: 10))
}

/** The `s` tag, for struck-through text. */
open class S(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("s", consumer, initialAttributes, null, inlineTag = true, emptyTag = false), HtmlInlineTag

/** An `s` tag holding what [block] builds. */
@HtmlTagMarker
inline fun FlowOrPhrasingContent.s(classes: String? = null, crossinline block: S.() -> Unit = {}): Unit =
    S(attributesMapOf("class", classes), consumer).visit(block)