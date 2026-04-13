package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML
import streetlight.web.shells.aboutShell

fun HTML.aboutPage(styles: String) {
    appHead("Streetlight | About", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        aboutShell()
    }
}