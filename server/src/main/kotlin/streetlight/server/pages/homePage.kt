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
                    column {
                        events.forEach { event ->
                            a {
                                href = "/event-portal/${event.eventId.value}"
                                card {
                                    label(event.title)
                                }
                            }
                        }
                    }
                }
                tab("Map") {
                    paragraph("Hello map")
                }
                tab("App") {
                    paragraph("Hello app")
                }
            }
        }
    }
}