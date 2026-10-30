package streetlight.web.model

import koala.dom.AppFacade
import koala.model.GeoCamera
import koala.model.Portal
import streetlight.model.io.ApiClient
import koala.model.GeoMap
import streetlight.web.model.RouteDock
import koala.model.Toaster
import streetlight.web.model.SessionClient

val AppFacade.api get() = app.get<ApiClient>()
val AppFacade.portal get() = app.get<Portal>()
val AppFacade.toaster get() = app.get<Toaster>()
val AppFacade.session get() = app.get<SessionClient>()
val AppFacade.geoMap get() = app.get<GeoMap>()
val AppFacade.geoCamera get() = app.get<GeoCamera>()
val AppFacade.dock get() = app.get<RouteDock>()
