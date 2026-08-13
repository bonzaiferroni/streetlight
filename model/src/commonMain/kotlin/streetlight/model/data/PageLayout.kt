package streetlight.model.data

import kampfire.api.Markdown
import koala.Image
import kotlinx.serialization.Serializable

@Serializable
data class PageLayout(
    override val blocks: List<LayoutBlock>
): LayoutContainer {
    override val name get() = "main"
}

@Serializable
sealed interface LayoutBlock

interface LayoutContainer {
    val name: String
    val blocks: List<LayoutBlock>
}

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
object MapBlock: LayoutBlock

@Serializable
object EventsBlock: LayoutBlock

@Serializable
data class TabsBlock(
    val tabs: List<TabContent>
): LayoutBlock

@Serializable
data class TabContent(
    override val name: String,
    override val blocks: List<LayoutBlock>
): LayoutContainer

@Serializable
object FooterBlock: LayoutBlock