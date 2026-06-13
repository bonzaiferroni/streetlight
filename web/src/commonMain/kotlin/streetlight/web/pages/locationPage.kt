package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.Location
import streetlight.model.data.LocationContent
import streetlight.web.shells.locationShell

fun HTML.locationPage(content: LocationContent, styles: String) {
    appHead("${content.location.name} | Streetlight", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        locationShell(content)
    }
}