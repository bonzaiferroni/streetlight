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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import streetlight.compose.model.EntityDetail
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.opacityLow
import streetlight.compose.modifier.zenBg
import streetlight.compose.modifier.shineBg
import streetlight.compose.model.bottomDetails
import streetlight.compose.model.toFlair
import streetlight.compose.model.toThemeColor
import streetlight.compose.model.topDetails
import streetlight.model.data.Entity

/** The layouts of a feed row, the cousin of the web's `FeedMode`. */
enum class FeedMode { Minimal, Row, Grid }

/** An entity in a feed, laid out by [mode]. The cousin of the web's `feedRow`. */
@Composable
fun FeedRow(entity: Entity, mode: FeedMode = FeedMode.Minimal) {
    when (mode) {
        FeedMode.Minimal -> MinimalFeedRow(entity)
        FeedMode.Row -> RowFeedRow(entity)
        FeedMode.Grid -> GridFeedRow(entity)
    }
}

/**
 * The thumbnail beside a column of the entity's top details, its title on up to two lines, and its bottom details,
 * then the flair badge, as tall as the thumbnail, in the entity's color at low opacity. The bottom details are in the
 * ink at high opacity, as a description is, so the minimal row stays quiet.
 *
 * The row has a unit of padding and its parts are a unit apart.
 */
@Composable
private fun MinimalFeedRow(entity: Entity) {
    val theme = currentTheme
    val rowHeight = theme.unit * RowHeightUnits

    Row(1, FeedRowMod.Base.padding(1)) {
        Thumbnail(entity, Modifier.size(rowHeight).borderRadius(1))
        Column(Modifier.weight(1f)) {
            EntityDetails(entity.topDetails, isColored = true)
            BasicText(
                text = entity.label,
                style = theme.text.regular.copy(lineHeight = 1.15.em),
                color = { theme.colors.ink },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            EntityDetails(entity.bottomDetails, isColored = false, Modifier.opacityHigh())
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
private fun RowFeedRow(entity: Entity) {
    val theme = currentTheme
    val rowHeight = theme.unit * RowHeightUnits

    Layout(
        content = {
            Row(1) {
                Thumbnail(entity, Modifier.size(rowHeight).borderRadius(1, outline = true))
                EntityText(
                    entity = entity,
                    modifier = Modifier.weight(1f)
                        .fadeClip(RowHeightUnits, inset = TextInsetUnits)
                        .padding(vertical = TextInsetUnits),
                )
                FlairBadge(entity, rowHeight)
            }
            CellGrid(entity.topDetails, entity.bottomDetails, FeedRowMod.Cells)
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
 * An entry of a grid: the image across the top at 3:2, whole over its backdrop; the title over the description, clipped
 * and fading as in the row, beside the flair badge; then the cells. The parts below the image are inset a unit at the
 * sides and stand a unit apart. The cousin of the web's `Grid` mode.
 */
@Composable
private fun GridFeedRow(entity: Entity) {
    val theme = currentTheme
    val rowHeight = theme.unit * RowHeightUnits

    Column(1, FeedRowMod.Base.padding(bottom = 2)) {
        Thumbnail(
            entity = entity,
            modifier = Modifier.fillMaxWidth().aspectRatio(GridImageRatio),
            url = entity.image?.let { it.medium ?: it.url },
            scale = ContentScale.Fit,
        )
        Row(1, Modifier.padding(horizontal = 1)) {
            EntityText(entity, Modifier.weight(1f).fadeClip(RowHeightUnits))
            FlairBadge(entity, rowHeight)
        }
        CellGrid(entity.topDetails, entity.bottomDetails, FeedRowMod.Cells)
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

/**
 * The entity's image at [url], scaled by [scale], over a backdrop of its color so an entity without one keeps its
 * shape.
 */
@Composable
private fun Thumbnail(
    entity: Entity,
    modifier: Modifier,
    url: Any? = entity.image?.let { it.thumb ?: it.url },
    scale: ContentScale = ContentScale.Crop,
) {
    val theme = currentTheme
    Box(modifier.background(theme.colors.colorOf(entity.toThemeColor()).copy(alpha = 0.2f))) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = scale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** The entity's flair, animated, in its color at low opacity, [size] square. */
@Composable
private fun FlairBadge(entity: Entity, size: Dp) {
    val theme = currentTheme
    AnimatedIcon(
        svg = entity.toFlair().animated,
        color = { theme.colors.colorOf(entity.toThemeColor()) },
        modifier = Modifier.opacityLow(),
        size = size,
    )
}

/**
 * The [details] the entity has on one line, joined by a dot and cut with an ellipsis: each in its own color when
 * [isColored], otherwise all in the ink.
 */
@Composable
private fun EntityDetails(details: List<EntityDetail>, isColored: Boolean, modifier: Modifier = Modifier) {
    val known = details.filter { it.text != null }
    if (known.isEmpty()) return
    val theme = currentTheme

    val text = buildAnnotatedString {
        known.forEachIndexed { index, detail ->
            if (index > 0) withStyle(SpanStyle(color = theme.colors.muted)) { append(" · ") }
            val color = if (isColored) theme.colors.colorOf(detail.color) else theme.colors.ink
            withStyle(SpanStyle(color = color)) { append(detail.text.orEmpty()) }
        }
    }
    BasicText(text, modifier, style = theme.text.small, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** The height of a row in units, the web's `--row-height`, which the thumbnail fills and the text is clipped at. */
private const val RowHeightUnits = 10

/** The space above and below a row's text, in units: the web's `--text-inset`. */
private const val TextInsetUnits = 0.5f

/** The width from which a row's cells sit beside it rather than below: the web's 960px container query. */
private val WideRowWidth = 960.dp

/** The shape of a grid entry's image: the web's `aspect-ratio: 3 / 2`. */
private const val GridImageRatio = 3f / 2f

/** The modifiers the feed rows share. */
private object FeedRowMod {
    val Base = Modifier.fillMaxWidth().zenBg()
    val Cells = Modifier.padding(horizontal = 1).borderRadius(2.5, moonShadow = true, outline = true).shineBg()
}
