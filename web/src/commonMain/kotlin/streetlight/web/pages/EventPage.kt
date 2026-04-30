package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.EventLocation
import streetlight.web.shells.eventShell

fun HTML.eventPage(event: EventLocation, styles: String) {
    appHead("Streetlight | ${event.title}", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        eventShell(event)
    }
}