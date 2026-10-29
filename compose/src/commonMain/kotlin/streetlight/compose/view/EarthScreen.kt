package streetlight.compose.view

import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.animateFloatAsState
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.hazeBlur
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import org.koin.compose.viewmodel.koinViewModel
import streetlight.compose.model.EarthModel
import streetlight.compose.model.EarthRoute
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/** The screen of [EarthRoute], which reads its content as the camera moves rather than inflating it. */
@Composable
fun EarthScreen() = context(koinViewModel<EarthModel>()) { EarthView() }

/**
 * The view of [EarthRoute]: the map across the whole page, with the events of each view it settles on, under its top
 * bar of the title and the [EarthHeader], and the [MapFocus] below it. The cousin of the web's `viewEarth`.
 */
@Composable
context(model: EarthModel)
fun EarthView() {
    PageThemeEffect(null)
    TopBarMount(TopBar.Title("Events"))
    MapMount(Modifier.fillMaxSize(), radius = 0, markerMap = model.markerMap, overlay = { EarthOverlay() })
}

/** The header below the top bar, then the focus in the space below it. */
@Composable
context(model: EarthModel)
private fun EarthOverlay() {
    val entities by model.focusEntitiesState.collectAsState()
    Column(0, Modifier.fillMaxSize().padding(top = currentPageInsets.calculateTopPadding())) {
        EarthHeader()
        MapFocus(entities, Modifier.fillMaxWidth().weight(1f))
    }
}

/**
 * The tag filter and the search, each over the blurred map, dimmed while the map moves. The cousin of the web's
 * `earthHeader` and `earthControls`.
 */
@Composable
context(model: EarthModel)
fun EarthHeader() {
    val theme = currentTheme
    val isMoving by model.isMovingState.collectAsState()
    val alpha by animateFloatAsState(if (isMoving) MovingAlpha else 1f, theme.magic.eased())

    Row(
        Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha }.padding(1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val blur = Modifier.hazeBlur(HazeInput.Sources(LocalMapHaze.current), MapBlur)
        TagFilter(model.tagState, backdrop = blur)
        Spacer(Modifier.weight(1f))
        SearchField(model.searchState, backdrop = blur)
    }
}

/** The opacity of the header while the map moves, as the web's `MoveDimmer`. */
private const val MovingAlpha = 0.6f
