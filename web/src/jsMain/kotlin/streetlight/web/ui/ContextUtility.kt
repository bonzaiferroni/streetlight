package streetlight.web.ui

import koala.dom.DOMRender
import koala.model.GeoCamera
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.model.MarkerMap
import streetlight.web.model.Toaster
import streetlight.web.model.UserGate

val DOMRender.api get() = app.get<ApiClient>()
val DOMRender.portal get() = app.get<Portal>()
val DOMRender.appScope get() = app.get<CoroutineScope>()
val DOMRender.toaster get() = app.get<Toaster>()
val DOMRender.gate get() = app.get<UserGate>()
val DOMRender.markerMap get() = app.get<MarkerMap>()
val DOMRender.geoMap get() = app.get<GeoCamera>()
