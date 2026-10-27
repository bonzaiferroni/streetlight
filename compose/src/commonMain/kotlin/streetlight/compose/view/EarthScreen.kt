package streetlight.compose.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import streetlight.compose.model.EarthRoute

/** The screen of [EarthRoute], which reads its content as the camera moves rather than inflating it. */
@Composable
fun EarthScreen() = EarthView()

/** The view of [EarthRoute]: the map across the whole page. The cousin of the web's `viewEarth`. */
@Composable
fun EarthView() {
    PageThemeEffect(null)
    MapMount(Modifier.fillMaxSize(), radius = 0)
}
