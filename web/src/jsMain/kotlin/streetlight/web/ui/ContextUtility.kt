package streetlight.web.ui

import koala.dom.RenderScope
import koala.model.GeoCamera
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.model.MarkerMap
import streetlight.web.model.Toaster
import streetlight.web.model.UserGate

val RenderScope.api get() = app.get<ApiClient>()
val RenderScope.portal get() = app.get<Portal>()
val RenderScope.appScope get() = app.get<CoroutineScope>()
val RenderScope.toaster get() = app.get<Toaster>()
val RenderScope.gate get() = app.get<UserGate>()
val RenderScope.markerMap get() = app.get<MarkerMap>()
val RenderScope.geoMap get() = app.get<GeoCamera>()
