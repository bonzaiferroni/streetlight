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
            column(FillWidth, AlignItemsCenter) {
                row {
                    heading1("Streetlight")
                    logo()
                }
            }
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
}