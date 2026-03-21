package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.shellBox
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShell
import streetlight.web.shells.HomeContent
import streetlight.web.shells.homeShell

fun ViewContext<Streetlight>.viewHome() {
    val content = HomeContent(emptyList(), emptyList())
    shellBox(HomeShell.homeBoxId, model.geoMap, model.appScope) {
        homeShell(content)
    }

    wireStreetMap()
}