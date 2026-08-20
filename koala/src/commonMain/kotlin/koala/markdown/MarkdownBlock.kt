package koala.markdown

sealed interface MarkdownBlock {
    val blockType: MarkdownBlockType
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
): MarkdownBlock, MarkdownTextBlock {
    override val blockType get() = MarkdownBlockType.Paragraph

    companion object {
        val Empty get() = MarkdownParagraph(emptyList())
    }
}

// Heading

data class MarkdownHeading(
    val level: Int,
    val filigree: Boolean,
    override val spans: List<MarkdownSpan>
): MarkdownBlock, MarkdownTextBlock {
    override val blockType get() = MarkdownBlockType.Heading
}

// Horizontal Rule

data object MarkdownHorizontalRule: MarkdownBlock {
    override val blockType get() = MarkdownBlockType.HorizontalRule
}

// Code Block

data class MarkdownCodeBlock(
    val language: String?,
    val code: String
): MarkdownBlock {
    override val blockType get() = MarkdownBlockType.Code
}

// Blockquote

data class MarkdownBlockquote(
    val paragraphs: List<MarkdownParagraph>
): MarkdownBlock {
    override val blockType get() = MarkdownBlockType.BlockQuote
}

// Lists

sealed interface MarkdownList: MarkdownBlock {
    val items: List<MarkdownListItem>
}

data class MarkdownUnorderedList(
    val marker: Char,
    override val items: List<MarkdownListItem>
): MarkdownList {
    override val blockType get() = MarkdownBlockType.UnorderedList
}

data class MarkdownOrderedList(
    val startNumber: Int,
    override val items: List<MarkdownListItem>
): MarkdownList {
    override val blockType get() = MarkdownBlockType.OrderedList
}

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
): MarkdownBlock, MarkdownImage {
    override val blockType get() = MarkdownBlockType.Image
}

data class MarkdownTable(
    val header: MarkdownTableRow,
    val alignments: List<MarkdownTableAlignment>,
    val rows: List<MarkdownTableRow>
) : MarkdownBlock {
    override val blockType get() = MarkdownBlockType.Table
}