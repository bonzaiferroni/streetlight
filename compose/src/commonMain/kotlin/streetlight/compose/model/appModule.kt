package streetlight.compose.model

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import streetlight.compose.io.AppTransport
import streetlight.model.io.ApiClient

/** The app's services and view models. */
val appModule = module {
    single { AppConfig(apiUrl = DEV_API_URL) }
    single { AppTransport(get<AppConfig>().apiUrl) }
    single { ApiClient(get<AppTransport>()) }
    singleOf(::ThemeHost)
    viewModelOf(::HomeModel)
}

/** The API of the server on this machine, where the app points while in development. */
private const val DEV_API_URL = "http://localhost:8080"
