package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import streetlight.compose.markdown.annotatedMarkdownOf
import streetlight.compose.modifier.fadeClip
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import streetlight.model.data.EntityDetail
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.opacityLow
import streetlight.compose.modifier.zenBg
import streetlight.compose.modifier.heavyCardBg
import streetlight.compose.modifier.schemeBg
import streetlight.model.data.toDetails
import streetlight.compose.model.toFlair
import streetlight.model.data.toEntityType
import streetlight.model.data.Entity
import streetlight.model.data.FeedContext
import streetlight.compose.model.currentPortal
import streetlight.compose.model.toRoute
import streetlight.compose.modifier.pointerClick

/** The layouts of a feed row, the cousin of the web's `FeedMode`. */
enum class FeedMode { Minimal, Row, Grid }

/**
 * An entity in a feed, laid out by [mode], within the entity's scheme color, its details read for the [feed] it is in,
 * opening the entity's page when the app has one. The cousin of the web's `feedRow`.
 */
@Composable
context(feed: FeedContext?)
fun FeedRow(entity: Entity, mode: FeedMode = FeedMode.Minimal) {
    val route = entity.toRoute()
    val portal = currentPortal
    CompositionLocalProvider(LocalSchemeColor provides entity.toEntityType()) {
        Box(if (route != null) Modifier.pointerClick { portal.open(route) } else Modifier) {
            when (mode) {
                FeedMode.Minimal -> MinimalFeedRow(entity)
                FeedMode.Row -> RowFeedRow(entity)
                FeedMode.Grid -> GridFeedRow(entity)
            }
        }
    }
}

/**
 * The thumbnail beside a column of the entity's priority details, its title on one line, and its other
 * details, then the flair badge, as tall as the thumbnail, in the entity's color at low opacity. The other details are
 * in the ink at high opacity, as a description is, so the minimal row stays quiet.
 *
 * The row has a unit of padding and its parts are a unit apart.
 */
@Composable
context(feed: FeedContext?)
private fun MinimalFeedRow(entity: Entity) {
    val theme = currentTheme
    val rowHeight = theme.unit * MinimalRowHeightUnits
    val (priority, rest) = entity.toDetails(feed).partition { it.isPriority }

    Row(1, FeedRowMod.Base.padding(1)) {
        Thumbnail(entity, Modifier.size(rowHeight).sharedPart(entity.toRoute(), SharedPart.Image).borderRadius(1))
        Column(Modifier.weight(1f)) {
            EntityDetails(priority, isColored = true)
            BasicText(
                text = entity.label,
                modifier = Modifier.sharedPart(entity.toRoute(), SharedPart.Title),
                style = theme.text.heading5.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.15.em),
                color = { theme.colors.ink },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            EntityDetails(rest, isColored = false, Modifier.opacityHigh())
        }
        FlairBadge(entity, rowHeight)
    }
}

/**
 * The main part of the row beside its cells: the thumbnail, outlined; the title on one line over the description,
 * clipped at the row's height and fading where it reaches it; and the flair badge. The cells sit beside it as the
 * row's right half from [WideRowWidth], below it otherwise. The cousin of the web's `Row` mode.
 */
@Composable
context(feed: FeedContext?)
private fun RowFeedRow(entity: Entity) {
    val theme = currentTheme
    val rowHeight = theme.unit * RowHeightUnits

    Layout(
        content = {
            Row(1) {
                Thumbnail(entity, Modifier.size(rowHeight).sharedPart(entity.toRoute(), SharedPart.Image).borderRadius(1, outline = Outline.Mid))
                EntityText(
                    entity = entity,
                    modifier = Modifier.weight(1f)
                        .fadeClip(RowHeightUnits, inset = TextInsetUnits)
                        .padding(vertical = TextInsetUnits),
                )
                FlairBadge(entity, rowHeight)
            }
            CellGrid(entity.toDetails(feed), FeedRowMod.Cells)
        },
        modifier = FeedRowMod.Base.padding(1),
    ) { measurables, constraints ->
        val (main, cells) = measurables
        val width = constraints.maxWidth
        val gap = theme.unit.roundToPx()

        when (width >= WideRowWidth.roundToPx()) {
            // the cells take the right half, less half the gap, as the web's calc(50% - var(--unit) / 2)
            true -> {
                val cellsWidth = (width - gap) / 2
                val mainPlaceable = main.measure(Constraints.fixedWidth(width - cellsWidth - gap))
                val cellsPlaceable = cells.measure(Constraints.fixedWidth(cellsWidth))
                val height = maxOf(mainPlaceable.height, cellsPlaceable.height)
                layout(width, height) {
                    mainPlaceable.place(0, (height - mainPlaceable.height) / 2)
                    cellsPlaceable.place(width - cellsWidth, (height - cellsPlaceable.height) / 2)
                }
            }
            false -> {
                val mainPlaceable = main.measure(Constraints.fixedWidth(width))
                val cellsPlaceable = cells.measure(Constraints.fixedWidth(width))
                val cellsGap = if (cellsPlaceable.height > 0) gap else 0
                layout(width, mainPlaceable.height + cellsGap + cellsPlaceable.height) {
                    mainPlaceable.place(0, 0)
                    cellsPlaceable.place(0, mainPlaceable.height + cellsGap)
                }
            }
        }
    }
}

