package streetlight.web.ui

import streetlight.web.model.Streetlight

interface ViewModel {
    val app: Streetlight

    val api get() = app.client.api
    val geo get() = app.geoMap
    val osm get() = app.client.location
    val portal get() = app.portal
    val stage get() = app.stage
}