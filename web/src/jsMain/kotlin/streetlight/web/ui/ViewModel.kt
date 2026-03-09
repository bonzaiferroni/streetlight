package streetlight.web.ui

import streetlight.web.model.AppContext

interface ViewModel {
    val app: AppContext

    val api get() = app.client.api
    val geo get() = app.geoMap
    val osm get() = app.client.location
    val portal get() = app.portal
}