package koala.markdown

import kampfire.model.Url

sealed interface MarkdownSpan {
    val text: String
    val index: Int
}

data class MarkdownText(
    override val text: String,
    override val index: Int,
): MarkdownSpan

data class MarkdownEmphasis(
    override val text: String,
    override val index: Int,
): MarkdownSpan

data class MarkdownStrong(
    override val text: String,
    override val index: Int,
): MarkdownSpan

data class MarkdownInlineCode(
    override val text: String,
    override val index: Int,
): MarkdownSpan

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

data class MarkdownLink(
    override val text: String,
    override val index: Int,
    val url: Url,
    val urlIndex: Int,
): MarkdownSpan