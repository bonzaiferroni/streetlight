package streetlight.web.pages

import koala.html.head
import kotlinx.html.HTML
import streetlight.model.data.Galaxy
import streetlight.web.shells.eventProfileShell
import streetlight.web.shells.galaxyShell

fun HTML.galaxyPage(galaxy: Galaxy) {
    head("Streetlight | ${galaxy.name}") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        galaxyShell(galaxy, emptyList())
    }
}