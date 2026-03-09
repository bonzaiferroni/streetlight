package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.shellBox
import koala.dom.wireGeoMap
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShell
import streetlight.web.shells.homeShell

fun RenderContext.viewHome(app: Streetlight) {
    val element = shellBox(HomeShell.homeBoxId) {
        homeShell()
    }

    wireGeoMap(app.geoMap, app.appScope, element)
    wireMapPanel(app)
}