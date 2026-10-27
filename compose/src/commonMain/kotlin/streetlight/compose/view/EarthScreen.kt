package streetlight.compose.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.zenBg
import streetlight.model.data.FeedContext
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import org.koin.compose.viewmodel.koinViewModel
import streetlight.compose.model.EarthModel
import streetlight.compose.model.EarthRoute
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/** The screen of [EarthRoute], which reads its content as the camera moves rather than inflating it. */
@Composable
fun EarthScreen() = context(koinViewModel<EarthModel>()) { EarthView() }

/**
 * The view of [EarthRoute]: the map across the whole page, with the events of each view it settles on, under the
 * [EarthHeader], and the [EarthFocus] below it. The cousin of the web's `viewEarth`.
 */
@Composable
context(model: EarthModel)
fun EarthView() {
    PageThemeEffect(null)
    MapMount(Modifier.fillMaxSize(), radius = 0, markerMap = model.markerMap, overlay = { EarthOverlay() })
}

/**
 * The header across the top of the map, then the focus in the space below it, held to its bottom and to the web's
 * 600px. A narrow map leaves a sixth of that space to the map, as the web's `EarthStyle.Grid` does below 800px; a wide
 * one gives the focus its full height.
 */
@Composable
context(model: EarthModel)
private fun EarthOverlay() {
    Column(0, Modifier.fillMaxSize()) {
        EarthHeader()
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.BottomStart) {
            val share = if (maxWidth < WideWidth) NarrowFocusShare else 1f
            EarthFocus(Modifier.widthIn(max = FocusWidth).heightIn(max = maxHeight * share))
        }
    }
}

/**
 * The entities of the marker in focus, the first featured and the rest each a [FeedRow] in the `Row` mode, in a column
 * that scrolls over the blurred map, fading in and out with the focus. The cousin of the web's `earthFocus`.
 */
@Composable
context(model: EarthModel)
fun EarthFocus(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val entities by model.focusEntitiesState.collectAsState()
    // the last entities stay while the panel fades out
    var shown by remember { mutableStateOf(entities.orEmpty()) }
    entities?.let { shown = it }

    val magic = theme.magic
    AnimatedVisibility(entities != null, modifier, enter = fadeIn(magic.eased()), exit = fadeOut(magic.eased())) {
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

/**
 * The map's title in a filigree on a band of paper, over the tag filter and the search, each over the blurred map,
 * dimmed while the map moves.
 * The cousin of the web's `earthHeader` and `earthControls`.
 */
@Composable
context(model: EarthModel)
fun EarthHeader() {
    val theme = currentTheme
    val isMoving by model.isMovingState.collectAsState()
    val alpha by animateFloatAsState(if (isMoving) MovingAlpha else 1f, theme.magic.eased())

    Column(0, Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha }) {
        Box(
            Modifier.fillMaxWidth().height(theme.unit * TitleHeightUnits).drawBehind {
                drawRect(
                    Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.5f to theme.colors.paper.copy(alpha = TitleBandAlpha),
                        1f to Color.Transparent,
                    )
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                "Events",
                Modifier.fillMaxWidth().filigree(),
                style = theme.text.heading3.copy(lineHeight = 1.em, textAlign = TextAlign.Center),
                color = { theme.colors.ink },
            )
        }
        Row(Modifier.fillMaxWidth().padding(1), verticalAlignment = Alignment.CenterVertically) {
            val blur = Modifier.hazeBlur(HazeInput.Sources(LocalMapHaze.current), MapBlur)
            TagFilter(model.tagState, backdrop = blur)
            Spacer(Modifier.weight(1f))
            SearchField(model.searchState, backdrop = blur)
        }
    }
}

/** The width below which the focus leaves part of the map's height to the map, the web's `EarthStyle.MinifiedWidth`. */
private val WideWidth = 800.dp

/** The widest the focus runs, as the web's. */
private val FocusWidth = 600.dp

/** The share of the space below the header the focus may take on a narrow map, the web's `minmax(0, 5fr)` by `1fr`. */
private const val NarrowFocusShare = 5f / 6

/**
 * The blur of the map behind the focus and the controls, the web's `--strong-blur` of 10px: CSS blurs to a deviation of
 * its value, and Haze to about 0.58 of its radius.
 */
private val MapBlur = HazeBlurStyle { blurRadius(20.dp) }

/** The height of the title's band in units, the web's `Height(8)`. */
private const val TitleHeightUnits = 8

/** The paper's opacity at the middle of the title's band, as the web's `EarthStyle.MapTitle`. */
private const val TitleBandAlpha = 0.6f

/** The opacity of the header while the map moves, as the web's `MoveDimmer`. */
private const val MovingAlpha = 0.6f
