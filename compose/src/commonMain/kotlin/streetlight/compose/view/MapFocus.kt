package streetlight.compose.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.zenBg
import streetlight.model.data.Entity
import streetlight.model.data.FeedContext

/**
 * The [entities] of the marker in focus, held to the bottom of the space it is given and to the web's 600px, the first
 * featured and the rest each a [FeedRow] in the `Row` mode, in a column that scrolls over the blurred map, fading in
 * and out with the focus. A narrow map leaves a sixth of the space to the map, as the web's `EarthStyle.Grid` does below
 * 800px; a wide one gives the focus its full height. The cousin of the web's `earthFocus`.
 */
@Composable
fun MapFocus(entities: List<Entity>?, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val magic = theme.magic
    // the last entities stay while the panel fades out
    var shown by remember { mutableStateOf(entities.orEmpty()) }
    entities?.let { shown = it }

    BoxWithConstraints(modifier, contentAlignment = Alignment.BottomStart) {
        val share = if (maxWidth < WideWidth) NarrowFocusShare else 1f
        AnimatedVisibility(
            entities != null,
            Modifier.widthIn(max = FocusWidth).heightIn(max = maxHeight * share),
            enter = fadeIn(magic.eased()),
            exit = fadeOut(magic.eased()),
        ) {
            Column(
                Modifier.borderRadius(0, moonShadow = true, outline = Outline.Mid)
                    .hazeBlur(HazeInput.Sources(LocalMapHaze.current), MapBlur)
                    .zenBg()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(theme.line),
            ) {
                shown.forEachIndexed { index, entity ->
                    context(null as FeedContext?) { FeedRow(entity, FeedMode.Row, isFeatured = index == 0) }
                }
            }
        }
    }
}

/**
 * The blur of the map behind a part of its overlay, the web's `--strong-blur` of 10px: CSS blurs to a deviation of its
 * value, and Haze to about 0.58 of its radius.
 */
val MapBlur = HazeBlurStyle { blurRadius(20.dp) }

/** The width below which the focus leaves part of the map's height to the map, the web's `EarthStyle.MinifiedWidth`. */
private val WideWidth = 800.dp

/** The widest the focus runs, as the web's. */
private val FocusWidth = 600.dp

/** The share of the space the focus may take on a narrow map, the web's `minmax(0, 5fr)` by `1fr`. */
private const val NarrowFocusShare = 5f / 6
