package koala.markdown

sealed interface MarkdownBlock {
}

interface MarkdownImage {
    val altText: String
    val url: String
    val maxWidthPercent: Int?
    val type: ImageType
}

// Blocks

interface MarkdownTextBlock {
    val spans: List<MarkdownSpan>
}

// Paragraph

data class MarkdownParagraph(
    override val spans: List<MarkdownSpan>
): MarkdownBlock, MarkdownTextBlock

// Heading

data class MarkdownHeading(
    val level: Int,
    val filigree: Boolean,
    override val spans: List<MarkdownSpan>
): MarkdownBlock, MarkdownTextBlock

// Horizontal Rule

data object MarkdownHorizontalRule: MarkdownBlock

// Code Block

data class MarkdownCodeBlock(
    val language: String?,
    val code: String
): MarkdownBlock

// Blockquote

data class MarkdownBlockquote(
    val paragraphs: List<MarkdownParagraph>
): MarkdownBlock

// Lists

sealed interface MarkdownList: MarkdownBlock {
    val items: List<MarkdownListItem>
}

data class MarkdownUnorderedList(
    val marker: Char,
    override val items: List<MarkdownListItem>
): MarkdownList

data class MarkdownOrderedList(
    val startNumber: Int,
    override val items: List<MarkdownListItem>
): MarkdownList

data class MarkdownListItem(
    val spans: List<MarkdownSpan>,
    val sublist: MarkdownList? = null
)

// Image

data class MarkdownBlockImage(
    override val altText: String,
    override val url: String,
    override val maxWidthPercent: Int?,
    override val type: ImageType,
): MarkdownBlock, MarkdownImage
