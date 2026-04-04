package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.web.shells.GalaxyProfileContent
import streetlight.web.shells.StarProfileContent
import streetlight.web.shells.galaxyProfileShell
import streetlight.web.shells.starProfileShell

fun HTML.starProfilePage(content: StarProfileContent, styles: String) {
    val star = content.star
    appHead("Streetlight | ${star.name}", styles) {
        // supportProtobuf()
        supportGeoMap()
    }
    appBody {
        starProfileShell(content)
    }
}