/**
 * An entry of a grid: the image across the top in its own shape, held between 1:2 and 4:1, whole over its backdrop; the
 * title over the description, clipped and fading as in the row, beside the flair badge; then the cells. The parts below the image are inset a unit at the
 * sides and stand a unit apart, and the entry's corners are rounded three units. The cousin of the web's `Grid` mode.
 */
@Composable
context(feed: FeedContext?)
private fun GridFeedRow(entity: Entity) {
    val theme = currentTheme
    val rowHeight = theme.unit * RowHeightUnits

    Column(1, FeedRowMod.GridBase.padding(bottom = 1)) {
        ContainImage(entity.image, Modifier.fillMaxWidth().aspectRatio(entity.imageRatio).sharedPart(entity.toRoute(), SharedPart.Image).background(entity.tint))
        Row(1, Modifier.padding(horizontal = 1)) {
            EntityText(entity, Modifier.weight(1f).fadeClip(RowHeightUnits))
            FlairBadge(entity, rowHeight)
        }
        CellGrid(entity.toDetails(feed), FeedRowMod.Cells)
    }
}

/** The title on one line over the description as markdown, with [modifier] setting how they are clipped. */
@Composable
private fun EntityText(entity: Entity, modifier: Modifier) {
    val theme = currentTheme
    val summary = remember(entity.body, theme.colors.primaryFg) { annotatedMarkdownOf(entity.body, theme.colors.primaryFg) }
    Column(modifier) {
        BasicText(
            text = entity.label,
            modifier = Modifier.sharedPart(entity.toRoute(), SharedPart.Title),
            style = theme.text.heading5.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.15.em),
            color = { theme.colors.ink },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        BasicText(
            text = summary,
            modifier = Modifier.padding(top = theme.line).opacityHigh(),
            style = theme.text.small.copy(lineHeight = 1.15.em),
            color = { theme.colors.ink },
        )
    }
}

/** The entity's image cropped to fill, over its [tint] so an entity without one keeps its shape. */
@Composable
private fun Thumbnail(entity: Entity, modifier: Modifier) {
    Box(modifier.background(entity.tint)) {
        AsyncImage(
            model = entity.image?.let { it.thumb ?: it.url },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** The shape of the entity's image in a grid entry: its own, held between [MinImageRatio] and [MaxImageRatio]. */
private val Entity.imageRatio get() = (image?.aspect ?: GridImageRatio).coerceIn(MinImageRatio, MaxImageRatio)

/** The entity's color, faint, behind its image. */
private val Entity.tint
    @Composable @ReadOnlyComposable get() = currentTheme.colors.colorOf(toEntityType()).copy(alpha = 0.2f)

/** The entity's flair, animated, in its color at low opacity, [size] square. */
@Composable
private fun FlairBadge(entity: Entity, size: Dp) {
    val theme = currentTheme
    AnimatedIcon(
        svg = entity.toFlair().animated,
        color = { theme.colors.colorOf(entity.toEntityType()) },
        modifier = Modifier.opacityLow(),
        size = size,
    )
}

/**
 * The [details] on one line, each after its label, joined by a dot and cut with an ellipsis: each in its color, or the
 * scheme color, when [isColored], otherwise all in the ink.
 */
@Composable
private fun EntityDetails(details: List<EntityDetail>, isColored: Boolean, modifier: Modifier = Modifier) {
    if (details.isEmpty()) return
    val theme = currentTheme
    val scheme = LocalSchemeColor.current

    val text = buildAnnotatedString {
        details.forEachIndexed { index, detail ->
            if (index > 0) withStyle(SpanStyle(color = theme.colors.muted)) { append(" · ") }
            val color = if (isColored) theme.colors.colorOf(detail.type ?: scheme) else theme.colors.ink
            withStyle(SpanStyle(color = color)) {
                detail.label?.let { append("$it ") }
                append(detail.text)
            }
        }
    }
    BasicText(text, modifier, style = theme.text.small, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** The height of a row in units, the web's `--row-height`, which the thumbnail fills and the text is clipped at. */
private const val RowHeightUnits = 10

/** The height of a minimal row in units, the web's `--row-height` in `Minimal`. */
private const val MinimalRowHeightUnits = 8

/** The space above and below a row's text, in units: the web's `--text-inset`. */
private const val TextInsetUnits = 0.5f

/** The width from which a row's cells sit beside it rather than below: the web's 960px container query. */
private val WideRowWidth = 960.dp

/** The shape of a grid entry's image when it has none of its own: the web's `aspect-ratio: 3 / 2`. */
private const val GridImageRatio = 3f / 2f

/** The tallest and the widest a grid entry's image may be, as width over height. */
private const val MinImageRatio = 1f / 2f
private const val MaxImageRatio = 4f

/** The modifiers the feed rows share. */
private object FeedRowMod {
    val Base = Modifier.fillMaxWidth().zenBg()
    val GridBase = Modifier.fillMaxWidth().borderRadius(3, moonShadow = true, outline = Outline.Low).heavyCardBg()
    val Cells = Modifier.padding(horizontal = 1).borderRadius(2.5, moonShadow = true, outline = Outline.Mid).schemeBg()
}
