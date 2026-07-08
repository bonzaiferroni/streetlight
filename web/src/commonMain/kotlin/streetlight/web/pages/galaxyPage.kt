package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.GalaxyContent
import streetlight.web.Screen
import streetlight.web.shells.galaxyShell

fun HTML.galaxyPage(content: GalaxyContent, styles: String) {
    val galaxy = content.galaxy
    appHead("Streetlight | ${galaxy.name}", styles) {
        // supportProtobuf()
        supportGeoMap()
    }
    appBody(Screen.Galaxy) {
        galaxyShell(content)
    }
}