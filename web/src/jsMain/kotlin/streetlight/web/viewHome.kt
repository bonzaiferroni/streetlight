package streetlight.web

import koala.html.*
import kotlinx.html.div
import streetlight.web.viewMapPanel

fun RenderContext.viewHome() {

    div {
        tabs {
            tab("Events") {
                // eventsTab(events)
            }
            tab("Map") {
                column(AlignItemsCenter) {
//                    geoMap()
//                    val maplibre = maplibregl.Map(jsObject {
//                        container = "geo-map"
//                        style = "https://tiles.openfreemap.org/styles/fiord"
//                        center = maplibregl.LngLat(-104.95, 39.75)
//                        zoom = 11
//                    })

//                    viewMapWindow(maplibre)
//                    viewGtfsMap(maplibre)
//                    viewMapPanel()
                    // homeFooter()
                }
            }
            tab("App") {
                // appTab()
            }
        }
    }
}