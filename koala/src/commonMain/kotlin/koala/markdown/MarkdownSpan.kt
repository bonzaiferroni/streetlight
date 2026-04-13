package koala.markdown

sealed interface MarkdownSpan {
}

// Text

data class MarkdownText(
    val text: String
): MarkdownSpan

// Emphasis

data class MarkdownEmphasis(
    val text: String
): MarkdownSpan

// Strong

data class MarkdownStrong(
    val text: String
): MarkdownSpan

// Code

data class MarkdownInlineCode(
    val text: String
): MarkdownSpan

// Link

data class MarkdownLink(
    val spans: List<MarkdownSpan>,
    val url: String
): MarkdownSpan