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
import kampfire.model.MutableTap
import koala.Svg
import koala.SvgFile
import streetlight.compose.model.heading
import streetlight.compose.model.toSvg
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedType

/**
 * Adds the feed section to this grid: the heading of the [feed]'s type in a filigree, between the menu of the types its
 * context offers, bound to [typeState], and the switch of [FeedMode], bound to [modeState]; then the feed's entries in
 * [mode], or a message when it has none. The cousin of the web's `feedSection`.
 *
 * The heading and the message span the grid's row. An entry takes one column in `Grid` mode, and the row otherwise.
 */
fun LazyGridScope.feedSection(
    feed: EntityFeed,
    typeState: MutableTap<FeedType>,
    mode: FeedMode,
    modeState: MutableTap<FeedMode>,
) {
    val source = feed.source
    val types = feed.types ?: source?.context?.types.orEmpty()

    fullItem { FeedHeading(source?.type ?: FeedType.Events, types, typeState, modeState) }
    if (feed.entities.isEmpty()) fullItem { EmptyFeed() }
    items(feed.entities, span = { GridItemSpan(if (mode == FeedMode.Grid) 1 else maxLineSpan) }) { entity ->
        FeedRow(entity, mode)
    }
}

/**
 * Adds the feed section to this staggered grid, as [LazyGridScope.feedSection] does to a grid. An entry takes one column
 * in `Grid` mode, its height its own, and the row otherwise.
 */
fun LazyStaggeredGridScope.feedSection(
    feed: EntityFeed,
    typeState: MutableTap<FeedType>,
    mode: FeedMode,
    modeState: MutableTap<FeedMode>,
) {
    val source = feed.source
    val types = feed.types ?: source?.context?.types.orEmpty()

    fullItem { FeedHeading(source?.type ?: FeedType.Events, types, typeState, modeState, isInset = mode == FeedMode.Grid) }
    if (feed.entities.isEmpty()) fullItem { EmptyFeed() }
    items(feed.entities, span = { if (mode == FeedMode.Grid) StaggeredGridItemSpan.SingleLane else StaggeredGridItemSpan.FullLine }) { entity ->
        FeedRow(entity, mode)
    }
}

/**
 * The heading of a feed of [type] in a filigree, the type menu left of it and the mode switch right of it, a unit in
 * from the sides unless [isInset], when the grid already holds it in.
 */
@Composable
private fun FeedHeading(
    type: FeedType,
    types: List<FeedType>,
    typeState: MutableTap<FeedType>,
    modeState: MutableTap<FeedMode>,
    isInset: Boolean = false,
) {
    val theme = currentTheme
    val selectedType by typeState.collectAsState()
    val selectedMode by modeState.collectAsState()

    // the menu and the switch are the same width, so the heading stays centered
    Row(Modifier.fillMaxWidth().padding(horizontal = if (isInset) 0 else 1, vertical = 1), verticalAlignment = Alignment.CenterVertically) {
        Row(1, Modifier.width(theme.unit * SwitchUnits).opacityHigh()) {
            types.forEach { option -> SwitchIcon(option.toSvg(), option == selectedType) { typeState.set(option) } }
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
            FeedMode.entries.forEach { option -> SwitchIcon(option.toSvg(), option == selectedMode) { modeState.set(option) } }
        }
    }
}

/** An option of a switch, drawn as [svg], in the primary color when [isSelected]. */
@Composable
private fun SwitchIcon(svg: Svg, isSelected: Boolean, onSelect: () -> Unit) {
    val theme = currentTheme
    Icon(
        svg = svg,
        color = { if (isSelected) theme.colors.primaryFg else theme.colors.ink },
        modifier = Modifier.clickable(onClick = onSelect),
    )
}

/** The message in place of an empty feed: an animated icon over a line of text, centered in its height. */
@Composable
private fun EmptyFeed() {
    val theme = currentTheme
    Column(
        Modifier.fillMaxWidth().height(theme.unit * EmptyUnits),
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
