package streetlight.web.pages

import koala.html.head
import koala.html.styles
import kotlinx.html.HTML
import streetlight.model.data.Event
import streetlight.web.shells.eventProfileShell

fun HTML.eventPage(event: Event) {
    head("Streetlight | ${event.title}") {
        supportProtobuf()
        supportGeoMap()
    }
    portalBody {
        eventProfileShell(event)
    }
}