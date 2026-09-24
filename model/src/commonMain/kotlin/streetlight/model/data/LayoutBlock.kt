package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.Labeled
import koala.markdown.HeadingLevel
import kotlinx.serialization.Serializable

/** A block of a page layout, placed by the page's design. */
@Serializable
sealed interface LayoutBlock {
    val blockType: BlockType
}

/** A layout block of plain text. */
@Serializable
data class TextBlock(
    val text: String,
): LayoutBlock {
    override val blockType get() = BlockType.Text
}

/** A layout block of markdown, at a size. */
@Serializable
data class RichTextBlock(
    val text: Markdown,
    val size: Size3?,
): LayoutBlock {
    override val blockType get() = BlockType.RichText
}

/** A layout block holding a heading, optionally in a filigree. */
@Serializable
data class HeadingBlock(
    val text: String,
    val level: HeadingLevel,
    val hasFiligree: Boolean,
): LayoutBlock {
    override val blockType get() = BlockType.Heading
}

/** The layout block where the page's entity header goes. */
@Serializable
object HeaderBlock: LayoutBlock {
    override val blockType get() = BlockType.Header
}

/** The layout block where the page's comments go. */
@Serializable
object CommentsBlock: LayoutBlock {
    override val blockType get() = BlockType.Comments
}

/** The layout block where the page's feed goes. */
@Serializable
object PostsBlock: LayoutBlock {
    override val blockType get() = BlockType.Posts
}

/** The layout block where the page's map goes. */
@Serializable
object MapBlock: LayoutBlock {
    override val blockType get() = BlockType.Map
}

/** The layout block where the page's events go. */
@Serializable
object EventsBlock: LayoutBlock {
    override val blockType get() = BlockType.Events
}

/** A layout block of tabs, each holding its own blocks. */
@Serializable
data class TabsBlock(
    val tabs: List<TabContent>
): LayoutBlock {
    override val blockType get() = BlockType.Tabs
}

/** A layout block of columns, each holding its own blocks. */
@Serializable
data class ColumnsBlock(
    override val blocks: List<LayoutBlock>
): LayoutBlock, LayoutContainer {
    override val blockType get() = BlockType.Columns
    override val name get() = blockType.label
}

/** Three sizes: small, medium and large. */
enum class Size3: Labeled {
    Small,
    Normal,
    Large;

    override val label get() = name

    companion object {
        val default = Normal
    }
}