package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.queryAndWireSwitch
import koala.dom.shellBox
import koala.dom.wireSwitch
import koala.html.Id
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShell
import streetlight.web.shells.HomeContent
import streetlight.web.shells.homeShell

fun ViewContext<Streetlight>.viewHome() {
    val content = HomeContent(emptyList(), emptyList())
    val element = shellBox(HomeShell.homeBoxId, model.geoMap, model.appScope) {
        homeShell(content)
    }

    queryAndWireSwitch(element, Id("ey"), onToggle = { console.log("eyy")})
    // queryAndWireToggleBlock(element)

    wireStreetMap()
}