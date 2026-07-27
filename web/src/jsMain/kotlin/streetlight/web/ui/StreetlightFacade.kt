package streetlight.web.ui

import koala.dom.AppFacade
import koala.model.GeoCamera
import koala.model.Portal
import streetlight.web.io.ApiClient
import streetlight.web.model.MarkerMap
import streetlight.web.model.Toaster
import streetlight.web.model.SessionGate

val AppFacade.api get() = app.get<ApiClient>()
val AppFacade.portal get() = app.get<Portal>()
val AppFacade.toaster get() = app.get<Toaster>()
val AppFacade.session get() = app.get<SessionGate>()
val AppFacade.markerMap get() = app.get<MarkerMap>()
val AppFacade.geoCamera get() = app.get<GeoCamera>()
