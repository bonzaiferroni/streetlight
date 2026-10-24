package streetlight.compose

import androidx.compose.runtime.Composable
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import streetlight.compose.io.buildImageLoader
import streetlight.compose.model.AppConfig
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.appModule
import streetlight.compose.model.rememberPortal
import streetlight.compose.view.AppPortal
import streetlight.compose.view.AppTheme

/** The root of the app. */
@Composable
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        val config = koinInject<AppConfig>()
        setSingletonImageLoaderFactory { context -> buildImageLoader(context, config) }

        AppTheme {
            context(rememberPortal(HomeRoute)) {
                AppPortal()
            }
        }
    }
}
