package streetlight.web.ui

import kampfire.model.reactIn
import koala.dom.ViewScope
import streetlight.model.ui.CityMap
import streetlight.model.ui.GalaxyMap
import streetlight.model.ui.PostMap
import streetlight.web.model.Earth
import streetlight.web.model.RouteDockState

fun ViewScope.earthMenu(model: Earth) {
    model.mapState.reactIn(contentScope) { map ->
        val title = when (map) {
            is GalaxyMap -> map.galaxy?.name
            is CityMap -> map.city?.name
            is PostMap, null -> null
        }
        title?.let { dock.mergeState(RouteDockState(title = it)) }
    }
}
