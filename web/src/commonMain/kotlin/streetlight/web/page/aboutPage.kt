package streetlight.web.page

import koala.PageResource
import koala.page.appHead
import kotlinx.html.HTML
import streetlight.model.ui.Screen
import streetlight.web.shell.aboutShell

fun HTML.aboutPage(resource: PageResource) {
    appHead("Streetlight | About", resource) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody(Screen.AboutApp, resource) {
        aboutShell()
    }
}