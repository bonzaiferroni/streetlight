package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable

@Serializable
data class PageLayout(
    override val blocks: List<LayoutBlock>
): LayoutContainer {
    override val name get() = "main"

    companion object {
        fun defaultOf(content: DesignContent) = when(content) {
            is LocationContent -> DefaultLayout.location
            is Media -> DefaultLayout.media
            is StarContent -> DefaultLayout.star
            is GalaxyContent -> DefaultLayout.galaxy
        }
    }
}

enum class BlockType(label: String? = null): Labeled {
    Header, Comments, Posts, Events, Map,
    Heading, Text, RichText("Rich Text"),
    Image, Gallery,
    Tabs, Columns;

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