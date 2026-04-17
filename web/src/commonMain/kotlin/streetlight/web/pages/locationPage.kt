package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.Location
import streetlight.web.shells.locationProfileShell

fun HTML.locationPage(location: Location, styles: String) {
    appHead("${location.name} | Streetlight", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        locationProfileShell(location)
    }
}