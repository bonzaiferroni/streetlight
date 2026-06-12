package streetlight.web.ui

import koala.dom.AppScope
import koala.model.GeoCamera
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.model.MarkerMap
import streetlight.web.model.Toaster
import streetlight.web.model.UserGate

val AppScope.api get() = app.get<ApiClient>()
val AppScope.portal get() = app.get<Portal>()
val AppScope.appScope get() = app.get<CoroutineScope>()
val AppScope.toaster get() = app.get<Toaster>()
val AppScope.gate get() = app.get<UserGate>()
val AppScope.markerMap get() = app.get<MarkerMap>()
val AppScope.geoMap get() = app.get<GeoCamera>()
