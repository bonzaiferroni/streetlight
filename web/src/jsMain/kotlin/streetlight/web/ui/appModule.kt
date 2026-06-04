package streetlight.web.ui

import koala.dom.AppContext
import koala.model.GeoMap
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.koin.dsl.module
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.data.PostEdit
import streetlight.model.data.GalaxyEdit
import streetlight.web.HomeRoute
import streetlight.web.StreetlightScreen
import streetlight.web.io.ApiClient
import streetlight.web.io.FetchClient
import streetlight.web.io.OSMClient
import streetlight.web.io.OmniLog
import streetlight.web.io.TransitClient
import streetlight.web.model.*

val appModule = module {
    single { MainScope() }
    single { SiteConfig() }
    single { CredentialStore() }
    single { FetchClient(get()) }

    // clients
    single { TransitClient(get()) }
    single { ApiClient(get()) }
    single { LightService(get(), get()) }
    single { OSMClient() }

    single { UserGate(get(), get(), get(), get()) }
    single { DataCache(get(), get(), get(), get(), get()) }
    single { Portal(HomeRoute, StreetlightScreen.entries, get()) }
    single { GateAgent(get(), get(), get()) }
    single { GeoMap(get()) }
    single { TransitMap(get(), get(), get(), get()) }
    single { MarkerMap(get(), get(), get()) }
    single { ChatRoom(get(), get()) }
    single { OmniLog(get(), get()) }
    single { MarkerService() }

    single { Toaster(get()) }
}

fun AppContext.getUserCreator(scope: CoroutineScope) =
    UserCreator(scope, koin.get(), koin.get(), koin.get(), koin.get())

fun AppContext.getEarthMap(scope: CoroutineScope) =
    EarthMap(scope, koin.get(), koin.get(), koin.get(), koin.get(), koin.get())

fun AppContext.getLocationEditor(edit: LocationEdit, scope: CoroutineScope) =
    LocationEditor(edit, scope, koin.get())

fun AppContext.getContentEditor(edit: PostEdit, scope: CoroutineScope) =
    PostEditor(edit, scope, koin.get(), koin.get())

fun AppContext.getLocationScout(galaxy: Galaxy, editor: LocationEditor, scope: CoroutineScope) =
    LocationScout(galaxy, editor, scope, koin.get(), koin.get(), koin.get(), koin.get())

fun AppContext.getEventEditor(edit: EventEdit, scope: CoroutineScope) =
    EventEditor(edit, scope, koin.get())

fun AppContext.getEventScout(galaxy: Galaxy, editor: EventEditor, location: LocationScout, scope: CoroutineScope) =
    EventScout(galaxy, editor, location, scope, koin.get())

fun AppContext.getGalaxyEditor(galaxy: GalaxyEdit, scope: CoroutineScope) =
    GalaxyEditor(galaxy, scope, koin.get(), koin.get(), koin.get(), koin.get())