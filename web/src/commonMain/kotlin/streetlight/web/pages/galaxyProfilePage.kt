package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.web.shells.GalaxyProfileContent
import streetlight.web.shells.galaxyProfileShell

fun HTML.galaxyProfilePage(content: GalaxyProfileContent, styles: String) {
    val galaxy = content.galaxy
    appHead("Streetlight | ${galaxy.name}", styles) {
        // supportProtobuf()
        supportGeoMap()
    }
    appBody {
        galaxyProfileShell(content)
    }
}