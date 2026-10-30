package streetlight.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import streetlight.compose.view.LocalTopBarHost
import streetlight.compose.view.TopBarHost
import streetlight.compose.view.DialogHost
import streetlight.compose.view.DialogHostState
import streetlight.compose.view.LocalDialogHost
import streetlight.compose.view.TopBarHostState
import androidx.compose.runtime.LaunchedEffect
import streetlight.compose.model.StarSession
import androidx.compose.runtime.CompositionLocalProvider
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import streetlight.compose.io.buildImageLoader
import streetlight.compose.model.AppEnv
import streetlight.compose.model.LocalPortal
import streetlight.compose.model.Portal
import streetlight.compose.model.appModule
import streetlight.compose.model.rememberPortal
import streetlight.compose.view.AppPortal
import streetlight.compose.view.MapHost
import streetlight.compose.view.AppTheme
import streetlight.compose.view.AppScaffold
import streetlight.compose.view.RouteDock
import streetlight.compose.view.ToasterHost
import streetlight.compose.view.HelmBar
import streetlight.compose.modifier.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The root of the app, moving through [portal], below a platform's [bar], such as the desktop window's title bar. A
 * platform that answers back on its own, as the desktop window does on its keys, makes the portal and passes it in.
 */
@Composable
fun App(portal: Portal = rememberPortal(), bar: @Composable () -> Unit = {}) {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        val env = koinInject<AppEnv>()
        setSingletonImageLoaderFactory { context -> buildImageLoader(context, env) }
        val session = koinInject<StarSession>()
        LaunchedEffect(session) { session.restore() }

        val topBarHost = remember { TopBarHostState() }
        val dialogHost = remember { DialogHostState() }
        CompositionLocalProvider(
            LocalPortal provides portal,
            LocalTopBarHost provides topBarHost,
            LocalDialogHost provides dialogHost,
        ) {
            AppTheme {
                AppScaffold(bar = bar, overlay = {
                    TopBarHost(Modifier.align(Alignment.TopCenter))
                    RouteDock(Modifier.align(Alignment.BottomCenter).padding(bottom = 2))
                    ToasterHost(Modifier.align(Alignment.BottomStart))
                    // the bar's menus open over the dock, and a dialog over everything
                    HelmBar()
                    DialogHost()
                }) {
                    MapHost { AppPortal() }
                }
            }
        }
    }
}
