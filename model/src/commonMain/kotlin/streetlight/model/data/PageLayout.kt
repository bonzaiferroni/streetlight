package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable

/** The blocks of a page, in order. */
@Serializable
data class PageLayout(
    override val blocks: List<LayoutBlock>
): LayoutContainer {
    override val name get() = "main"

    companion object {
        /** The layout of [content]'s kind of page, when it has no design. */
        fun defaultOf(content: DesignContent) = when(content) {
            is LocationContent -> DefaultLayout.location
            is Media -> DefaultLayout.media
            is StarContent -> DefaultLayout.star
            is GalaxyContent -> DefaultLayout.galaxy
        }
    }
}

/** The kinds of [LayoutBlock]. */
enum class BlockType(label: String? = null): Labeled {
    Header, Comments, Posts, Events, Map,
    Heading, Text, RichText("Rich Text"),
    Image, Gallery,
    Tabs, Columns;

    override val label = label ?: name
}

/** A named list of layout blocks, such as a page or a tab. */
interface LayoutContainer {
    val name: String
    val blocks: List<LayoutBlock>
}

/** A tab of a [TabsBlock] and its blocks. */
@Serializable
data class TabContent(
    override val name: String,
    override val blocks: List<LayoutBlock>
): LayoutContainer