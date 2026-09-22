package streetlight.web.ui

import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.GalaxyMapRoute
import kampfire.model.reactIn
import koala.dom.ViewScope
import streetlight.model.ui.CityMap
import streetlight.model.ui.GalaxyMap
import streetlight.model.ui.PostMap
import streetlight.web.model.Earth
import streetlight.web.model.RouteDockState

fun ViewScope.earthMenu(model: Earth) {
    model.focusState.reactIn(contentScope) { focus ->
        dock.setVisible(focus == null)
    }

    model.mapState.reactIn(contentScope) { map ->
        when (map) {
            is GalaxyMap -> map.galaxy?.let { dock.mergeState(GalaxyMapRoute(it.slug), RouteDockState(title = it.name)) }
            is CityMap -> map.city?.let { dock.mergeState(CityMapRoute(it.slug), RouteDockState(title = it.name)) }
            is PostMap, null -> Unit
        }
    }
}
