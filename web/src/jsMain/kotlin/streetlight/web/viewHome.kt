package streetlight.web

import koala.dom.RenderContext

fun RenderContext.viewHome(app: AppContext) {
    console.log("loading home")

    homeContent(app.portal)

    val maplibre = maplibregl.Map(jsObject {
        container = "geo-map"
        style = "https://tiles.openfreemap.org/styles/fiord"
        center = maplibregl.LngLat(-104.95, 39.75)
        zoom = 11
    })

    maplibre.addControl(maplibregl.NavigationControl())
    maplibre.addControl(maplibregl.FullscreenControl())

    app.attachMapWindow(maplibre)
    app.attachGtfsMap(maplibre)
    app.viewMapPanel()
}