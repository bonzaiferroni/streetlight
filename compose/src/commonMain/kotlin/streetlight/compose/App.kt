package streetlight.compose

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.appModule
import streetlight.compose.model.rememberPortal
import streetlight.compose.view.AppPortal

/** The root of the app. */
@Composable
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        context(rememberPortal(HomeRoute)) {
            AppPortal()
        }
    }
}
