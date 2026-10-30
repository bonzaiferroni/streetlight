package streetlight.web.view

import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.GalaxyMapRoute
import kampfire.model.reactIn
import koala.dom.ViewScope
import streetlight.model.ui.CityMap
import streetlight.model.ui.GalaxyMap
import streetlight.model.ui.EventsMap
import streetlight.web.model.Earth
import streetlight.web.model.dock

context(model: Earth)
fun ViewScope.earthMenu() {
    model.focusEntitiesState.reactIn(contentScope) { focus ->
        dock.setVisible(focus == null)
    }
}
