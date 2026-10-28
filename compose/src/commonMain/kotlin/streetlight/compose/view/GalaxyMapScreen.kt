package streetlight.compose.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import streetlight.compose.modifier.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.GalaxyMapModel
import streetlight.compose.model.GalaxyMapRoute
import streetlight.compose.utility.collectAsState
import streetlight.model.data.GalaxyContent

/** The screen of [route]: its galaxy once inflated, shown in the [GalaxyMapView]. */
@Composable
fun GalaxyMapScreen(route: GalaxyMapRoute) = Inflated<GalaxyContent>(route) { content ->
    context(koinViewModel<GalaxyMapModel> { parametersOf(content) }) { GalaxyMapView() }
}

/**
 * The view of [GalaxyMapRoute]: the map across the whole page, framing the markers of the galaxy's feed,
 * under its top bar of the galaxy's name, and the [MapFocus] below it, in the galaxy's own theme.
 */
@Composable
context(model: GalaxyMapModel)
fun GalaxyMapView() {
    val galaxy = model.content.galaxy
    PageThemeEffect(galaxy.design?.theme)
    TopBarMount(TopBar.Title(galaxy.name))
    MapMount(
        Modifier.fillMaxSize(),
        center = model.center,
        area = model.area,
        radius = 0,
        markerMap = model.markerMap,
        overlay = { GalaxyMapOverlay() },
    )
}

/** The focus in the space below the top bar. */
@Composable
context(model: GalaxyMapModel)
private fun GalaxyMapOverlay() {
    val entities by model.focusEntitiesState.collectAsState()
    Column(0, Modifier.fillMaxSize().padding(top = TopBarUnits)) {
        MapFocus(entities, Modifier.fillMaxWidth().weight(1f))
    }
}
