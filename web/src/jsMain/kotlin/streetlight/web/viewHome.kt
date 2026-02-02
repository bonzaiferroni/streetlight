package streetlight.web

import koala.dom.RenderContext
import koala.html.MapId

fun RenderContext.viewHome(app: AppContext) {
    console.log("loading home")

    homeContent(app.portal)

    val maplibre = maplibregl.Map(jsObject {
        container = MapId.widget
        style = "https://tiles.openfreemap.org/styles/fiord"
        center = maplibregl.LngLat(-104.95, 39.75)
        zoom = 11
    })

    maplibre.addControl(maplibregl.NavigationControl())
    maplibre.addControl(maplibregl.FullscreenControl())

     viewMapWindow(maplibre, app)
     app.attachGtfsMap(maplibre)
     viewMapPanel(app)
}