package streetlight.web.integration

import koala.dom.AppContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.koinApplication
import org.koin.dsl.module
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
        })
    }.koin

    return AppContainer(koin)
}
