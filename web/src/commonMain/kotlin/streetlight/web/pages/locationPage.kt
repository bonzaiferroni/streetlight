package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.LocationContent
import streetlight.model.ui.Screen
import streetlight.web.shells.locationShell

fun HTML.locationPage(content: LocationContent, styles: String) {
    appHead("${content.location.name} | Streetlight", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody(Screen.Location, content.design?.theme) {
        locationShell(content)
    }
}