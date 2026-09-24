package koala.markdown

import kampfire.model.Url

/** A parsed block of markdown. */
sealed interface MarkdownBlock {
    val blockType: ContentBlock
}

/** An image in markdown, written `![alt](url, width)`, with the positions of its alt text and URL in the source. */
interface MarkdownImage: MarkdownUrl {
    val altText: String
    val altTextIndex: Int
    val maxWidthPercent: Int?
    val type: ImageType
}

// Blocks

/** A block made of inline spans. */
interface MarkdownTextBlock {
    val spans: List<MarkdownSpan>
}

// Paragraph

/** A paragraph. */
data class MarkdownParagraph(
    override val spans: List<MarkdownSpan>
): MarkdownBlock, MarkdownTextBlock {
    override val blockType get() = ContentBlock.Paragraph

    companion object {
        val Empty get() = MarkdownParagraph(emptyList())
    }
}

// Heading

/** A heading of [level] 1 to 6. A heading ending in `---` is drawn in a [filigree]. */
data class MarkdownHeading(
    val level: Int,
    val filigree: Boolean,
    override val spans: List<MarkdownSpan>
): MarkdownBlock, MarkdownTextBlock {
    override val blockType get() = ContentBlock.Heading
}

// Horizontal Rule

/** A horizontal rule. */
data object MarkdownHorizontalRule: MarkdownBlock {
    override val blockType get() = ContentBlock.HorizontalRule
}

// Code Block

/** A fenced code block, with the position of its code in the source. */
data class MarkdownCodeBlock(
    val language: String?,
    val code: String,
    val codeIndex: Int,
): MarkdownBlock {
    override val blockType get() = ContentBlock.Code
}

// Blockquote

/** A blockquote. A last line starting with `--` is its [citation]. */
data class MarkdownBlockquote(
    val paragraphs: List<MarkdownParagraph>,
    val citation: String?
): MarkdownBlock {
    override val blockType get() = ContentBlock.BlockQuote
}

// Lists

/** A list of items, each of which may hold a sublist. */
sealed interface MarkdownList: MarkdownBlock {
    val items: List<MarkdownListItem>
}

/** An unordered list. Its [marker] chooses the bullet; `_` draws none. */
data class MarkdownUnorderedList(
    val marker: Char,
    override val items: List<MarkdownListItem>
): MarkdownList {
    override val blockType get() = ContentBlock.UnorderedList

    companion object {
        val Markers = setOf('-', '*', '+', '_')
    }
}

/** An ordered list, numbered from [startNumber]. */
data class MarkdownOrderedList(
    val startNumber: Int,
    override val items: List<MarkdownListItem>
): MarkdownList {
    override val blockType get() = ContentBlock.OrderedList
}

/** An item of a list, with an optional nested list. */
data class MarkdownListItem(
    val spans: List<MarkdownSpan>,
    val sublist: MarkdownList? = null
)

// Image

/** An image on a line of its own, shown as a figure with its alt text as the caption. */
data class MarkdownBlockImage(
    override val altText: String,
    override val altTextIndex: Int,
    override val url: Url,
    override val urlIndex: Int,
    override val maxWidthPercent: Int?,
    override val type: ImageType,
): MarkdownBlock, MarkdownImage {
    override val blockType get() = ContentBlock.Image
}

// Table

/** A table, with its header, column alignments and rows. */
data class MarkdownTable(
    val header: MarkdownTableRow,
    val alignments: List<MarkdownTableAlignment>,
    val rows: List<MarkdownTableRow>
) : MarkdownBlock {
    override val blockType get() = ContentBlock.Table
}

/** The alignment of a table column. */
enum class MarkdownTableAlignment { Left, Center, Right, None }

data class MarkdownTableCell(val spans: List<MarkdownSpan>)

data class MarkdownTableRow(val cells: List<MarkdownTableCell>)