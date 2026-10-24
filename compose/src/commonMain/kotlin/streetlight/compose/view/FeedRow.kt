package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import streetlight.compose.model.bottomDetails
import streetlight.compose.model.toFlair
import streetlight.compose.model.toThemeColor
import streetlight.compose.model.topDetails
import streetlight.model.data.Entity

/** The layouts of a feed row, the cousin of the web's `FeedMode`. `Row` and `Grid` draw as `Minimal` until they are built. */
enum class FeedMode { Minimal, Row, Grid }

/** An entity in a feed, laid out by [mode]. The cousin of the web's `feedRow`. */
@Composable
fun FeedRow(entity: Entity, mode: FeedMode = FeedMode.Minimal) {
    when (mode) {
        FeedMode.Minimal, FeedMode.Row, FeedMode.Grid -> MinimalFeedRow(entity)
    }
}

/**
 * The thumbnail beside a column of the entity's top details, its title on up to two lines, and its bottom details,
 * then the flair badge, as tall as the thumbnail, in the entity's color at low opacity.
 *
 * The row has a unit of padding and its parts are a unit apart.
 */
@Composable
private fun MinimalFeedRow(entity: Entity) {
    val theme = currentTheme
    val rowHeight = theme.unit * RowHeightUnits

    Row(
        modifier = Modifier.fillMaxWidth().zenBg().padding(theme.unit),
        horizontalArrangement = Arrangement.spacedBy(theme.unit),
    ) {
        Box(
            Modifier.size(rowHeight)
                .borderRadius(1)
                .background(theme.colors.colorOf(entity.toThemeColor()).copy(alpha = 0.2f))
        ) {
            AsyncImage(
                model = entity.image?.let { it.thumb ?: it.url },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(rowHeight),
            )
        }
        Column(Modifier.weight(1f)) {
            EntityDetails(entity.topDetails)
            BasicText(
                text = entity.label,
                style = theme.text.regular.copy(lineHeight = 1.15.em),
                color = { theme.colors.ink },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            EntityDetails(entity.bottomDetails)
        }
        AnimatedIcon(
            svg = entity.toFlair().animated,
            color = { theme.colors.colorOf(entity.toThemeColor()) },
            modifier = Modifier.opacityLow(),
            size = rowHeight,
        )
    }
}

/** [details] on one line, each in its color, joined by a dot and cut with an ellipsis. */
@Composable
private fun EntityDetails(details: List<EntityDetail>) {
    if (details.isEmpty()) return
    val theme = currentTheme

    val text = buildAnnotatedString {
        details.forEachIndexed { index, detail ->
            if (index > 0) withStyle(SpanStyle(color = theme.colors.muted)) { append(" · ") }
            withStyle(SpanStyle(color = theme.colors.colorOf(detail.color))) { append(detail.text) }
        }
    }
    BasicText(text, style = theme.text.small, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** The height of a row in units, the web's `--row-height`, which the thumbnail fills. */
private const val RowHeightUnits = 10
