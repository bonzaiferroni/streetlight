package streetlight.compose.model

import koala.model.Toaster
import kotlinx.coroutines.MainScope
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import streetlight.compose.io.AppTransport
import streetlight.model.io.ApiClient
import streetlight.compose.io.OsmClient
import streetlight.model.data.Account
import streetlight.model.data.EntityFeed
import streetlight.model.data.GalaxyContent
import streetlight.model.data.HomeContent
import streetlight.model.data.LocationContent
import org.koin.core.module.dsl.viewModel

/** The app's services and view models. */
val appModule = module {
    single { AppEnv(apiUrl = DEV_API_URL) }
    single { AppTransport(get<AppEnv>().apiUrl) }
    single { ApiClient(get<AppTransport>()) }
    singleOf(::ThemeHost)
    single { AppConfig(MainScope()) }
    single { Inflator(get()) }
    single { GeoCamera() }
    single { GeoMap(get()) }
    single { StarSession(get()) }
    single { OsmClient() }
    single { Toaster(MainScope()) }
    viewModel { (route: AppRoute) -> InflationModel(route, get()) }
    viewModel { (content: HomeContent) -> HomeModel(content, get(), get(), get()) }
    viewModel { (content: GalaxyContent) -> GalaxyModel(content, get(), get()) }
    viewModel { (page: EntityFeed) -> FeedPageModel(page, get(), get()) }
    viewModel { (content: LocationContent) -> LocationModel(content, get(), get()) }
    viewModel { EarthModel(get(), get()) }
    viewModel { (content: GalaxyContent) -> GalaxyMapModel(content, get()) }
    viewModel { (account: Account) -> AccountEditor(account, get(), get()) }
    viewModel { (target: PostGalaxy) -> LocationScout(target.galaxy, get(), get(), get(), get(), get()) }
    viewModel { (target: PostGalaxy) -> EventScout(target.galaxy, get(), get(), get(), get(), get()) }
}

/** The API of the server on this machine, where the app points while in development. */
private const val DEV_API_URL = "http://localhost:8080"
