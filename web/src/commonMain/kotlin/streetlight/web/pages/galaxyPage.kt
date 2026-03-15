package streetlight.web.pages

import koala.html.head
import kotlinx.html.HTML
import streetlight.model.data.Galaxy
import streetlight.web.shells.GalaxyShellContent
import streetlight.web.shells.eventProfileShell
import streetlight.web.shells.galaxyShell

fun HTML.galaxyPage(content: GalaxyShellContent) {
    val galaxy = content.galaxy
    head("Streetlight | ${galaxy.name}") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        galaxyShell(content)
    }
}