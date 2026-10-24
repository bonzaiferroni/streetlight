package streetlight.compose

import androidx.compose.runtime.Composable
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.rememberPortal
import streetlight.compose.view.AppPortal

/** The root of the app. */
@Composable
fun App() {
    context(rememberPortal(HomeRoute)) {
        AppPortal()
    }
}
