package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*
import streetlight.model.data.*

fun HTML.homePage(events: List<Event>) {
    head("Streetlight | Home") {
        // styles("home.css")
        // scripts("home.js")
    }
    body {
        column {
            column(FillWidth, AlignItemsCenter, NoGap) {
                row {
                    heading1("Streetlight")
                    logo()
                }
                box(Dim) {
                    +"a "
                    span {
                        modify(NoDim, Glow)
                        +"Colfax"
                    }
                    +" music community"
                }
            }
            tabs {
                tab("Events") {
                    eventsTab(events)
                }
                tab("Map") {
                    geoMap()
                    paragraph("Hello map")
                }
                tab("App") {
                    appTab()
                }
            }
        }
    }
}