package streetlight.web.pages

import koala.PageResource
import koala.html.appHead
import kotlinx.html.HTML
import streetlight.model.ui.Screen
import streetlight.web.shells.aboutShell

fun HTML.aboutPage(resource: PageResource) {
    appHead("Streetlight | About", resource) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody(Screen.AboutApp, resource) {
        aboutShell()
    }
}