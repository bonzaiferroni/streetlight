package streetlight.compose.model

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import streetlight.compose.io.AppTransport
import streetlight.model.io.ApiClient
import streetlight.model.data.GalaxyContent
import streetlight.model.data.HomeContent
import org.koin.core.module.dsl.viewModel

/** The app's services and view models. */
val appModule = module {
    single { AppConfig(apiUrl = DEV_API_URL) }
    single { AppTransport(get<AppConfig>().apiUrl) }
    single { ApiClient(get<AppTransport>()) }
    singleOf(::ThemeHost)
    singleOf(::AppSettings)
    single { Inflator(get()) }
    viewModel { (route: AppRoute) -> InflationModel(route, get()) }
    viewModel { (content: HomeContent) -> HomeModel(content, get(), get()) }
    viewModel { (content: GalaxyContent) -> GalaxyModel(content, get(), get()) }
}

/** The API of the server on this machine, where the app points while in development. */
private const val DEV_API_URL = "http://localhost:8080"
