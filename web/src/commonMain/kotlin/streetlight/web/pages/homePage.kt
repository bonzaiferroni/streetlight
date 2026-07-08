package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import streetlight.model.data.HomeContent
import streetlight.web.Screen
import streetlight.web.shells.homeShell

fun HTML.homePage(content: HomeContent, styles: String) {
    appHead("Streetlight | Home", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody(Screen.Home) {
        homeShell(content)
    }
}