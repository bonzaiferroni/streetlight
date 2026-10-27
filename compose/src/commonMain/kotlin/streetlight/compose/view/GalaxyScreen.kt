package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import koala.markdown.HeadingLevel
import streetlight.compose.model.FeedModel
import streetlight.compose.model.GalaxyModel
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.EntityFeed
import streetlight.model.data.EventsBlock
import streetlight.model.data.GalaxyContent
import streetlight.model.data.HeaderBlock
import streetlight.model.data.HeadingBlock
import streetlight.model.data.ImageBlock
import streetlight.model.data.LayoutBlock
import streetlight.model.data.MapBlock
import streetlight.model.data.PageLayout
import streetlight.model.data.PostsBlock
import streetlight.model.data.TextBlock
import streetlight.model.data.toDetails
import streetlight.compose.model.GalaxyRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The screen of [route]: its galaxy once inflated, shown in the [GalaxyView]. */
@Composable
fun GalaxyScreen(route: GalaxyRoute) = Inflated<GalaxyContent>(route) { content ->
    context(koinViewModel<GalaxyModel> { parametersOf(content) }) { GalaxyView() }
}

/**
 * The view of [GalaxyRoute]: the header, then the galaxy's page layout block by block, as the
 * web's `renderLayout` lays it out, in the galaxy's own theme, and the footer. The cousin of the web's `galaxyShell`.
 */
@Composable
context(model: GalaxyModel)
fun GalaxyView() {
    val loaded = model.content
    val page by model.feed.feedState.collectAsState()
    val mode by model.feed.modeState.collectAsState()
    val theme = currentTheme
    PageThemeEffect(loaded.galaxy.design?.theme)

    // in grid mode the grid holds its items a unit from the edges, in place of their own padding
    val isGrid = mode == FeedMode.Grid
    val side = if (isGrid) 0 else 1
    val layout = loaded.galaxy.design?.layout ?: PageLayout.defaultOf(loaded)

    LazyStaggeredGridView(
        spacing = if (isGrid) theme.unit else theme.line,
        contentPadding = PaddingValues(if (isGrid) theme.unit else 0.dp),
    ) {
        fullItem { AppHeader() }
        context(model.feed) {
            layout.blocks.forEach { block -> layoutBlock(block, loaded, page, mode, side) }
        }
        fullItem { AppFooter(GalaxyViewSource, Modifier.padding(top = 8)) }
    }
}

/**
 * Adds [block] of the galaxy's page to the grid, from its [content] and the [feed] as last read, [page], in [mode], a
 * full item [side] units in from the edges. A block the app does not yet lay out adds nothing.
 */
context(feed: FeedModel)
private fun LazyStaggeredGridScope.layoutBlock(
    block: LayoutBlock,
    content: GalaxyContent,
    page: EntityFeed,
    mode: FeedMode,
    side: Int,
) {
    when (block) {
        is HeaderBlock -> fullItem {
            Column(1, Modifier.padding(horizontal = side, vertical = 1)) {
                EntityHeader(content.galaxy, "a galaxy", if (block.showCells) content.galaxy.toDetails() else emptyList())
                GalaxyButtons()
            }
        }
        PostsBlock, EventsBlock -> feedSection(page, mode)
        MapBlock -> fullItem {
            MapSlot(
                Modifier.fillMaxWidth().padding(horizontal = side).height(currentTheme.unit * MapHeightUnits)
                    .borderRadius(2, moonShadow = true),
                center = content.galaxy.geoPoint,
            )
        }
        is HeadingBlock -> fullItem { LayoutHeading(block, Modifier.padding(horizontal = side)) }
        is TextBlock -> fullItem {
            val theme = currentTheme
            BasicText(block.text, Modifier.padding(horizontal = side), style = theme.text.paragraph, color = { theme.colors.ink })
        }
        is ImageBlock -> fullItem { ContainImage(block.image, Modifier.fillMaxWidth().padding(horizontal = side).height(currentTheme.unit * ImageHeightUnits)) }
        // comments, skills, rich text, galleries, tabs and columns wait for their own bounty
        else -> Unit
    }
}

/** The galaxy menu at the start and the button to create a post at the end, which do nothing until the app has them. */
@Composable
private fun GalaxyButtons() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Button("galaxies", onClick = {}, style = ButtonStyle.Zen)
        Button("Create Post", onClick = {}, style = ButtonStyle.Zen)
    }
}

/** A heading of the page's layout at its level, in a filigree when it has one. The web's `renderHeading`. */
@Composable
private fun LayoutHeading(block: HeadingBlock, modifier: Modifier) {
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

/** The height of a map block in units, as on the web's pages. */
private const val MapHeightUnits = 48

/** The height of an image block in units. */
private const val ImageHeightUnits = 48

/** The path of this view's source, linked from its footer. */
private const val GalaxyViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/GalaxyScreen.kt"
