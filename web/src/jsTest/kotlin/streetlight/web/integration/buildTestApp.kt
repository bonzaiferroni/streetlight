package streetlight.web.integration

import koala.dom.AppContainer
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.Screen
import streetlight.web.io.ApiClient
import streetlight.web.model.Toaster

fun buildTestApp(
    scope: CoroutineScope,
    api: ApiClient,
): AppContainer {
    val koin = koinApplication {
        modules(module {
            single { scope }
            single { api }
            single { Toaster(get()) }
            single { Portal(HomeRoute, Screen.entries) }
        })
    }.koin

    return AppContainer(koin)
}
