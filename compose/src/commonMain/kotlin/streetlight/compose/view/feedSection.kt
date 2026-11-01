package streetlight.compose.view

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import koala.Svg
import koala.SvgFile
import streetlight.compose.model.FeedModel
import streetlight.compose.model.toSvg
import streetlight.compose.modifier.pointerClick
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedType
import androidx.compose.foundation.layout.Spacer
import streetlight.compose.model.isSearched

/**
 * Adds the feed section of the [feed] model to this grid, showing [page], the feed as the view last read it, in [mode],
 * its entries opening their pages: the menu of the types its context offers with the switch of [FeedMode]; for a type
 * that is searched, the tag filter of an events feed and the search; then the entries, or a message when it has none.
 * The cousin of the web's `feedSection`.
 *
 * A lazy grid's builder cannot read state, so the view reads [page] and [mode] and passes them in; the controls read the
 * rest from [feed]. The menu and the message span the grid's row. An entry takes one column in `Grid` mode, and the
 * row otherwise.
 */
context(feed: FeedModel)
fun LazyGridScope.feedSection(page: EntityFeed, mode: FeedMode) {
    val source = page.source
    val type = source?.type ?: FeedType.Events
    val types = page.types ?: source?.context?.types.orEmpty()

    fullItem { FeedMenu(types, Modifier.padding(horizontal = 1, vertical = 1)) }
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
    // in grid mode the grid holds its items a unit in and a unit apart, in place of their own side and bottom padding
    val side = if (mode == FeedMode.Grid) 0 else 1

    fullItem { FeedMenu(types, Modifier.padding(start = side, top = 1, end = side, bottom = side)) }
    if (type.isSearched) fullItem { FeedFilter(type, Modifier.padding(start = side, end = side, bottom = 1)) }
    if (page.entities.isEmpty()) fullItem { EmptyFeed() }
    items(page.entities, span = { if (mode == FeedMode.Grid) StaggeredGridItemSpan.SingleLane else StaggeredGridItemSpan.FullLine }) { entity ->
        context(source?.context) { FeedRow(entity, mode) }
    }
}

/**
 * The menu of [types], bound to the [feed]'s type, with the switch of its mode at the end, over a rule. The cousin of
 * the web's type menu.
 */
@Composable
context(feed: FeedModel)
private fun FeedMenu(types: List<FeedType>, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val selectedType by feed.typeState.collectAsState()
    val selectedMode by feed.modeState.collectAsState()
    val rule = theme.colors.ink.copy(alpha = RuleAlpha)

    Row(
        1,
        modifier.fillMaxWidth().drawBehind {
            val y = size.height - theme.line.toPx() / 2
            drawLine(rule, Offset(0f, y), Offset(size.width, y), theme.line.toPx())
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(1, Modifier.weight(1f)) {
            types.forEach { option -> FeedTypeOption(option, option == selectedType) { feed.typeState.set(option) } }
        }
        Row(1, Modifier.opacityHigh()) {
            FeedMode.entries.forEach { option -> SwitchIcon(option.toSvg(), option == selectedMode) { feed.modeState.set(option) } }
        }
    }
}

/**
 * An option of the type menu, naming [type], larger when [isSelected]. It is laid out at the selected size and scaled
 * down from its baseline, so its size changes without the text being laid out again.
 */
@Composable
private fun FeedTypeOption(type: FeedType, isSelected: Boolean, modifier: Modifier = Modifier, onSelect: () -> Unit) {
    val theme = currentTheme
    val unselectedScale = theme.text.heading5.fontSize.value / theme.text.heading2.fontSize.value
    val scale by animateFloatAsState(if (isSelected) 1f else unselectedScale, theme.magic.eased())
    var baseline by remember { mutableFloatStateOf(1f) }
    BasicText(
        text = type.label,
        modifier = modifier
            .pointerClick(onSelect)
            // the width follows the scale, so the options after it close up
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout((placeable.width * scale).roundToInt(), placeable.height) { placeable.place(0, 0) }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, baseline)
            },
        style = theme.text.heading2.copy(fontWeight = FontWeight.Medium, lineHeight = 1.em),
        onTextLayout = { baseline = it.firstBaseline / it.size.height },
        color = { theme.colors.ink },
        maxLines = 1,
        softWrap = false,
    )
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

/** The alpha of the rule under the type menu: the web's `BorderBottom2Px`, at a quarter of the ink. */
private const val RuleAlpha = 0.25f

/** The height of the empty feed's message, in units. */
private const val EmptyUnits = 24
