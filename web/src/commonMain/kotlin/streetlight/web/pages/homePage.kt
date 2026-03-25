package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import streetlight.web.shells.HomeContent
import streetlight.web.shells.homeShell

fun HTML.homePage(content: HomeContent) {
    appHead("Streetlight | Home") {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        homeShell(content)
    }
}