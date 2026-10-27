package streetlight.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import streetlight.compose.io.buildImageLoader
import streetlight.compose.model.AppConfig
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.LocalPortal
import streetlight.compose.model.appModule
import streetlight.compose.model.rememberPortal
import streetlight.compose.view.AppPortal
import streetlight.compose.view.MapHost
import streetlight.compose.view.AppTheme

/** The root of the app, with a platform's [overlay] above its pages, such as the desktop's back button. */
@Composable
fun App(overlay: @Composable () -> Unit = {}) {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        val config = koinInject<AppConfig>()
        setSingletonImageLoaderFactory { context -> buildImageLoader(context, config) }

        AppTheme {
            CompositionLocalProvider(LocalPortal provides rememberPortal(HomeRoute)) {
                MapHost {
                    Box {
                        AppPortal()
                        overlay()
                    }
                }
            }
        }
    }
}
