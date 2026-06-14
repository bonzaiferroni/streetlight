package streetlight.web.ui

import koala.dom.AppFacade
import koala.dom.AppScope
import koala.model.GeoCamera
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.model.MarkerMap
import streetlight.web.model.Toaster
import streetlight.web.model.UserGate

val AppFacade.api get() = app.get<ApiClient>()
val AppFacade.portal get() = app.get<Portal>()
val AppFacade.appScope get() = app.get<CoroutineScope>()
val AppFacade.toaster get() = app.get<Toaster>()
val AppFacade.gate get() = app.get<UserGate>()
val AppFacade.markerMap get() = app.get<MarkerMap>()
val AppFacade.geoMap get() = app.get<GeoCamera>()
