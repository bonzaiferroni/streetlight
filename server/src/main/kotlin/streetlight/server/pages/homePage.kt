package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*
import streetlight.model.data.*

fun HTML.homePage(events: List<Event>) {
    head("Streetlight | Home") {
        styles("homePage.css")
        scripts("homePage.js")
        geoMapResources()
        script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") { }
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
                    column(AlignItemsCenter) {
                        geoMap()
                        homeFooter()
                    }
                }
                tab("App") {
                    appTab()
                }
            }
        }
    }
}

fun FlowContent.homeFooter() {
    val giants = "May we choose a world of good and faithful giants. "
    row(AlignItemsCenter) {
        style = "height: 20rem;"
        column(AlignItemsCenter, NoGap, FillWidth) {
            lottie("spinning_circles") {
                style = "height: 10rem;"
            }
            paragraph(giants, Italic, Dim)
        }
    }
}