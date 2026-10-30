package streetlight.web.subApp

import kampfire.model.Messenger
import koala.core.appExceptionHandler
import koala.model.ContentFetcher
import koala.model.GeoCamera
import koala.model.GeoMap
import koala.model.Portal
import koala.model.RouteInflator
import koala.utility.LaunchTelemetry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus
import org.koin.dsl.bind
import org.koin.dsl.module
import streetlight.model.ui.LocationAppRoute
import streetlight.model.ui.LocationAppScreen
import streetlight.model.io.ApiClient
import streetlight.web.io.WebTransport
import koala.model.Toaster

/** The services of a LocationApp, one of each, created on first use. */
val locationAppModule = module {
    single { MainScope() + appExceptionHandler + LaunchTelemetry("LocationApp") }
    single { WebTransport() }
    single { ApiClient(get<WebTransport>()) }

    single { Toaster(get()) } bind Messenger::class
    single { Portal(LocationAppRoute, LocationAppScreen.portalScreens) }
    single<ContentFetcher> { LocationAppFetcher(get()) }
    single { RouteInflator(get(), get(), get(), get()) }
    single { GeoCamera(get()) }
    single { GeoMap(get()) }
}
