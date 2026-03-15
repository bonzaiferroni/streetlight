package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.ViewContext
import koala.dom.shellBox
import koala.dom.wireGeoMap
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShell
import streetlight.web.shells.SpotlightContent
import streetlight.web.shells.homeShell

fun ViewContext<Streetlight>.viewHome() {
    val content = SpotlightContent(emptyList(), emptyList())
    shellBox(HomeShell.homeBoxId, model.geoMap, model.appScope) {
        homeShell(content)
    }

    wireStreetMap()
}