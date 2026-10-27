package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import koala.markdown.HeadingLevel
import streetlight.compose.markdown.annotatedMarkdownOf
import streetlight.compose.model.FeedModel
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.model.data.CommentsBlock
import streetlight.model.data.DesignContent
import streetlight.model.data.Entity
import streetlight.model.data.EntityFeed
import streetlight.model.data.EventsBlock
import streetlight.model.data.GalaxyContent
import streetlight.model.data.HeaderBlock
import streetlight.model.data.HeadingBlock
import streetlight.model.data.ImageBlock
import streetlight.model.data.ImageShape
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LocationContent
import streetlight.model.data.MapBlock
import streetlight.model.data.Media
import streetlight.model.data.PageLayout
import streetlight.model.data.PostsBlock
import streetlight.model.data.RichTextBlock
import streetlight.model.data.Size3
import streetlight.model.data.StarContent
import streetlight.model.data.TextBlock
import streetlight.model.data.toDetails

/**
 * Adds the page of [content] to the grid by its design's layout, or the default layout of its kind, the [feed] as last
 * read, [page], in [mode], each block [side] units in from the edges. The cousin of the web's `renderLayout`.
 */
context(feed: FeedModel)
fun LazyStaggeredGridScope.renderLayout(content: DesignContent, page: EntityFeed, mode: FeedMode, side: Int) {
    val layout = content.design?.layout ?: PageLayout.defaultOf(content)
    layout.blocks.forEach { renderBlock(it, content, page, mode, side) }
}

/** Adds [block] of the page of [content] to the grid, as [renderLayout] does. The cousin of the web's `renderBlock`. */
context(feed: FeedModel)
fun LazyStaggeredGridScope.renderBlock(
    block: LayoutBlock,
    content: DesignContent,
    page: EntityFeed,
    mode: FeedMode,
    side: Int,
) {
    val sideMod = Modifier.padding(horizontal = side)
    when (block) {
        is HeaderBlock -> fullItem { RenderHeader(block, content, sideMod.padding(vertical = 1)) }
        PostsBlock, EventsBlock -> feedSection(page, mode)
        MapBlock -> fullItem { RenderMap(content, sideMod) }
        is HeadingBlock -> fullItem { RenderHeading(block, sideMod) }
        is TextBlock -> fullItem { RenderText(block, sideMod) }
        is RichTextBlock -> fullItem { RenderRichText(block, sideMod) }
        is ImageBlock -> fullItem { RenderImage(block, sideMod) }
        // comments, skills, galleries, tabs and columns wait for their own bounty
        else -> fullItem { RenderPlaceholder(block, sideMod) }
    }
}

/** The header of [content], laid out by [block]. A medium's header waits for its own bounty. The web's `renderHeader`. */
@Composable
fun RenderHeader(block: HeaderBlock, content: DesignContent, modifier: Modifier = Modifier) {
    when (content) {
        is LocationContent -> EntityHeader(content.location, "at", content.location.detailsOf(block), modifier)
        is StarContent -> EntityHeader(content.star, "a streetlighter", content.star.detailsOf(block), modifier)
        is GalaxyContent -> Column(1, modifier) {
            EntityHeader(content.galaxy, "a galaxy", content.galaxy.detailsOf(block))
            GalaxyButtons()
        }
        is Media -> RenderPlaceholder(block, modifier)
    }
}

/** The details a header shows by [block]: the entity's own, or none when it hides its cells. */
private fun Entity.detailsOf(block: HeaderBlock) = if (block.showCells) toDetails() else emptyList()

/** The galaxy menu at the start and the button to create a post at the end, which do nothing until the app has them. */
@Composable
private fun GalaxyButtons() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Button("galaxies", onClick = {}, style = ButtonStyle.Zen)
        Button("Create Post", onClick = {}, style = ButtonStyle.Zen)
    }
}

/** The map of [content], looking over its point. The web's `renderMap`. */
@Composable
fun RenderMap(content: DesignContent, modifier: Modifier = Modifier) {
    MapSlot(
        modifier.fillMaxWidth().height(currentTheme.unit * MapHeightUnits).borderRadius(2, moonShadow = true),
        center = content.geoPoint,
    )
}

/** The heading of [block] at its level, in a filigree when it has one. The web's `renderHeading`. */
@Composable
fun RenderHeading(block: HeadingBlock, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val style = when (block.level) {
        HeadingLevel.H1 -> theme.text.heading1
        HeadingLevel.H2 -> theme.text.heading2
        HeadingLevel.H3 -> theme.text.heading3
        HeadingLevel.H4 -> theme.text.heading4
        HeadingLevel.H5 -> theme.text.heading5
        HeadingLevel.H6 -> theme.text.heading6
    }
    BasicText(
        block.text,
        modifier.fillMaxWidth().then(if (block.hasFiligree) Modifier.filigree() else Modifier),
        style = style.copy(lineHeight = 1.em, textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}

/** The plain text of [block]. The web's `renderText`. */
@Composable
fun RenderText(block: TextBlock, modifier: Modifier = Modifier) {
    val theme = currentTheme
    BasicText(block.text, modifier, style = theme.text.paragraph, color = { theme.colors.ink })
}

/** The markdown of [block] at its size. The web's `renderRichText`. */
@Composable
fun RenderRichText(block: RichTextBlock, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val style = when (block.size) {
        Size3.Small -> theme.text.small
        Size3.Normal, null -> theme.text.paragraph
        Size3.Large -> theme.text.large
    }
    val text = remember(block.text, theme.colors.primaryFg) { annotatedMarkdownOf(block.text, theme.colors.primaryFg) }
    BasicText(text, modifier, style = style, color = { theme.colors.ink })
}

/**
 * The image of [block] at its share of the width, rounded unless square. Its other shapes and its fit wait; it is
 * contained. The web's `renderImage`.
 */
@Composable
fun RenderImage(block: ImageBlock, modifier: Modifier = Modifier) {
    val width = block.width?.let { it / 100f } ?: 1f
    val shape = if (block.shape == ImageShape.Square) Modifier else Modifier.borderRadius(2)
    ContainImage(block.image, modifier.fillMaxWidth(width).height(currentTheme.unit * ImageHeightUnits).then(shape))
}

/** The note that [block] is coming soon, in place of a block the app does not yet lay out. */
@Composable
fun RenderPlaceholder(block: LayoutBlock, modifier: Modifier = Modifier) {
    val theme = currentTheme
    BasicText(
        "${block.blockType.label} coming soon",
        modifier.fillMaxWidth().padding(2),
        style = theme.text.regular.copy(textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}

/** The height of a map block in units, as on the web's pages. */
private const val MapHeightUnits = 48

/** The height of an image block in units. */
private const val ImageHeightUnits = 48
