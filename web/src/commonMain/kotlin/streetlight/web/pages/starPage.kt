package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.StarContent
import streetlight.model.ui.Screen
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