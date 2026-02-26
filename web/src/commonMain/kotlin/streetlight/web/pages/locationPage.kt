package streetlight.web.pages

import koala.html.head
import kotlinx.html.HTML
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.web.shells.homeShell
import streetlight.web.shells.locationShell

fun HTML.locationPage(location: Location) {
    head("${location.name} | Streetlight") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        locationShell(location)
    }
}