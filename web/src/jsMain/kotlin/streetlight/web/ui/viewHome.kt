package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.queryAndWireSwapBlock
import koala.dom.queryAndWireSwitch
import koala.dom.shellBox
import koala.dom.wireBlock
import koala.html.Id
import koala.html.textBlock
import koala.model.mapDistinct
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyProfileKey
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
    // queryAndWireToggleBlock(element)
    wireInterestControls(app, root)
    wireStarredEvents(root)

    wireStreetMap()
}

fun ViewContext<Streetlight>.wireStarredEvents(root: HTMLElement) {
    val app = model
    val userInterest = app.userInterest
    val eventsFlow = userInterest.stateFlow.mapDistinct { it.events }
    val swapIdFlow = eventsFlow.mapDistinct {
        when (it.isEmpty()) {
            true -> HomeShellKey.StarInfoId
            else -> HomeShellKey.StarEventsId
        }
    }

    queryAndWireSwapBlock(root, HomeShellKey.StarSwapId, bindFlow = swapIdFlow)
    wireBlock(HomeShellKey.StarEventsId, root, wireOnView = false) {
        flowBlock(eventsFlow) { events ->
            column {
                events.forEach { event ->
                    textBlock(event.title)
                }
            }
        }
    }
}