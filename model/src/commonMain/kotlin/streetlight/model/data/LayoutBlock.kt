package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.Labeled
import koala.markdown.HeadingLevel
import kotlinx.serialization.Serializable

@Serializable
sealed interface LayoutBlock {
    val blockType: BlockType
}

@Serializable
data class TextBlock(
    val text: String,
): LayoutBlock {
    override val blockType get() = BlockType.Text
}

@Serializable
data class RichTextBlock(
    val text: Markdown,
    val size: Size3?,
): LayoutBlock {
    override val blockType get() = BlockType.RichText
}

@Serializable
data class HeadingBlock(
    val text: String,
    val level: HeadingLevel,
    val hasFiligree: Boolean,
): LayoutBlock {
    override val blockType get() = BlockType.Heading
}

@Serializable
object HeaderBlock: LayoutBlock {
    override val blockType get() = BlockType.Header
}

@Serializable
object MapBlock: LayoutBlock {
    override val blockType get() = BlockType.Map
}

@Serializable
object EventsBlock: LayoutBlock {
    override val blockType get() = BlockType.Events
}

@Serializable
data class TabsBlock(
    val tabs: List<TabContent>
): LayoutBlock {
    override val blockType get() = BlockType.Tabs
}

@Serializable
data class ColumnsBlock(
    override val blocks: List<LayoutBlock>
): LayoutBlock, LayoutContainer {
    override val blockType get() = BlockType.Columns
    override val name get() = blockType.label
}

enum class Size3: Labeled {
    Small,
    Normal,
    Large;

    override val label get() = name

    companion object {
        val default = Normal
    }
}