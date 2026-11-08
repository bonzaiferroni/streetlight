package streetlight.web.model

import koala.model.Toaster
import kampfire.model.Messenger
import koala.utility.LaunchTelemetry
import koala.core.appExceptionHandler
import koala.dom.AppContainer
import koala.model.ContentFetcher
import koala.model.GeoCamera
import koala.model.GeoMap
import koala.model.Portal
import koala.model.RouteInflator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus
import org.koin.dsl.bind
import org.koin.dsl.module
import streetlight.model.ui.EarthMap
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.Screen
import streetlight.model.io.ApiClient
import streetlight.web.io.WebTransport
import streetlight.web.io.OSMClient
import streetlight.web.io.OmniClient
import streetlight.web.io.TransitClient

/** The web client's services, one of each, created on first use. */
val webModule = module {
    single { MainScope() + appExceptionHandler + LaunchTelemetry("App") }
    single { SiteConfig(get()) }
    single { CredentialStore() }
    single { WebTransport() }

    // clients
    single { TransitClient(get()) }
    single { ApiClient(get<WebTransport>()) }
    single { OSMClient() }

    single { DataCache(get()) }
    single { Toaster(get()) } bind Messenger::class
    single { Portal(HomeRoute(), Screen.entries) }
    single { RouteDock(get(), get())}
    single<ContentFetcher> { AppContentFetcher(get()) }
    single { RouteInflator(get(), get(), get(), get()) }
    single { SessionClient(get(), get(), get(), get(), get()) }
    // single { GateAgent(get(), get(), get()) }
    single { GeoCamera(get()) }
    single { GeoMap(get()) }
    single { TransitMap(get(), get(), get(), get()) }
    single(createdAtStart = true) { StarLocator(get()) }
    single { ChatRoom(get(), get()) }
    single { OmniClient(get(), get()) }
}

fun AppContainer.getUserCreator(scope: CoroutineScope) =
    UserCreator(scope, koin.get(), koin.get(), koin.get(), koin.get())

fun AppContainer.getEarthMap(scope: CoroutineScope, initialMap: EarthMap?) =
    Earth(scope, initialMap, koin.get(), koin.get(), koin.get(), koin.get(), koin.get())
