package streetlight.web.subApp

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.closeOpenPopovers
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.dom.page.shellBox
import koala.dom.page.shellBoxWithMap
import koala.model.MarkerMap
import streetlight.web.model.setPoints
import koala.model.GeoCamera
import streetlight.model.data.EventLocation
import streetlight.model.ui.EventRoute
import streetlight.model.ui.Screen
import streetlight.web.shell.LocationAppEventShell
import streetlight.web.shell.locationAppEventShell
import koala.dom.routeBlock
import koala.model.Portal
import koala.model.RouteInflator
import koala.modifier.Magic
import koala.modifier.modify
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationAppRoute
import streetlight.model.ui.LocationAppScreen
import streetlight.web.dom.page.applyTheme
import streetlight.web.shell.LocationAppShell
import streetlight.web.shell.locationAppShell
import streetlight.web.view.RouteView
import web.dom.document

/** Renders the routes of a LocationApp from its [Portal]. */
fun ViewScope.viewLocationPortal() {
    val portal = app.get<Portal>()
    val inflator = app.get<RouteInflator>()

    flowBlock(
        tap = portal.screenState,
        mod = modify(Magic),
        name = ::viewLocationPortal.name,
        onTransition = { document.closeOpenPopovers() },
    ) { screen ->
        val routeScope = RouteScope(this, inflator, portal.stateNow)
        RouteView.setActiveScope(routeScope)
        with (routeScope) {
            when (screen) {
                LocationAppScreen.Home -> viewLocationAppHome()
                Screen.Event -> viewLocationAppEvent()
                else -> textBlock("Coming soon: $screen")
            }
        }
    }
}

/** An event at the location of a LocationApp. */
private fun RouteScope.viewLocationAppEvent() {
    val camera = app.get<GeoCamera>()

    routeBlock<EventRoute, EventLocation>(LocationAppEventShell.IslandId) { event ->
        val markerMap = MarkerMap(app.get()).apply { setPoints(listOf(event)) }
        shellBoxWithMap(markerMap) {
            locationAppEventShell(event)
        }

        camera.panMap(event.geoPoint)
    }
}

/** The home of a LocationApp: the location's page. */
private fun RouteScope.viewLocationAppHome() {
    routeBlock<LocationAppRoute, LocationContent>(LocationAppShell.IslandId) { content ->
        shellBox {
            locationAppShell(content)
            applyTheme(content.design?.theme)
        }
    }
}
