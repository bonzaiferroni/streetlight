package streetlight.compose.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
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
 * [EarthHeader]. The cousin of the web's `viewEarth`.
 */
@Composable
context(model: EarthModel)
fun EarthView() {
    PageThemeEffect(null)
    MapMount(Modifier.fillMaxSize(), radius = 0, markerMap = model.markerMap, overlay = { EarthHeader() })
}

/**
 * The map's title in a filigree on a band of paper, over the tag filter and the search, dimmed while the map moves.
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
            TagFilter(model.tagState)
            Spacer(Modifier.weight(1f))
            SearchField(model.searchState)
        }
    }
}

/** The height of the title's band in units, the web's `Height(8)`. */
private const val TitleHeightUnits = 8

/** The paper's opacity at the middle of the title's band, as the web's `EarthStyle.MapTitle`. */
private const val TitleBandAlpha = 0.6f

/** The opacity of the header while the map moves, as the web's `MoveDimmer`. */
private const val MovingAlpha = 0.6f
