package streetlight.web

fun RenderContext.viewHome() {
    console.log("loading home")

    homeContent()

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