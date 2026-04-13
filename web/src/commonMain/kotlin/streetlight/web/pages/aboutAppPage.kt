package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.web.shells.HomeContent
import streetlight.web.shells.aboutAppShell
import streetlight.web.shells.homeShell

fun HTML.aboutAppPage(styles: String) {
    appHead("Streetlight | About", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        aboutAppShell()
    }
}