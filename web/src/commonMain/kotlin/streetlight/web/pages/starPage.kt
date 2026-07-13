package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.StarContent
import streetlight.web.Screen
import streetlight.web.shells.StarProfileContent
import streetlight.web.shells.starProfileShell
import streetlight.web.shells.starShell

fun HTML.starPage(content: StarContent, styles: String) {
    val star = content.star
    appHead("Streetlight | ${star.name}", styles) {
        // supportProtobuf()
        supportGeoMap()
    }
    appBody(Screen.Star) {
        starShell(content)
    }
}