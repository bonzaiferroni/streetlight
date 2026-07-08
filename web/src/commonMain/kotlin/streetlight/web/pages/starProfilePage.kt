package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.web.Screen
import streetlight.web.shells.StarProfileContent
import streetlight.web.shells.starProfileShell

fun HTML.starProfilePage(content: StarProfileContent, styles: String) {
    val star = content.star
    appHead("Streetlight | ${star.name}", styles) {
        // supportProtobuf()
        supportGeoMap()
    }
    appBody(Screen.Star) {
        starProfileShell(content)
    }
}