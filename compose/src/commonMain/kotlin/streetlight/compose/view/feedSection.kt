package streetlight.compose.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import koala.Svg
import koala.SvgFile
import streetlight.compose.model.FeedModel
import streetlight.compose.model.heading
import streetlight.compose.model.toSvg
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedType
import androidx.compose.foundation.layout.Spacer
import streetlight.compose.model.isSearched

/**
 * Adds the feed section of the [feed] model to this grid, showing [page], the feed as the view last read it, in [mode]:
 * the heading of its type in a filigree, between the menu of the types its context offers and the switch of
 * [FeedMode]; for a type that is searched, the tag filter of an events feed and the search; then the entries, or a
 * message when it has none. The cousin of the web's `feedSection`.
 *
 * A lazy grid's builder cannot read state, so the view reads [page] and [mode] and passes them in; the controls read the
 * rest from [feed]. The heading and the message span the grid's row. An entry takes one column in `Grid` mode, and the
 * row otherwise.
 */
context(feed: FeedModel)
fun LazyGridScope.feedSection(page: EntityFeed, mode: FeedMode) {
    val source = page.source
    val type = source?.type ?: FeedType.Events
    val types = page.types ?: source?.context?.types.orEmpty()

    fullItem { FeedHeading(type, types, Modifier.padding(horizontal = 1, vertical = 1)) }
    if (type.isSearched) fullItem { FeedFilter(type, Modifier.padding(start = 1, end = 1, bottom = 1)) }
    if (page.entities.isEmpty()) fullItem { EmptyFeed() }
    items(page.entities, span = { GridItemSpan(if (mode == FeedMode.Grid) 1 else maxLineSpan) }) { entity ->
        context(source?.context) { FeedRow(entity, mode) }
    }
}

/**
 * Adds the feed section of the [feed] model to this staggered grid, as [LazyGridScope.feedSection] does to a grid. An
 * entry takes one column in `Grid` mode, its height its own, and the row otherwise.
 */
context(feed: FeedModel)
fun LazyStaggeredGridScope.feedSection(page: EntityFeed, mode: FeedMode) {
    val source = page.source
    val type = source?.type ?: FeedType.Events
    val types = page.types ?: source?.context?.types.orEmpty()
    // in grid mode the grid holds its items a unit in, in place of their own side padding
    val side = if (mode == FeedMode.Grid) 0 else 1

    fullItem { FeedHeading(type, types, Modifier.padding(horizontal = side, vertical = 1)) }
    if (type.isSearched) fullItem { FeedFilter(type, Modifier.padding(start = side, end = side, bottom = 1)) }
    if (page.entities.isEmpty()) fullItem { EmptyFeed() }
    items(page.entities, span = { if (mode == FeedMode.Grid) StaggeredGridItemSpan.SingleLane else StaggeredGridItemSpan.FullLine }) { entity ->
        context(source?.context) { FeedRow(entity, mode) }
    }
}

/**
 * The heading of a feed of [type] in a filigree, the menu of [types] left of it, bound to the [feed]'s type, and the
 * switch of its mode right of it.
 */
@Composable
context(feed: FeedModel)
private fun FeedHeading(type: FeedType, types: List<FeedType>, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val selectedType by feed.typeState.collectAsState()
    val selectedMode by feed.modeState.collectAsState()

    // the menu and the switch are the same width, so the heading stays centered
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(1, Modifier.width(theme.unit * SwitchUnits).opacityHigh()) {
            types.forEach { option -> SwitchIcon(option.toSvg(), option == selectedType) { feed.typeState.set(option) } }
        }
        BasicText(
            text = type.heading,
            modifier = Modifier.weight(1f).filigree(),
            style = theme.text.heading2.copy(
                lineHeight = 1.em,
                textAlign = TextAlign.Center,
                shadow = Shadow(theme.colors.paper, Offset(0f, 2f), 8f),
            ),
            color = { theme.colors.ink },
            maxLines = 1,
            softWrap = false,
        )
        Row(
            Modifier.width(theme.unit * SwitchUnits).opacityHigh(),
            horizontalArrangement = Arrangement.spacedBy(theme.unit, Alignment.End),
        ) {
            FeedMode.entries.forEach { option -> SwitchIcon(option.toSvg(), option == selectedMode) { feed.modeState.set(option) } }
        }
    }
}

/**
 * The filters of the [feed], of [type]: the tag filter at the start for events, and the search at the end.
 */
@Composable
context(feed: FeedModel)
private fun FeedFilter(type: FeedType, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (type == FeedType.Events) TagFilter(feed.tagState) else Spacer(Modifier)
        SearchField(feed.searchState)
    }
}

/** An option of a switch, drawn as [svg], in the primary color when [isSelected]. */
@Composable
private fun SwitchIcon(svg: Svg, isSelected: Boolean, modifier: Modifier = Modifier, onSelect: () -> Unit) {
    val theme = currentTheme
    Icon(
        svg = svg,
        color = { if (isSelected) theme.colors.primaryFg else theme.colors.ink },
        modifier = modifier.clickable(onClick = onSelect),
    )
}

/** The message in place of an empty feed: an animated icon over a line of text, centered in its height. */
@Composable
private fun EmptyFeed(modifier: Modifier = Modifier) {
    val theme = currentTheme
    Column(
        modifier.fillMaxWidth().height(theme.unit * EmptyUnits),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedIcon(SvgFile.Binoculars.animated, color = { theme.colors.ink }, size = theme.unit * 10)
        BasicText("Nothing to show!", Modifier.opacityHigh(), style = theme.text.regular, color = { theme.colors.ink })
    }
}

/** The width of the type menu and of the mode switch, in units: the web's `FeedSection.SwitchWidth`. */
private const val SwitchUnits = 12

/** The height of the empty feed's message, in units. */
private const val EmptyUnits = 24
