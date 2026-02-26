package streetlight.web

import koala.dom.RenderContext
import koala.dom.shellBox
import koala.dom.wireGeoMap
import streetlight.web.shells.HomeShell
import streetlight.web.shells.homeShell

fun RenderContext.viewHome(app: AppContext) {
    val element = shellBox(HomeShell.homeBoxId) {
        homeShell()
    }

    wireGeoMap(app.geoMap, app.appScope, element)
    wireMapPanel(app)
}