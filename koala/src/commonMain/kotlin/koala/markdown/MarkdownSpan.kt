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
    val altText: String,
    val url: String,
    val maxWidthPercent: Int?,
    val type: ImageType,
) : MarkdownSpan

data class MarkdownLink(
    val spans: List<MarkdownSpan>,
    val url: String
): MarkdownSpan