package streetlight.model.data

import kampfire.api.Markdown
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class Layout(
    val blocks: List<LayoutBlock>
)

@Serializable
sealed interface LayoutBlock

@Serializable
data class ImageBlock(
    val image: Image
): LayoutBlock

@Serializable
data class TextBlock(
    val text: String,
): LayoutBlock

@Serializable
data class RichTextBlock(
    val text: Markdown,
): LayoutBlock

@Serializable
object HeaderBlock: LayoutBlock

@Serializable
object FooterBlock: LayoutBlock

@Serializable
object MapBlock: LayoutBlock

@Serializable
object EventsBlock: LayoutBlock

@Serializable
data class TabsBlock(
    val tabs: List<TabContent>
): LayoutBlock

@Serializable
data class TabContent(
    val name: String,
    val blocks: List<LayoutBlock>
)