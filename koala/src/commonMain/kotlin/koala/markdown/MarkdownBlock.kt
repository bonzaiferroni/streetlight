package koala.markdown

import kampfire.model.Url

sealed interface MarkdownBlock {
    val blockType: ContentBlock
}

interface MarkdownImage: MarkdownUrl {
    val altText: String
    val altTextIndex: Int
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
    override val blockType get() = ContentBlock.Paragraph

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
    override val blockType get() = ContentBlock.Heading
}

// Horizontal Rule

data object MarkdownHorizontalRule: MarkdownBlock {
    override val blockType get() = ContentBlock.HorizontalRule
}

// Code Block

data class MarkdownCodeBlock(
    val language: String?,
    val code: String,
    val codeIndex: Int,
): MarkdownBlock {
    override val blockType get() = ContentBlock.Code
}

// Blockquote

data class MarkdownBlockquote(
    val paragraphs: List<MarkdownParagraph>,
    val citation: String?
): MarkdownBlock {
    override val blockType get() = ContentBlock.BlockQuote
}

// Lists

sealed interface MarkdownList: MarkdownBlock {
    val items: List<MarkdownListItem>
}

data class MarkdownUnorderedList(
    val marker: Char,
    override val items: List<MarkdownListItem>
): MarkdownList {
    override val blockType get() = ContentBlock.UnorderedList
}

data class MarkdownOrderedList(
    val startNumber: Int,
    override val items: List<MarkdownListItem>
): MarkdownList {
    override val blockType get() = ContentBlock.OrderedList
}

data class MarkdownListItem(
    val spans: List<MarkdownSpan>,
    val sublist: MarkdownList? = null
)

// Image

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

data class MarkdownTable(
    val header: MarkdownTableRow,
    val alignments: List<MarkdownTableAlignment>,
    val rows: List<MarkdownTableRow>
) : MarkdownBlock {
    override val blockType get() = ContentBlock.Table
}

enum class MarkdownTableAlignment { Left, Center, Right, None }

data class MarkdownTableCell(val spans: List<MarkdownSpan>)

data class MarkdownTableRow(val cells: List<MarkdownTableCell>)