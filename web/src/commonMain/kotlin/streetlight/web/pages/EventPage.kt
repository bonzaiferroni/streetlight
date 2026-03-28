package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.Event
import streetlight.web.shells.eventProfileShell

fun HTML.eventPage(event: Event, styles: String) {
    appHead("Streetlight | ${event.title}", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        eventProfileShell(event)
    }
}