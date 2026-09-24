package koala.markdown

import kampfire.model.Url

/** A run of inline markdown, with its text and its position in the source. */
sealed interface MarkdownSpan {
    val text: String
    val index: Int
}

/** A span that holds a URL, with its position in the source. */
interface MarkdownUrl {
    val url: Url
    val urlIndex: Int
}

/** Plain text. */
data class MarkdownText(
    override val text: String,
    override val index: Int,
): MarkdownSpan

/** Emphasized text, written `*text*`. */
data class MarkdownEmphasis(
    override val text: String,
    override val index: Int,
): MarkdownSpan

/** Strong text, written `**text**`. */
data class MarkdownStrong(
    override val text: String,
    override val index: Int,
): MarkdownSpan

/** Code, written between backticks. */
data class MarkdownInlineCode(
    override val text: String,
    override val index: Int,
): MarkdownSpan

/** Struck-through text, written `~~text~~`. */
data class MarkdownStrikethrough(
    override val text: String,
    override val index: Int,
): MarkdownSpan

/** An image within a paragraph. The first in a paragraph floats beside it. */
data class MarkdownInlineImage(
    override val altText: String,
    override val altTextIndex: Int,
    override val url: Url,
    override val urlIndex: Int,
    override val maxWidthPercent: Int?,
    override val type: ImageType,
) : MarkdownSpan, MarkdownImage {
    override val text get() = altText
    override val index get() = altTextIndex
}

/** A link, written `[text](url)`. */
data class MarkdownLink(
    override val text: String,
    override val index: Int,
    override val url: Url,
    override val urlIndex: Int,
): MarkdownSpan, MarkdownUrl