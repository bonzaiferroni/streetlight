package koala.markdown

sealed interface MarkdownSpan {
}

data class MarkdownText(
    val text: String
): MarkdownSpan

data class MarkdownEmphasis(
    val text: String
): MarkdownSpan

data class MarkdownStrong(
    val text: String
): MarkdownSpan

data class MarkdownInlineCode(
    val text: String
): MarkdownSpan

data class MarkdownInlineImage(
    override val altText: String,
    override val url: String,
    override val maxWidthPercent: Int?,
    override val type: ImageType,
) : MarkdownSpan, MarkdownImage

data class MarkdownLink(
    val spans: List<MarkdownSpan>,
    val url: String
): MarkdownSpan