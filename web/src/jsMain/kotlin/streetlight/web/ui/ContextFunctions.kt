package streetlight.web.ui

import koala.dom.RenderContext
import koala.model.GeoMap
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.model.GalaxyStage
import streetlight.web.model.StreetMap
import streetlight.web.model.Toaster
import streetlight.web.model.UserGate

val RenderContext.api get() = app.get<ApiClient>()
val RenderContext.portal get() = app.get<Portal>()
val RenderContext.appScope get() = app.get<CoroutineScope>()
val RenderContext.toaster get() = app.get<Toaster>()
val RenderContext.gate get() = app.get<UserGate>()
val RenderContext.stage get() = app.get<GalaxyStage>()
val RenderContext.streetMap get() = app.get<StreetMap>()
val RenderContext.geoMap get() = app.get<GeoMap>()
