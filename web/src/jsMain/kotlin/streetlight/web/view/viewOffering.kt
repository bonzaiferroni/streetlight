package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.dom.page.shellBox
import streetlight.model.data.Offering
import streetlight.model.ui.OfferingRoute
import streetlight.web.shell.OfferingShell
import streetlight.web.shell.offeringShell

fun ViewScope.viewOffering(offering: Offering) {
    shellBox {
        offeringShell(offering)
    }
}

fun RouteScope.viewOfferingRoute() {
    routeBlock<OfferingRoute, Offering>(OfferingShell.IslandId) { offering ->
        viewOffering(offering)
    }
}
