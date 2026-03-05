package streetlight.web.ui

import streetlight.web.model.AppContext

interface ViewModel {
    val app: AppContext

    val api get() = app.client.api
    val geoMap get() = app.geoMap
}