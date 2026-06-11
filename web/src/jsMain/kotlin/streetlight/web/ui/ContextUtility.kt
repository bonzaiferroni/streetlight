package streetlight.web.ui

import koala.dom.ScopedDOM
import koala.model.GeoCamera
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.model.MarkerMap
import streetlight.web.model.Toaster
import streetlight.web.model.UserGate

val ScopedDOM.api get() = app.get<ApiClient>()
val ScopedDOM.portal get() = app.get<Portal>()
val ScopedDOM.appScope get() = app.get<CoroutineScope>()
val ScopedDOM.toaster get() = app.get<Toaster>()
val ScopedDOM.gate get() = app.get<UserGate>()
val ScopedDOM.markerMap get() = app.get<MarkerMap>()
val ScopedDOM.geoMap get() = app.get<GeoCamera>()
