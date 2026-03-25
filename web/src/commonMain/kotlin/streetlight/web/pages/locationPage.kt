package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.Location
import streetlight.web.shells.locationShell

fun HTML.locationPage(location: Location) {
    appHead("${location.name} | Streetlight") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        locationShell(location)
    }
}