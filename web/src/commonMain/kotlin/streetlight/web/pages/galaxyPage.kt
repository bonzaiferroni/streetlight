package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.data.GalaxyContent
import streetlight.model.ui.Screen
import streetlight.web.shells.galaxyShell

fun HTML.galaxyPage(content: GalaxyContent, styles: String) {
    val galaxy = content.galaxy
    appHead("Streetlight | ${galaxy.name}", styles) {
        supportGeoMap()
    }
    appBody(Screen.Galaxy) {
        galaxyShell(content)
    }
}