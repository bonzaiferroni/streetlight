package streetlight.web.ui

import koala.dom.*
import koala.html.Id
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShellKey
import streetlight.web.shells.HomeContent
import streetlight.web.shells.homeShell

fun ViewContext<Streetlight>.viewHome() {
    val app = model
    val content = HomeContent(emptyList(), emptyList())
    val root = shellBox(HomeShellKey.ContainerId, app.geoMap, app.appScope) {
        homeShell(content)
    }

    queryAndWireSwitch(root, Id("bruh"), onToggle = { console.log("bruh") })
    wireEventStars(root)
    wireGalaxyStars(root)
    wireStarredEvents(root)
    wireGalaxyMenu(app, root, null)

    wireStreetMap()
}