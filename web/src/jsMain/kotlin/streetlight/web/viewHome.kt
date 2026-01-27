package streetlight.web

fun AppContext.viewHome() {
    val maplibre = maplibregl.Map(jsObject {
        container = "geo-map"
        style = "https://tiles.openfreemap.org/styles/fiord"
        center = maplibregl.LngLat(-104.95, 39.75)
        zoom = 11
    })

    maplibre.addControl(maplibregl.NavigationControl())
    maplibre.addControl(maplibregl.FullscreenControl())

    attachMapWindow(maplibre)
    attachGtfsMap(maplibre)
    viewMapPanel()
}