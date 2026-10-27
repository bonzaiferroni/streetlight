package streetlight.compose.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import streetlight.compose.model.EarthModel
import streetlight.compose.model.EarthRoute

/** The screen of [EarthRoute], which reads its content as the camera moves rather than inflating it. */
@Composable
fun EarthScreen() = context(koinViewModel<EarthModel>()) { EarthView() }

/**
 * The view of [EarthRoute]: the map across the whole page, with the events of each view it settles on. The cousin of
 * the web's `viewEarth`.
 */
@Composable
context(model: EarthModel)
fun EarthView() {
    PageThemeEffect(null)
    MapMount(Modifier.fillMaxSize(), radius = 0, markerMap = model.markerMap)
}
