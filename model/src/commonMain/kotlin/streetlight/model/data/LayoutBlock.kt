package streetlight.model.data

import kampfire.api.Markdown
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
sealed interface LayoutBlock {
    val blockType: BlockType
}

@Serializable
data class ImageBlock(
    val image: Image?,
    val frame: ImageFrame? = null,
): LayoutBlock {
    override val blockType get() = BlockType.Image
}

enum class ImageFrame {
    Circle,
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