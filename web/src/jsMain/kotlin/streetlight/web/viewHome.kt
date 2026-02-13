package streetlight.web

import kampfire.model.GeoPoint
import koala.css.AlignItemsCenter
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.box
import koala.dom.column
import koala.dom.shellBox
import koala.dom.tab
import koala.dom.tabs
import koala.html.Id
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.web.shells.HomeShell
import streetlight.web.shells.homeShell

fun RenderContext.viewHome(app: AppContext) {
    val element = shellBox(HomeShell.homeBoxId) {
        homeShell()
    }
    wireGeoMap(app.geoMap, app.appScope, element)

    viewMapPanel(app)
}