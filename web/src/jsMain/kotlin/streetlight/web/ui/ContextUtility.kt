package streetlight.web.ui

import koala.dom.RenderContext
import koala.model.GeoCamera
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.model.MarkerMap
import streetlight.web.model.Toaster
import streetlight.web.model.UserGate

val RenderContext.api get() = app.get<ApiClient>()
val RenderContext.portal get() = app.get<Portal>()
val RenderContext.appScope get() = app.get<CoroutineScope>()
val RenderContext.toaster get() = app.get<Toaster>()
val RenderContext.gate get() = app.get<UserGate>()
val RenderContext.markerMap get() = app.get<MarkerMap>()
val RenderContext.geoMap get() = app.get<GeoCamera>()
