package streetlight.web.view

import kampfire.model.toDataOrNull
import kampfire.model.reactIn
import koala.dom.*
import koala.model.withLabel
import streetlight.model.data.CityContent
import streetlight.model.ui.CityConfigRoute
import streetlight.model.ui.CityRoute
import streetlight.model.ui.EventsMapRoute
import streetlight.web.shell.CityShell
import streetlight.web.shell.cityShell
import koala.dom.page.*
import streetlight.web.model.api
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.session
import streetlight.web.model.toaster

fun ViewScope.viewCity(content: CityContent) {
    shellBox {
        cityShell(content)
    }

    applyTheme(null)

    followFeedRoute<CityRoute>({ CityRoute(content.city.slug, it) }) { route ->
        api.city.readCityContent(route.slug, route.feed).toDataOrNull(toaster) { it.feed }
    }
}

fun RouteScope.viewCityRoute() {
    routeBlock<CityRoute, CityContent>(CityShell.IslandId) { content ->
        viewCity(content)
    }
}
