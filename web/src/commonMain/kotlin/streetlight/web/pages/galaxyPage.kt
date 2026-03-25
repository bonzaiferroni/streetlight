package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.web.shells.GalaxyProfileContent
import streetlight.web.shells.galaxyShell

fun HTML.galaxyPage(content: GalaxyProfileContent) {
    val galaxy = content.galaxy
    appHead("Streetlight | ${galaxy.name}") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        galaxyShell(content)
    }
}