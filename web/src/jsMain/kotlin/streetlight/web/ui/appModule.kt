package streetlight.web.ui

import koala.model.GeoMap
import koala.model.Portal
import kotlinx.coroutines.MainScope
import org.koin.dsl.module
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
    single { OSMClient() }

    single { UserGate(get(), get(), get(), get()) }
    single { DataCache(get(), get(), get(), get(), get()) }
    single { Portal(HomeRoute, StreetlightScreen.entries, get()) }
    single { GateAgent(get(), get(), get()) }
    single { GeoMap(get()) }
    single { TransitMap(get(), get(), get(), get()) }
    single { StreetMap(get(), get(), get()) }
    single { ChatRoom(get(), get()) }
    single { OmniLog(get(), get()) }

    single { Toaster(get()) }

    factory { UserCreator(it.get(), get(), get(), get(), get()) }
    factory { EarthMap(it.get(), get(), get(), get())}
    factory { EventEditor(it[0], it[1], get())}
    factory { GalaxyEditor(it[0], it[1], get(), get(), get(), get())}
    factory { LocationEditor(it[0], it[1], get(), get()) }
    factory { LocationFinderProto2(it[0], get(), get(), get()) }
    factory { ContentEditor(it[0], it[1], get(), get()) }
    factory { EventScout(it[0], it[1])}
    factory { LocationScout(it[0], it[1], it[2], get(), get(), get(), get()) }
}