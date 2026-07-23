package streetlight.web.ui

import kampfire.model.Messenger
import koala.utils.LaunchTelemetry
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
import streetlight.model.data.Account
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.MediaEdit
import streetlight.model.data.StarEdit
import streetlight.model.ui.EarthMap
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.Screen
import streetlight.web.io.ApiClient
import streetlight.web.io.FetchClient
import streetlight.web.io.OSMClient
import streetlight.web.io.OmniLog
import streetlight.web.io.TransitClient
import streetlight.web.model.*

val appModule = module {
    single { MainScope() + appExceptionHandler + LaunchTelemetry("App") }
    single { SiteConfig() }
    single { CredentialStore() }
    single { FetchClient() }

    // clients
    single { TransitClient(get()) }
    single { ApiClient(get()) }
    single { LightService(get(), get()) }
    single { OSMClient() }

    single { StarSession(get(), get()) }
    single { DataCache(get(), get(), get(), get(), get()) }
    single { Portal(HomeRoute, Screen.entries) }
    // single { GateAgent(get(), get(), get()) }
    single { GeoCamera(get()) }
    single { GeoMap(get(), get()) }
    single { TransitMap(get(), get(), get(), get()) }
    single { MarkerMap(get(), get(), get()) }
    single { ChatRoom(get(), get()) }
    single { OmniLog(get(), get()) }
    single { MarkerService() }
    single<ContentFetcher> { AppContentFetcher(get()) }
    single { RouteInflator(get(), get(), get(), get()) }

    single { Toaster(get()) } bind Messenger::class
}

fun AppContainer.getUserCreator(scope: CoroutineScope) =
    UserCreator(scope, koin.get(), koin.get(), koin.get(), koin.get())

fun AppContainer.getEarthMap(scope: CoroutineScope, initialMap: EarthMap) =
    Earth(scope, initialMap, koin.get(), koin.get(), koin.get(), koin.get(), koin.get())

fun AppContainer.getLocationEditor(edit: LocationEdit, scope: CoroutineScope) =
    LocationEditor(edit, scope, koin.get())

fun AppContainer.getMediaEditor(edit: MediaEdit, scope: CoroutineScope) =
    MediaEditor(edit, scope, koin.get(), koin.get())

fun AppContainer.getLocationScout(galaxy: Galaxy, editor: LocationEditor, scope: CoroutineScope) =
    LocationScout(galaxy, editor, scope, koin.get(), koin.get(), koin.get(), koin.get())

fun AppContainer.getEventEditor(edit: EventEdit, scope: CoroutineScope) =
    EventEditor(edit, scope, koin.get())

fun AppContainer.getEventScout(galaxy: Galaxy, editor: EventEditor, location: LocationScout, scope: CoroutineScope) =
    EventScout(galaxy, editor, location, scope, koin.get())

fun AppContainer.getGalaxyEditor(galaxy: GalaxyEdit, scope: CoroutineScope) =
    GalaxyEditor(galaxy, scope, koin.get(), koin.get(), koin.get(), koin.get())

fun AppContainer.getFeedbackDesk(scope: CoroutineScope) =
    FrontDesk(scope, koin.get(), koin.get())

fun AppContainer.getSiteMonitor(scope: CoroutineScope) = SiteMonitor(scope, koin.get(), koin.get())

fun AppContainer.getAccountEditor(edit: Account, scope: CoroutineScope) =
    AccountEditor(edit, scope, koin.get(), koin.get())

fun AppContainer.getProfileEditor(edit: StarEdit, scope: CoroutineScope) =
    ProfileEditor(edit, scope, koin.get(), koin.get())