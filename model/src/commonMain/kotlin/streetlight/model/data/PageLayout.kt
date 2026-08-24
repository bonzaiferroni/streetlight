package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable

@Serializable
data class PageLayout(
    override val blocks: List<LayoutBlock>
): LayoutContainer {
    override val name get() = "main"
}

enum class BlockType(label: String? = null): Labeled {
    Text,
    RichText("Rich Text"),
    Heading,
    Image,
    Gallery,
    Header,
    Map,
    Events,
    Tabs,
    Columns;

    override val label = label ?: name
}

interface LayoutContainer {
    val name: String
    val blocks: List<LayoutBlock>
}

@Serializable
data class TabContent(
    override val name: String,
    override val blocks: List<LayoutBlock>
): LayoutContainer