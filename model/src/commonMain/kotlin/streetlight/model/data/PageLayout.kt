package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.Labeled
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class PageLayout(
    override val blocks: List<LayoutBlock>
): LayoutContainer {
    override val name get() = "main"
}

enum class BlockType(label: String? = null): Labeled {
    Image,
    Text,
    RichText("Rich Text"),
    Header,
    Map,
    Events,
    Tabs;

    override val label = label ?: name
}

@Serializable
sealed interface LayoutBlock {
    val blockType: BlockType
}

interface LayoutContainer {
    val name: String
    val blocks: List<LayoutBlock>
}

@Serializable
data class ImageBlock(
    val image: Image
): LayoutBlock {
    override val blockType get() = BlockType.Image
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
): LayoutBlock {
    override val blockType get() = BlockType.RichText
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
data class TabContent(
    override val name: String,
    override val blocks: List<LayoutBlock>
): LayoutContainer